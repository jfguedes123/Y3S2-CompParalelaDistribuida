package Scenes;
import Client.EchoClientHandler;
import Client.TimeClient;
import Server.ClientManager;

import java.util.Objects;
import java.util.Scanner;


public class InGame {

    public InGame(EchoClientHandler socket, Scanner input) {

        System.out.println("In Game");
        System.out.println("Room number: " + socket.getRoomId());
        System.out.println("The word is " + socket.getGameWord());
        System.out.println("The turn is" + socket.getGameTurn());




        if (Objects.equals(socket.getGameTurn(), "YourTurn")) {

            System.out.println("It's your turn!");
            System.out.println("Enter your letter/word: ");
            //input.nextLine();
            String guess = input.nextLine();

            if ("3".equals(guess)) {
                socket.quit();  return; }

            if (guess.length() == 1) {
                socket.guessLetter(guess);
            } else {
                socket.guessWord(guess);
            }
        }
        else if (Objects.equals(socket.getGameTurn(), "OpponentTurn")) {
            System.out.println("It's your opponent's turn!");
            String quit = input.nextLine();
            if ("3".equals(quit)) {
                System.out.println("You quit the game");
                socket.quit();   }
        }
        else {
            System.out.println("Waiting for the server to tell the play");
        }
    }

}


// this should be waiting for the requests from the server

