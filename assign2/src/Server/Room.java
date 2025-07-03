package Server;

import java.util.ArrayList;
import java.time.LocalDateTime;


public class Room {

    Integer id;
    Game game;
    // here the gamelogic
    ArrayList<ServerClient> clients = new ArrayList<>();
    boolean isFull = false;
    boolean isStarted = false;

    public LocalDateTime startedTime;

    public Room(Integer id) {
        this.id = id;

    }

    public boolean addClient(ServerClient client) {
        if (clients.size() < 3) {
            clients.add(client);
            if (clients.size() == 2) {
                isFull = true;

            }
            return true;
        }
        return false;
    }

    public boolean isConnected(ServerClient client) {
        return clients.contains(client);

    }

    public void startGame(boolean isRanked) {

        Game game = new Game(this,isRanked);
        this.game = game;
        Thread gameThread = new Thread(game::startGame);
        gameThread.start();

    }

    public void removePlayer(ServerClient client) {
        clients.remove(client);
    }

    public void stopGame(Thread gameThread) {
        gameThread.interrupt();
    }


    // TODO: getters and setters

    public Game getGame() {
        return this.game;
    }

    public Integer getId() {
        return id;
    }

    public ArrayList<ServerClient> getClients() {
        return clients;
    }

}
