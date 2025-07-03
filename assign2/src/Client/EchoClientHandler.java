package Client;

import java.io.*;
import java.net.*;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static Client.TimeClient.openPage;

public class EchoClientHandler {

    public enum SceneName {
        LOGIN,
        CONNECTSERVER,
        SIGNUP,
        MAINMENU,
        INGAME
    }

    private Socket clientSocket;
    private String serverAddress;
    private int serverPort;
    private boolean reconnecting = false;

    private String loginEmail;
    private String roomId;
    private String token;
    private TimeClient.pageName currentPage;


    private String GameTurn = null;
    private String GameWord = null;
    private String GameWinner = null;


    private  PrintWriter out;
    private BufferedReader in;

    Thread listener = null;

    public EchoClientHandler() {
        Socket socket = new Socket();
        this.clientSocket = socket;
    }

    // TODO: makes the connection to the server and starts the listener thread
    public void connect(String addr, int port) { // connecta ao server e inicia o listener, cria um novo socket se estiver a reconnectar
        try {
            serverAddress = addr;
            serverPort = port;
            InetAddress ip = InetAddress.getByName(addr);

            if(reconnecting){
                this.clientSocket = new Socket();
            }

            clientSocket.connect(new InetSocketAddress(ip, port), 1000);
            System.out.println("Connected to server");

            if(reconnecting){
                openPage(this.currentPage);
            }

            reconnecting = false;

            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            if(listener != null && listener.isAlive()) {
                listener.interrupt();
            }
            //out.println(sendPlayerId(token));
            listener = new Thread(this::listen);
            listener.start();
        } catch (IOException e) {
            reconnecting = true;
            System.out.println("Error connecting to server: " + e.getMessage());
        }
    }


    public void reconnect() throws InterruptedException { // tenta reconectar ao server a cada 5 segundos fazendo novamente connect
        if(!reconnecting) {
            reconnecting = true;
            System.out.println("Reconnecting to server");
            connect(serverAddress, serverPort);

        } else {
            System.out.println("Trying to reconnect in 5 seconds");

            Thread.sleep(5000);
            reconnecting = false;
        }
    }



    // TODO: listens for messages from the server
    public void listen() {
        boolean running = true;
        System.out.println("Listening for messages");
        String key = null;
        String value = null;

        while(running){

            try {
                String request = in.readLine();
                if (request != null) {
                    System.out.println("Received message: " + request);
                    //reconnected();
                    String[] keyValuePairs = request.split(" ");

                    String pair = keyValuePairs[0];
                    key = pair.split(":")[0];

                    switch (key) {

                        case "login" -> onReceivedLogin(request);
                        case "roomId" -> onReceivedFindMatch(pair.split(":")[1]);
                        case "quit" -> running = false;
                        case "gameevent" -> {
                            System.out.println("Received game event: " + request);
                            onReceivedGameEvent(request);
                        }
                        case "gameMessage" -> {
                            System.out.println("Received game message: " + request);
                            onReceivedGameMessage(request);
                        }
                        case "quitgame" -> onReceiveQuit(request);
                        case null, default -> System.out.println("Invalid scene");
                    }
                }
            } catch (IOException e) {
                try {
                    this.reconnect();
                } catch (InterruptedException ex) {
                    System.out.println("Error reconnecting: " + ex.getMessage());
                    throw new RuntimeException(ex);
                }
            }
        }
        try {
            // closing resources
            clientSocket.close();
            out.close();
            in.close();
        } catch (IOException ex) {
            Logger.getLogger(EchoClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // TODO: HANDLES RECEIVED MESSAGES FROM THE SERVER

    public void onReceiveQuit(String request){
        System.out.println("You quit the game");
        this.roomId = null;
        openPage(TimeClient.pageName.MAINMENU);
    }

    public void onReceivedGameMessage(String request) {
        String message = request.split(":")[1];
        System.out.println("Received Game Message: " + message);
    }

    public void onReceivedGameEvent(String request) {
        String[] keyValuePairs = request.split(" ");
        String roomId = keyValuePairs[0].split(":")[1];
        String Turn = keyValuePairs[1].split(":")[1];
        String word = keyValuePairs[2].split(":")[1];
        String winner = keyValuePairs[3].split(":")[1];

        this.GameTurn = Turn;
        this.GameWord = word;
        this.GameWinner = winner;

        System.out.println("Received Game Event: " + roomId + " " + Turn + " " + word + " " + winner);
            openPage(TimeClient.pageName.INGAME);
    }



    public void onReceivedLogin(String request) { // neste caso sabemos se o Login foi registado pelo server, senão tentamos novamente

        String[] keyValuePairs = request.split(" ");
        String value = keyValuePairs[0].split(":")[1];
        String token = keyValuePairs[1].split(":")[1];

        if(value.equals("success")) {
            System.out.println("Login successful");
            this.token = token;
            openPage(TimeClient.pageName.MAINMENU);
            //pass to next scene
        } else {
            System.out.println("Login failed");
            TimeClient.openPage(TimeClient.pageName.LOGIN);
            // show error message
        }
    }

    public void onReceivedFindMatch(String received) { //
        System.out.println("Found Room");
        this.roomId = received;
        openPage(TimeClient.pageName.INGAME);
    }

    // TODO: FUNCTIONS TO SEND REQUESTS TO THE SERVER

    public void login(String email, String password) { // envia o request para o login
        String scene = "login";

        String request = "scene:" + scene +  " loginemail:" + email  + " loginpassword:" + password  +"\n";
       // System.out.println(request);
        out.println(request);
    }

    public void findRankedMatch() { // envia o request para a partida ranked
        String scene = "findmatch";
        String request = "scene:" + scene +  " gametype:" + "ranked"; // add here the rating of the player
        out.println(request);
    }

    public void findSimpleMatch() { // envia o request para a partida Simple
        String scene = "findmatch";
        String request = "scene:" + scene + " gametype:" + "simple"  ; // add here the rating of the player
        out.println(request);
    }

    public void RoomId(String roomId) {
        String request = "roomid:" + roomId + ";";
        out.println(request);
    }

    public void guessLetter(String move) {
        System.out.println("You guessed letter: " + move);
        String request =  "scene:" + "gameevent"  + " letter:" + move ;
        out.println(request);
        System.out.println("Sendingg letter: " + move);
    }

    public void guessWord(String word) {
        String request = "scene:" + "gameevent"  + " word:" + word ;
        out.println(request);
    }

    public void quit() {
        String request = "scene:gameevent" + " quit";
        out.println(request);
        openPage(TimeClient.pageName.MAINMENU);
    }

    // TODO: GETTERS AND SETTERS
    public String getLoginEmail() {
            return this.loginEmail;
        }

    public void setLoginEmail(String loginEmail) {
        this.loginEmail = loginEmail;
    }

    public String getRoomId() {
        return this.roomId;
    }

    public void setPageName(TimeClient.pageName pageName) { // altera a página atual do user
        this.currentPage = pageName;
    }

    public String getGameTurn() {
        return GameTurn;
    }

    public String getGameWord() {
        return GameWord;
    }

    public String getGameWinner() {
        return GameWinner;
    }
}



