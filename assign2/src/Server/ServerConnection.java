package Server;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static Server.Server.clientManager;

public class ServerConnection {

    Socket clientSocket;

    private PrintWriter out;
    private BufferedReader in;
    Player player = new Player();

    public ServerConnection(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(), true);
    }


    public boolean isReconnect() throws IOException {
        boolean connected = false;
        while (!connected) {

            try {
                String request = in.readLine();
                if (request == null) {
                    return false;
                }
                System.out.println("Reconnect request: " + request);
                String[] keyValuePairs = request.split(" ");
                String pair = keyValuePairs[0];
                String key = pair.split(":")[1];

                if (key.equals("login")) {
                    System.out.println("djsalkfkldsa");
                     connected = onReceivedLogin(request);

                }

            } catch (IOException e) {
                System.out.println("Error recognizing reconnect request");
                e.printStackTrace();
            }
        }
        System.out.println(clientManager.getDisconnectedClients().size());
        System.out.println(clientManager.getClients().size());

        boolean connectedclient = clientManager.isDisconnected(this.player.getId());
        System.out.println(this.player.getId());
        System.out.println(connectedclient);
        if (connectedclient) {
            System.out.println("BBBBBBBBBBBBBBBBB");
            ServerClient client = clientManager.getClient(this.player.getId());
                try {
                client.setClientSocket(clientSocket);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            clientManager.removeDisconnectedClientBackOnline(this.player.getId());
            return true;
        } else {
            return false;
        }
    }

    public boolean onReceivedLogin(String request) {
        System.out.println("Login request received");

        String[] keyValuePairs = request.split(" ");

        String loginEmail = keyValuePairs[1].split(":")[1];
        String loginPassword = keyValuePairs[2].split(":")[1];
        String HashedPassword = hashPassword(loginPassword);

        String fileToken = "";

        String filePath = "../assign2/src/Scenes/players.txt";
        boolean isValidToken = false;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] credentials = line.split(",");
                if (credentials.length == 5) {  // Ensure the line has all required fields
                    String fileUsername = credentials[0].trim();
                    String fileEmail = credentials[1].trim();
                    String filePasswordHash = credentials[2].trim();
                    int fileRank = Integer.parseInt(credentials[3].trim());
                     fileToken = credentials[4].trim();
                    if (loginEmail.equals(fileEmail) && HashedPassword.equals(filePasswordHash)) {
                        this.player.setId(fileToken);
                        this.player.setName(fileUsername);
                        this.player.setPassword(filePasswordHash);
                        this.player.setEmail(fileEmail);
                        this.player.setRank(fileRank);
                        isValidToken = true;
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("An error occurred while reading the players file.");
            e.printStackTrace();
        }

        if (isValidToken) {
            out.println("login:success " + "token:" + fileToken);
            return true;
        } else {
            out.println("login:failure");
            System.out.println("Invalid token received");
            return false;
        }

    }



    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Player getPlayer(){
        return this.player;
    }
}

