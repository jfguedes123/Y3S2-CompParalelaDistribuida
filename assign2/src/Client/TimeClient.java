package Client;

import Scenes.InGame;
import Scenes.Login;
import Scenes.MainMenu;

import java.io.*;
import java.util.Scanner;
import java.util.UUID;

/**
 * This program demonstrates a simple TCP/IP socket client.
 *
 * @author www.codejava.net
 */
public class TimeClient {

    public enum pageName {
        LOGIN,
        CONNECTSERVER,
        MAINMENU,
        INGAME,
        INQUEUE
    }

    public static Login login;
    public static MainMenu mainMenu;
    public static InGame inGame;

    public static Scanner input = new Scanner(System.in);
    private static EchoClientHandler clientSocket;

    public TimeClient() throws IOException {
        clientSocket = new EchoClientHandler();
            openPage(pageName.CONNECTSERVER);
    }

    public static void main(String[] args) {
        try {
            new TimeClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // controls the pages that are opened
    public static void openPage(pageName page) {
        switch (page) {

            case LOGIN:
                login = new Login(clientSocket, input);
                clientSocket.setPageName(pageName.LOGIN);
                break;
            case CONNECTSERVER:
                clientSocket.connect("localhost", 5003);
                clientSocket.setPageName(pageName.CONNECTSERVER);
                openPage(pageName.LOGIN);
                break;
            case MAINMENU:
                mainMenu = new MainMenu(clientSocket, input);
                clientSocket.setPageName(pageName.MAINMENU);
                break;
            case INGAME:
                clientSocket.setPageName(pageName.INGAME);
                inGame = new InGame(clientSocket, input);
                break;
            case INQUEUE:
                clientSocket.setPageName(pageName.INQUEUE);
                System.out.println("InQueue");
                input.nextLine();
                break;
        }
    }
}