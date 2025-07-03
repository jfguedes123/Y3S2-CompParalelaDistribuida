package Server;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Game {
    public ReentrantLock lock = new ReentrantLock();
    public boolean isRanked;
    public Room room;
    public ArrayList<ServerClient> players;
    public int current_player_turn;
    public boolean isFinished = false;
    public boolean receivedLetter = false;
    public boolean receivedWord = false;
    public String letterGuess;
    public String wordGuess;
    public Player winner;

    public Game(Room room,boolean isRanked) {
        this.room = room;
        players = room.clients;
        this.isRanked = isRanked;
    }

    public void startGame() {

        System.out.println("The game has started");
        String word = pickWord();
        StringBuilder current_word = new StringBuilder();
        current_word.append("_".repeat(word.length()));

        while (!isFinished) {

            for(int i = 0; i < players.size(); i++){
                current_player_turn = i;

                if(!players.get(current_player_turn).player.isOnline() || players.get(current_player_turn).player.room != room){
                     continue;
                }

                sendGameEvent(room.getId().toString(),i, current_word.toString());

                synchronized (lock) {
                    while (!(receivedLetter || receivedWord) && players.get(current_player_turn).player.isOnline() && players.get(current_player_turn).player.room == room) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }


                if(receivedLetter){

                    receivedLetter=false;
                    sendMessage(players.get(current_player_turn).player + " guessed the letter: " + letterGuess);
                    current_word =  addLettertoCurrentWord(word, current_word, letterGuess);
                    if(checkWinner(current_word, word)){
                        this.winner = players.get(current_player_turn).player;
                        finishGame();
                        showRank();
                        break;
                    }
                    sendMessage("The guess was incorrect!");

                }
                else if(receivedWord){
                    receivedWord=false;
                    sendMessage(players.get(current_player_turn).player + " guessed the word: " + wordGuess);
                    if(Objects.equals(wordGuess, word)){
                        this.winner = players.get(current_player_turn).player;
                        finishGame();
                        break;
                    }
                    sendMessage("The guess was incorrect!");
                    // needs to disconnect the user
                }
            }

        }
        LeaveRoom();
    }

    public void LeaveRoom(){
        for(int i = 0; i < players.size(); i++){
            players.get(i).send("quitgame");
            players.get(i).player.setRoomNull();
        }
    }

    public String pickWord(){
        List<String> words;
        try {
            words = Files.readAllLines(Paths.get("../assign2/src/Server/words.txt"));
        } catch (IOException e) {
            e.printStackTrace();
            return "Error in words.txt";
        }
        Random rand = new Random();
        int n = rand.nextInt(words.size());
        return words.get(n);
    }

    public synchronized void updateRanks(){
        for(int i = 0; i < players.size(); i++){
            if(players.get(i).player == winner){
                players.get(i).player.rank += 1;
            }
            else if(players.get(i).player.rank > 0){
                players.get(i).player.rank -= 1;
            }
        }
        sendMessage("The ranks have been updated!");
        updateRanksInFile();
    }

    private void updateRanksInFile() {
        File inputFile = new File("../assign2/src/Scenes/players.txt");
        List<String> fileContents = new ArrayList<>();

        lock.lock();
        try {
            try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
                String currentLine;
                while ((currentLine = reader.readLine()) != null) {
                    String[] parts = currentLine.split(",");
                    String playerId = parts[4];

                    for (ServerClient serverClient : players) {
                        if (serverClient.player.getId().equals(playerId)) {
                            parts[3] = String.valueOf(serverClient.player.rank);
                            break;
                        }
                    }

                    fileContents.add(String.join(",", parts));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(inputFile))) {
                for (String line : fileContents) {
                    writer.write(line + System.lineSeparator());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean checkWinner(StringBuilder current_word, String word) {
       return current_word.toString().equals(word);
    }

    public StringBuilder addLettertoCurrentWord(String word, StringBuilder current_word, String letter) {
        int[] indices = new int[word.length()];
        int count = 0;

        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == letter.charAt(0)) {
                indices[count++] = i;
            }
        }

        for (int i = 0; i < count; i++) {
            current_word.setCharAt(indices[i], letter.charAt(0));
        }
        return current_word;
    }

    public void finishGame(){
        sendMessage(players.get(current_player_turn).player.name + " won the game!");
        sendMessage("Your word was: " + wordGuess);

        if(isRanked) {
            updateRanks();
        }
        isFinished = true;
    }

    public void showRank() {
        for(int i = 0; i < players.size(); i++){
            players.get(i).send("Your rank: " + players.get(i).player.rank);
        }
    }

    public void receivedLetter(String letter,ServerClient player){
        synchronized (lock) {
            if (player == players.get(current_player_turn)) {
                letterGuess = letter;
                receivedLetter = true;
                lock.notifyAll();
            }
        }
    }

    public void receivedWord(String word, ServerClient player) {
        synchronized (lock) {
            if (player == players.get(current_player_turn)) {
                wordGuess = word;
                receivedWord = true;
                lock.notifyAll();
            }
        }
    }

    public void sendMessage(String update){
        for(int i = 0; i < players.size(); i++){
            players.get(i).sendGameMessage(update);
        }
    }

    public void sendGameEvent(String roomId, Integer Turn, String word){
        for(int i = 0; i < players.size(); i++){
            if (i ==Turn) players.get(i).send("gameevent:roomId:" + roomId + " Turn:" + "YourTurn" + " word:" + word + " winner:" + winner);
            else players.get(i).send("gameevent:roomId:" + roomId + " Turn:" + "OpponentTurn" + " word:" + word + " winner:" + winner);
        }
    }
}
