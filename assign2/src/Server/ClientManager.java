package Server;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static Server.RoomManager.createRRoom;

public class ClientManager {
    private static ClientManager instance;

    private final CopyOnWriteArrayList<ServerClient> clients = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<ServerClient> disconnectedClients = new CopyOnWriteArrayList<>();

    PlayerWaitingQueue rankedQueue = new PlayerWaitingQueue();
    PlayerWaitingQueue simpleQueue = new PlayerWaitingQueue();
    int counter;

    public ClientManager() {
        counter = 0;
    }

    public static synchronized ClientManager getInstance() {
        if (instance == null) {
            instance = new ClientManager();
        }
        return instance;
    }

    // TODO: GETTERS

    public CopyOnWriteArrayList<ServerClient> getClients() {
        return clients;
    }

    public CopyOnWriteArrayList<ServerClient> getDisconnectedClients() {
        return disconnectedClients;
    }

    // TODO: CLIENT MANAGEMENT LOGIC

    public synchronized boolean addOnlineClient(ServerClient client) {
        if (!clients.contains(client)) {
            clients.add(client);
            System.out.println("jfkldwasjfsa");
            System.out.println(clients.size());
            return true;
        }
        return false;
    }


    public synchronized void addDisconnectedClient(ServerClient client) {
        clients.remove(client);
        disconnectedClients.add(client);
        client.player.setOnline(false);
        System.out.println("Added to the array Client disconnected: " + client.player.id);

    }

    public synchronized boolean isDisconnected(String id) {
        for (ServerClient existingClient : disconnectedClients) {
            System.out.println(disconnectedClients.size());
            System.out.println(existingClient.getPlayer().getId());
            if (existingClient.getPlayer().getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void removeDisconnectedClientBackOnline(String clientId) {
        ServerClient client = null;
        for (ServerClient disconnectedClient : disconnectedClients) {
            if (disconnectedClient.getPlayer().getId().equals(clientId)) {
                client = disconnectedClient;
                System.out.println("he reconnected");
                break;
            }
        }

        if (client != null) {
            System.out.println("fjkdlsajçfdsjlkfdsajlfdsakljç");
            disconnectedClients.remove(client);
            clients.add(client);
            client.getPlayer().setOnline(true);
        } else {
            System.out.println("Client not found in disconnected clients: " + clientId);
        }
    }

    public synchronized void removeClient(ServerClient client) {
        if(clients.contains(client)) {
           // System.out.println("Client removed: " + client.player.id);
            removePlayerFromQueue(client);
            clients.remove(client);
            System.out.println("CCCCCCCCCCCCCCCCCCCCCCCCCCCC");
        }
        else if(disconnectedClients.contains(client)) {
            removePlayerFromQueue(client);
            disconnectedClients.remove(client);
            //System.out.println("Client removed: " + client.player.id);
        }
        else {
           // System.out.println("Client not found");
        }
    }

    // TODO: PLAYER MATCHMAKING LOGIC

    public void addPlayerToQueue(ServerClient serverClient, boolean ranked) { // adiciona um jogador a fila

        if (ranked) {
            if (!rankedQueue.contains(serverClient)) {
                rankedQueue.addPlayer(serverClient);
                System.out.println("Queue size: ");
                System.out.println(rankedQueue.getQueueSize());
            }
        } else {
            if (!simpleQueue.contains(serverClient)) {
                simpleQueue.addPlayer(serverClient);
                System.out.println("Queue size: ");
                System.out.println(simpleQueue.getQueueSize());
            }
        }
    }

    public synchronized void removePlayersFromQueue(ArrayList<ServerClient> players) { // remove um grupo de jogadores da fila
        for (ServerClient p : players) {
            removePlayerFromQueue(p);
        }
    }

    public synchronized void removePlayerFromQueue(ServerClient player){
        rankedQueue.removePlayer(player);
        simpleQueue.removePlayer(player);
    }

    synchronized void FindRoomsSimpleQueue() {
        ArrayList<ServerClient> group = new ArrayList<>();

        synchronized (simpleQueue) {
            for (ServerClient a : simpleQueue.getQueue()) {
                group.add(a);
                if (group.size() == 3) {
                    Room aRoom = createRRoom(group);

                    this.updateRooms(group, aRoom);
                    aRoom.startGame(false);
                    this.removePlayersFromQueue(group);

                    group.clear();
                }
            }
        }
    }

    public synchronized void FindRoomsRankedQueue() {
        CopyOnWriteArrayList<ServerClient> sortedQueue = new CopyOnWriteArrayList<>(rankedQueue.getQueue()); // Use CopyOnWriteArrayList for thread-safe iteration
        sortedQueue.sort(Comparator.comparing(ServerClient::getPlayer)
                .thenComparing(player -> player.getPlayer().getRank())
                .thenComparing(player -> player.getPlayer().getTimeInQueue()));

        ArrayList<ServerClient> group = new ArrayList<>();
        Set<ServerClient> uniquePlayers = new HashSet<>();

        for (int rank = 0; rank < 10; rank++) {

            for (ServerClient player : sortedQueue) {

                if (uniquePlayers.contains(player) || !player.player.calculateRange(rank)) {
                    continue;
                }

                group.add(player);
                uniquePlayers.add(player);

                if (group.size() == 3) {
                    System.out.println("Aqui");
                    System.out.println(rank);
                    Room a = createRRoom(group);
                    this.removePlayersFromQueue(group);
                    this.updateRooms(group, a);
                    this.resetIntervalAndTimeinQueue(group);
                    removeFromArrayList(group, sortedQueue);
                    a.startGame(true);

                    group.clear();
                    uniquePlayers.clear();
                }
            }
            group.clear();
            uniquePlayers.clear();
        }
    }

    public synchronized void resetIntervalAndTimeinQueue(ArrayList<ServerClient> group){
        for (ServerClient player : group){
            player.getPlayer().setTimeInQueue(0);
            player.getPlayer().interval = 0;
        }
    }

    private void removeFromArrayList(ArrayList<ServerClient> playersToRemove, CopyOnWriteArrayList<ServerClient> sortedQueue) {
        for (ServerClient player : playersToRemove) {
            sortedQueue.remove(player);
        }
    }

    public synchronized void updateRooms(ArrayList<ServerClient> players, Room room) {
        for (ServerClient p : players) {
            p.sendFoundRoom(room.getId().toString());
            p.getPlayer().setRoom(room);
        }
    }


    public synchronized void updatePlayersInterval() { // relaxa os ranks dependendo do tempo à que estão a espera
        counter += 1;
        for (ServerClient p : rankedQueue.getQueue()) {
            p.getPlayer().setTimeInQueue(p.getPlayer().getTimeInQueue() + 5);
        }
        for (ServerClient p : simpleQueue.getQueue()) {
            p.getPlayer().setTimeInQueue(p.getPlayer().getTimeInQueue() + 5);
        }
        if (counter == 2) {
            for (ServerClient p : rankedQueue.getQueue()) {
                p.getPlayer().expandInterval();
            }
            counter = 0;
        }
        FindRoomsRankedQueue();
        FindRoomsSimpleQueue();
    }

    public synchronized ServerClient getClient(String token) {
        for (ServerClient client : disconnectedClients) {
            if (client.getPlayer().getId().equals(token)) {
                return client;
            }
        }
        return null;
    }

}