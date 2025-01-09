package client;

import models.*;

import java.util.Scanner;
import java.io.IOException;
import java.util.ArrayList;

public class GameUI {
    private final Scanner scanner;

    public GameUI() {
        this.scanner = new Scanner(System.in);
    }

    public void displayMainMenu() {
        System.out.println("\n=== Main Menu ===");
        System.out.println("1. Create new game room");
        System.out.println("2. Join existing room");
        System.out.println("3. View active rooms");
        System.out.println("4. View my stats");
        System.out.println("5. Exit");
        System.out.print("Choose an option: ");
    }

    public String getPlayerName() {
        System.out.print("Enter your name: ");
        return scanner.nextLine().trim();
    }

    public String getMenuChoice() {
        return scanner.nextLine().trim();
    }

    public String getRoomName() {
        System.out.print("Enter room name: ");
        return scanner.nextLine().trim();
    }

    public String getRoomIdToJoin(ArrayList<GameRoom> rooms) {
        if (rooms.isEmpty()) {
            return "";
        }
        System.out.print("Enter room number to join: ");
        String input = scanner.nextLine().trim();
        try {
            int choice = Integer.parseInt(input);
            if (choice > 0 && choice <= rooms.size()) {
                return rooms.get(choice - 1).getGameRoomID().toString();
            }
        } catch (NumberFormatException ignored) {
        }
        return "";
    }

    public void displayActiveRooms(Iterable<GameRoom> rooms) {
        System.out.println("\n=== Active Rooms ===");
        ArrayList<GameRoom> availableRooms = new ArrayList<>();
        
        // First collect all waiting rooms
        for (GameRoom room : rooms) {
            if (room.getRoomStatus().equals(GameRoom.RoomStatus.WAITING)) {
                availableRooms.add(room);
            }
        }
        
        // Then display them with proper numbering
        if (availableRooms.isEmpty()) {
            System.out.println("No active rooms available.");
        } else {
            for (int i = 0; i < availableRooms.size(); i++) {
                GameRoom room = availableRooms.get(i);
                System.out.printf("%d. Room: %s (ID: %s), Status: %s%n",
                        i + 1, room.getGameRoomName(), room.getGameRoomID(), room.getRoomStatus());
            }
        }
    }

    public void displayPlayerStats(PlayerStats playerStats) {
        System.out.println("\n=== Player Stats ===");
        System.out.println(playerStats.toString());
    }

    public void displayBoard(GameState state) {
        char[][] board = state.getBoard();
        System.out.println("\nCurrent board:");
        System.out.println("  1 2 3");
        for (int i = 0; i < 3; i++) {
            System.out.print((i + 1) + " ");
            for (int j = 0; j < 3; j++) {
                char symbol = board[i][j];
                if (symbol == ' ') {
                    for (Move move : state.getMoves()) {
                        if (move.getRow() == i && move.getColumn() == j) {
                            symbol = move.getPlayer().equals(state.getPlayerX()) ? 'X' : 'O';
                            break;
                        }
                    }
                }
                System.out.print(symbol);
                if (j < 2) System.out.print("|");
            }
            System.out.println();
            if (i < 2) System.out.println("  -+-+-");
        }
        System.out.println();
    }

    public void displayGameResult(GameState state, Player currentPlayer) {
        if (state.getWinner() != null) {
            if (state.getWinner().equals(currentPlayer)) {
                System.out.println("Congratulations! You won!");
            } else {
                System.out.println("Game Over. You lost!");
            }
        } else {
            System.out.println("Game ended in a draw!");
        }
    }

    public String getUserInput(boolean inChatMode, Player currentPlayerTurn) {
        if (currentPlayerTurn != null && !inChatMode) {
            System.out.println("Your turn!");
        }
        System.out.print(inChatMode ? "You: " : "Enter your move (row[1-3] column[1-3]) or command: ");
        return scanner.nextLine().trim();
    }

    public void displayWaitingForMove(Player currentPlayerTurn) {
        System.out.println("Waiting for " + currentPlayerTurn.getName() + "'s move...");
    }

    public void displayMessage(String message) {
        System.out.println(message);
    }

    public void displayError(String error) {
        System.out.println("Error: " + error);
    }

    public String getInputIfAvailable(boolean inChatMode, Player currentPlayerTurn) {
        try {
            if (System.in.available() > 0) {
                return scanner.nextLine().trim();
            }
        } catch (IOException e) {
            displayError("Error reading input: " + e.getMessage());
        }
        return null;
    }
} 