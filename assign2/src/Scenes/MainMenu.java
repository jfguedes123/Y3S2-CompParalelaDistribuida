package Scenes;

import Client.EchoClientHandler;
import Client.TimeClient;
import Server.ClientManager;

import java.util.Scanner;

import static Client.TimeClient.openPage;

public class MainMenu {

    public MainMenu(EchoClientHandler socket, Scanner input) {
        System.out.println("Main Menu");
        System.out.println("1. Simple Game Mode");
        System.out.println("2. Rank Game Mode");
        System.out.println("3. Exit");
        System.out.println("Enter your choice: ");
        int choice = input.nextInt();

        switch (choice) {
            case 1:
                playSimpleGame(socket);
                break;
            case 2:
                playRankedGame(socket);
                break;
            case 3:
                exitGame(socket);
                break;
            default:
                System.out.println("Invalid choice. Please enter a number between 1 and 3.");
                break;
        }


    }

    private void playSimpleGame(EchoClientHandler socket) {
        System.out.println("Looking for a room...");
        socket.findSimpleMatch();

        openPage(TimeClient.pageName.INQUEUE);
    }

    private void playRankedGame(EchoClientHandler socket) {
        System.out.println("Looking for a ranked game...");
        socket.findRankedMatch();
        openPage(TimeClient.pageName.INQUEUE);
    }



    private void showHighScores() {
        System.out.println("Showing high scores...");
        // Implement high scores logic here
    }

    private void exitGame(EchoClientHandler socket) {
        socket.quit();
        System.out.println("Exiting the game...");
        // Implement exit logic here
    }
}
