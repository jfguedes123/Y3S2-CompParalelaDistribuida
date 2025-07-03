package Server;

import javax.xml.crypto.dsig.keyinfo.KeyValue;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

import static Server.Server.clientManager;
import static Server.Server.roomManager;

public class ServerClient implements Runnable {

    Socket clientSocket;

    //boolean isFindingMatch = false;
    Player player;

    private PrintWriter out;
    private BufferedReader in;

    public ServerClient(Socket client,Player player) throws IOException {
        this.clientSocket = client;
        this.player = player;
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(), true);

    }

    public void setClientSocket(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    public void run() { // listener para os request do user
        String request;
        boolean running = true;


        while (running) {
            try {
                System.out.println("Waiting for client request");

                request = in.readLine();

                if (request == null) {
                    clientManager.addDisconnectedClient(this);
                    boolean dc =waitingForClientReconnect();
                   // System.out.println("Error handling client");
                    if(dc) {
                        break;
                    }
                    continue;
                }

                System.out.println("here is the request:" + request);

                String scene = null;
                String[] keyValuePairs = request.split(" ");
               // System.out.println("keyValuePairs: " + keyValuePairs);
                String pair = keyValuePairs[0];
                String[] requestInformation = Arrays.copyOfRange(keyValuePairs, 1, keyValuePairs.length);

                String[] keyValue = pair.split(":");
                if (keyValue.length == 2) {
                    String key = keyValue[0];
                    String value = keyValue[1];
                    System.out.println(key + " " + value);
                    if (key.equals("scene")) {
                        scene = value;
                        if (scene.equals("reconnect")){
                        }
                         else if (scene.equals("findmatch")) {
                            System.out.println("entered here");
                            onReceivedFindMatch(requestInformation);
                        } else if (scene.equals("gameevent")) {
                            onReceiveGameEvent(requestInformation);}
                        else if (scene.equals("quit")) {
                            disconnectClient();
                            running = false;
                        }
                    } else {
                        System.out.println("Invalid scene received");
                    }
                }
            } catch (IOException e) {
                clientManager.addDisconnectedClient(this);
                boolean dc =waitingForClientReconnect();
                System.out.println("Error handling client");
                if(dc) {
                    break;
                }
              e.printStackTrace();
            }
        }
        try {
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Error closing the client socket");
        }
    }

    public void disconnectClient() {
        try {
            clientManager.removeClient(this);
            clientSocket.close();
            System.out.println("Client disconnected ");
        } catch (IOException e) {
            System.out.println("Error disconnecting client");
        }
    }

    public boolean waitingForClientReconnect() {
        try {
            Thread.sleep(30000);
            System.out.println("Ele está a dar disconnect");
            System.out.println(clientManager.getDisconnectedClients().size());
            System.out.println(clientManager.getClients().size());
            if (clientManager.getDisconnectedClients().contains(this)) {
                clientManager.removeClient(this);
                if(this.player.getRoom() != null) {
                    this.player.getRoom().removePlayer(this);
                }
                closeConnection();
                clientSocket.close();
                System.out.println("Client disconnected from the server");
                return true;
            }
            return false;
        } catch (InterruptedException e) {
            System.out.println("Error reconnecting client");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }




    public void onReceivedFindMatch(String[] request) { // ao receber o request de find match, verifica se é ranked ou simple e adiciona o player à queue correspondente
        System.out.println("Finding match...");

        String MatchType = request[0];

        String[] keyValue = MatchType.split(":");

        String key = keyValue[0];
        String value = keyValue[1];

        if (key.equals("gametype")){

            if(value.equals("ranked")){
                System.out.println("looking for ranked match");

                clientManager.addPlayerToQueue(this,true);
            } else if (value.equals("simple")){
                System.out.println("looking for simple match");

                clientManager.addPlayerToQueue(this,false);
            } else {
                System.out.println("Invalid game type");
            }
        }
    }


    public void onReceiveGameEvent(String[] request) {
         String MatchType = request[0];

         String[] keyValue = MatchType.split(":");

         String key = keyValue[0];
         String value = keyValue[1];

         if (key.equals("letter")){
             System.out.println("received letter " + value + " from player " + this.player.getId());
             this.player.getRoom().getGame().receivedLetter(value,this);
         }
         else if (key.equals("word")){
             this.player.getRoom().getGame().receivedWord(value,this);
         }
         else if (key.equals("quit")){
             this.player.getRoom().removePlayer(this);
         }
    }


    public void send(String message) {
        out.println(message);
    }


    public void sendFoundRoom(String roomId) {
        this.send("roomId:" + roomId);
    }

    public void sendGameMessage(String message) {
        this.send("gameMessage:" + message);
    }

    public void closeConnection() {
        try {
            if (out!= null) {
                out.close();
            }
            if (in!= null) {
                in.close();
            }
        } catch (IOException e) {
            System.out.println("Error closing connection");
            e.printStackTrace();
        }
    }

    public Player getPlayer() {
        return this.player;
    }
}
