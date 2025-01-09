package client;

import interfaces.GameServerInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Logger;

public class TicTacToeClient {
    private static final Logger LOGGER = Logger.getLogger(TicTacToeClient.class.getName());
    private final GameManager gameManager;
    private final GameUI ui;

    public TicTacToeClient() throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        GameServerInterface server = (GameServerInterface) registry.lookup("TicTacToeServer");
        this.ui = new GameUI();
        this.gameManager = new GameManager(server, ui);
    }

    public void start() {
        try {
            gameManager.login();
            mainMenu();
        } catch (RemoteException e) {
            ui.displayError("Error connecting to server: " + e.getMessage());
        }
    }

    private void mainMenu() throws RemoteException {
        while (true) {
            try {
                ui.displayMainMenu();
                String choice = ui.getMenuChoice();
                
                switch (choice) {
                    case "1" -> gameManager.createRoom();
                    case "2" -> gameManager.joinRoom();
                    case "3" -> ui.displayActiveRooms(gameManager.getActiveRooms());
                    case "4" -> gameManager.viewStats();
                    case "5" -> {
                        gameManager.exit();
                        ui.displayMessage("Goodbye!");
                        return;
                    }
                    default -> ui.displayError("Invalid option. Please try again.");
                }
            } catch (RemoteException e) {
                ui.displayError("Error communicating with server: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        try {
            TicTacToeClient client = new TicTacToeClient();
            client.start();
        } catch (RemoteException | NotBoundException e) {
            System.out.println("Error connecting to server: " + e.getMessage());
        }
    }
} 