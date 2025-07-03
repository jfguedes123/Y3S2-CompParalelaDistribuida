package Scenes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import Client.EchoClientHandler;

public class SignIn {
    public ReentrantLock lock = new ReentrantLock();
    private String token;

    public SignIn(EchoClientHandler socket, Scanner input) {
        String username;
        String email;
        String password;

        System.out.println("Sign up:");

        System.out.println("username: ");
        username = input.nextLine();

        System.out.println("email: ");
        email = input.nextLine();

        System.out.println("password: ");
        password = input.nextLine();

        System.out.println("Signing up...");

        if (registerUser(username, email, password)) {
            System.out.println("Registration successful!");
            socket.login(email, password);
        } else {
            System.out.println("Registration failed.");
        }
    }

    private boolean registerUser(String username, String email, String password) {
        String filePath = "assign2/src/Scenes/players.txt";
        String hashedPassword = hashPassword(password);
        String rank = "1";
        token = generateToken();

        if (hashedPassword == null) {
            System.out.println("An error occurred while hashing the password.");
            return false;
        }

        lock.lock();
        try {
            if (isUsernameOrEmailTaken(username, email, filePath)) {
                System.out.println("Username or email already taken.");
                return false;
            }

            try (PrintWriter out = new PrintWriter(new FileWriter(filePath, true))) {
                out.println(username + "," + email + "," + hashedPassword + "," + rank + "," + token);
                return true;
            } catch (IOException e) {
                System.out.println("An error occurred while writing to the players file.");
                e.printStackTrace();
            }
        } finally {
            lock.unlock();
        }
        return false;
    }

    private boolean isUsernameOrEmailTaken(String username, String email, String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] credentials = line.split(",");
                if (credentials.length >= 2) {
                    String fileUsername = credentials[0].trim();
                    String fileEmail = credentials[1].trim();
                    if (fileUsername.equals(username) || fileEmail.equals(email)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("An error occurred while reading the players file.");
            e.printStackTrace();
        }
        return false;
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

    private String generateToken() {
        return UUID.randomUUID().toString();
    }
}
