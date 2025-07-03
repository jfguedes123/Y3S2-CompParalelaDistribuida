package Scenes;

import Client.EchoClientHandler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

public class Login {
    public ReentrantLock lock = new ReentrantLock();

    public Login(EchoClientHandler socket, Scanner input) {
        System.out.println("Welcome! Please choose an option:");
        System.out.println("1. Log in");
        System.out.println("2. Sign up");

        int choice = Integer.parseInt(input.nextLine());

        if (choice == 1) {
            performLogin(socket, input);
        } else if (choice == 2) {
            new SignIn(socket, input);
        } else {
            System.out.println("Invalid choice. Please restart the application.");
        }
    }

    private void performLogin(EchoClientHandler socket, Scanner input) {
        String email;
        String password;

        System.out.println("Log in:");
        System.out.println("email: ");
        email = input.nextLine();

        System.out.println("password: ");
        password = input.nextLine();

        System.out.println("Logging in...");

        String token = getTokenIfValidCredentials(email, password);
        if (token != null) {
            socket.login(email, password);
        } else {
            System.out.println("Invalid email or password.");
            new Login(socket, input);
        }
    }

    private String getTokenIfValidCredentials(String email, String password) {
        String filePath = "assign2/src/Scenes/players.txt";
        System.out.println("Reading file: " + filePath);

        lock.lock();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] credentials = line.split(",");
                if (credentials.length == 5) {
                    String fileEmail = credentials[1].trim();
                    String filePasswordHash = credentials[2].trim();
                    String token = credentials[4].trim();
                    if (fileEmail.equals(email) && filePasswordHash.equals(hashPassword(password))) {
                        System.out.println("Credentials matched!");
                        return token;
                    }
                } else {
                    continue;
                }
            }
        } catch (IOException e) {
            System.out.println("An error occurred while reading the players file.");
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        System.out.println("No matching credentials found.");
        return null;
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
}
