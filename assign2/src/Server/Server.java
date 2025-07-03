package Server;



import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import java.net.*;
import java.util.*;
import java.util.concurrent.locks.*;


public class Server {

    public static ClientManager clientManager = new ClientManager();
    public static RoomManager roomManager = new RoomManager();
    public static ServerSocket serverSocket;
    private ReentrantLock playerListLock = new ReentrantLock();
    private ReentrantLock updateSchedulerLock = new ReentrantLock();
    private volatile boolean stopRequested = false;

    public Server(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            clientManager = new ClientManager();
            roomManager = new RoomManager();
            ServerConnection serverConnection;


            while (true) {
                Socket connectionServer = serverSocket.accept();
                System.out.println("Server connected");
                serverConnection = new ServerConnection(connectionServer);

                if (serverConnection.isReconnect()) {
                    System.out.println("Client reconnected");
                    continue;
                }

                ServerClient player = new ServerClient(connectionServer,serverConnection.getPlayer());
                clientManager.addOnlineClient(player);
                System.out.println("New client connected: " + connectionServer.getInetAddress().getHostAddress());


                new Thread(() -> {
                    player.run();
                }).start();

                new Thread(this::runUpdateTask).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Server closed on port " + port);
    }

    private void runUpdateTask() {
        while (!stopRequested) {
            updateSchedulerLock.lock();
            try {
                clientManager.updatePlayersInterval();
               // System.out.println("Finding matches...");

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Task interrupted.");
                }
            } finally {
                updateSchedulerLock.unlock();
            }
        }
    }

    public static void main(String[] args) {
        new Server(5003);
    }

}