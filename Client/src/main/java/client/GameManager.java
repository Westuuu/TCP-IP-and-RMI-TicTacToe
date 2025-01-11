package client;

import interfaces.GameServerInterface;
import models.*;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameManager {
    private final GameServerInterface server;
    private final GameUI ui;
    private Player currentPlayer;
    private ChatManager chatManager;
    private final AtomicBoolean inChatMode = new AtomicBoolean(false);

    private class ChatManager implements Runnable {
        private final UUID roomId;
        private final GameRoom room;
        private final boolean isRoomOwner;
        private ChatServer chatServer;
        private ChatClient chatClient;
        private final AtomicBoolean running = new AtomicBoolean(true);
        private final Thread chatThread;

        public ChatManager(UUID roomId, GameRoom room, boolean isRoomOwner) {
            this.roomId = roomId;
            this.room = room;
            this.isRoomOwner = isRoomOwner;
            this.chatThread = new Thread(this);
        }

        public void start() {
            chatThread.start();
        }

        public void stop() {
            running.set(false);
            if (chatServer != null) {
                chatServer.close();
            }
            if (chatClient != null) {
                chatClient.close();
            }
            chatThread.interrupt();
        }

        public void sendMessage(String message) {
            if (chatServer != null) {
                chatServer.sendMessage(message);
            } else if (chatClient != null) {
                chatClient.sendMessage(message);
            }
        }

        @Override
        public void run() {
            try {
                if (isRoomOwner) {
                    setupChatServer();
                } else {
                    setupChatClient();
                }

                while (running.get()) {
                    String message = null;
                    if (chatServer != null) {
                        message = chatServer.receiveMessage();
                    } else if (chatClient != null) {
                        message = chatClient.receiveMessage();
                    }
                    
                    if (message != null) {
                        ui.displayMessage("[CHAT] " + message);
                    }
                    
                    Thread.sleep(250);
                }
            } catch (IOException | InterruptedException e) {
                ui.displayError("Chat error: " + e.getMessage());
            } finally {
                stop();
            }
        }

        private void setupChatServer() throws IOException, InterruptedException {
            chatServer = new ChatServer(roomId);
            chatServer.start();
            int serverPort = chatServer.getPort();
            try {
                server.setOwnerPort(roomId, room.getOwner(), serverPort);
                ui.displayMessage("Chat server started on port " + serverPort + ", waiting for opponent to connect...");
                
                int waitAttempts = 0;
                while (!chatServer.isConnected() && waitAttempts < 30 && running.get()) {
                    Thread.sleep(1000);
                    waitAttempts++;
                }
                
                if (!chatServer.isConnected() && running.get()) {
                    throw new IOException("No client connected within timeout period");
                }
            } catch (RemoteException e) {
                throw new IOException("Failed to set up chat server: " + e.getMessage());
            }
        }

        private void setupChatClient() throws IOException, InterruptedException {
            Thread.sleep(2000);
            
            String ownerIP = null;
            int ownerPort = -1;
            int retries = 0;
            boolean connected = false;
            
            while (retries < 30 && !connected && running.get()) {
                try {
                    ownerIP = server.getOpponentIP(roomId, currentPlayer);
                    ownerPort = server.getPort(roomId, room.getOwner());
                    
                    if (ownerIP != null && ownerPort > 0) {
                        ui.displayMessage("Found owner's chat server at " + ownerIP + ":" + ownerPort);
                        chatClient = new ChatClient();
                        try {
                            chatClient.connect(ownerIP, ownerPort);
                            connected = true;
                            ui.displayMessage("Successfully connected to chat server!");
                            break;
                        } catch (IOException e) {
                            ui.displayMessage("Failed to connect to chat server, will retry...");
                        }
                    } else {
                        ui.displayMessage("Waiting for owner to start chat server... (Attempt " + (retries + 1) + "/30)");
                    }
                    
                    Thread.sleep(1000);
                    retries++;
                } catch (RemoteException e) {
                    ui.displayMessage("Error getting owner's chat info, will retry... (Attempt " + (retries + 1) + "/30)");
                    Thread.sleep(1000);
                    retries++;
                }
            }
            
            if (!connected && running.get()) {
                throw new IOException("Could not connect to owner's chat server after 30 attempts");
            }
        }
    }

    public GameManager(GameServerInterface server, GameUI ui) {
        this.server = server;
        this.ui = ui;
    }

    public void login() throws RemoteException {
        while (true) {
            String name = ui.getPlayerName();
            if (!name.isEmpty()) {
                currentPlayer = new Player(name);
                server.registerPlayer(currentPlayer);
                ui.displayMessage("Successfully logged in as " + name);
                break;
            }
        }
    }

    public void createRoom() throws RemoteException {
        String roomName = ui.getRoomName();
        UUID roomId = server.createRoom(roomName, currentPlayer);
        ui.displayMessage("Room created successfully!");
        waitForPlayers(roomId);
    }

    public void joinRoom() throws RemoteException {
        ArrayList<GameRoom> allRooms = server.getActiveRooms();
        ArrayList<GameRoom> availableRooms = new ArrayList<>();
        
        for (GameRoom room : allRooms) {
            if (room.getRoomStatus() == GameRoom.RoomStatus.WAITING) {
                availableRooms.add(room);
            }
        }
        
        ui.displayActiveRooms(availableRooms);
        String roomId = ui.getRoomIdToJoin(availableRooms);
        if (roomId.isEmpty()) {
            ui.displayError("Invalid room selection.");
            return;
        }
        try {
            UUID gameRoomID = UUID.fromString(roomId);
            server.joinRoom(gameRoomID, currentPlayer);
            ui.displayMessage("Successfully joined the room!");
            playGame(gameRoomID);
        } catch (IllegalArgumentException e) {
            ui.displayError("Invalid room ID format.");
        }
    }

    public void viewStats() {
        try {
            currentPlayer = server.getUpdatedPlayer(currentPlayer.getPlayerId());
            PlayerStats currentPlayerStats = currentPlayer.getPlayerStats();
            ui.displayPlayerStats(currentPlayerStats);
        } catch (RemoteException e) {
            ui.displayError("Error getting stats from server: " + e.getMessage());
        }
    }

    public ArrayList<GameRoom> getActiveRooms() throws RemoteException {
        return server.getActiveRooms();
    }

    private void waitForPlayers(UUID roomId) {
        try {
            ui.displayMessage("Waiting for another player to join...");
            while (true) {
                GameRoom room = server.getRoomInfo(roomId);
                if (room.isRoomFull()) {
                    server.startGame(roomId);
                    playGame(roomId);
                    break;
                }
                Thread.sleep(1000);
            }
        } catch (RemoteException | InterruptedException e) {
            ui.displayError("Error while waiting for players: " + e.getMessage());
        }
    }

    private void handleMove(UUID roomId, GameState state, String input) throws RemoteException {
        try {
            String[] parts = input.split("\\s+");
            if (parts.length != 2) {
                ui.displayError("Invalid input format. Please use 'row column' format.");
                return;
            }

            int row = Integer.parseInt(parts[0]) - 1;
            int col = Integer.parseInt(parts[1]) - 1;
            
            Move move = new Move(row, col, state.getMoves().size(), state.getGameID(), currentPlayer);
            try {
                server.makeMove(roomId, move);
            } catch (RemoteException e) {
                if (e.getMessage().contains("Invalid move")) {
                    ui.displayError("Invalid move. Please try again.");
                    return;
                }
                throw e;
            }
        } catch (NumberFormatException e) {
            ui.displayError("Invalid input. Please enter numbers.");
        }
    }

    private void handleUserInput(UUID roomId, GameState state) throws RemoteException {
        String input = ui.getUserInput(inChatMode.get(), state.getCurrentPlayerTurn());

        if (input.equals("/chat")) {
            inChatMode.set(true);
            ui.displayMessage("Entered chat mode. Type /move to return to game.");
        } else if (input.equals("/move")) {
            inChatMode.set(false);
            ui.displayMessage("Entered move mode. Type /chat to return to chat.");
        } else if (inChatMode.get()) {
            if (chatManager != null) {
                chatManager.sendMessage(input);
            }
        } else {
            handleMove(roomId, state, input);
        }
    }

    private void playGame(UUID roomId) {
        try {
            ui.displayMessage("Waiting for game to start...");
            GameRoom room = null;
            while (true) {
                room = server.getRoomInfo(roomId);
                if (room.getRoomStatus() == GameRoom.RoomStatus.PLAYING) {
                    break;
                }
                Thread.sleep(1000);
            }

            boolean isRoomOwner = currentPlayer.equals(room.getGameState().getPlayerX());
            
            chatManager = new ChatManager(roomId, room, isRoomOwner);
            chatManager.start();

            ui.displayMessage("\nGame started! Use /chat to enter chat mode and /move to enter move mode");
            
            int lastMoveCount = -1;
            
            while (true) {
                GameState state = server.getGameState(roomId);
                
                if (state.isFinished()) {
                    if (!inChatMode.get()) {
                        ui.displayBoard(state);
                    }
                    ui.displayGameResult(state, currentPlayer);
                    currentPlayer = server.getUpdatedPlayer(currentPlayer.getPlayerId());
                    cleanup();
                    break;
                }

                boolean stateChanged = state.getMoves().size() != lastMoveCount;
                
                if (stateChanged) {
                    ui.displayBoard(state);
                    lastMoveCount = state.getMoves().size();
                }
                
                if (state.getCurrentPlayerTurn() != null && state.getCurrentPlayerTurn().equals(currentPlayer)) {
                    handleUserInput(roomId, state);
                } else {
                    if (!inChatMode.get() && stateChanged) {
                        ui.displayWaitingForMove(state.getCurrentPlayerTurn());
                    }

                    String input = ui.getInputIfAvailable(inChatMode.get(), state.getCurrentPlayerTurn());
                    if (input != null) {
                        if (input.equals("/chat")) {
                            inChatMode.set(true);
                            ui.displayMessage("Entered chat mode. Type /move to return to game.");
                        } else if (input.equals("/move")) {
                            inChatMode.set(false);
                            ui.displayMessage("Entered move mode. Type /chat to return to chat.");
                        } else if (inChatMode.get()) {
                            if (chatManager != null) {
                                chatManager.sendMessage(input);
                            }
                        }
                    }
                    
                    Thread.sleep(100);
                }
            }
        } catch (RemoteException | InterruptedException e) {
            ui.displayError("Error during game: " + e.getMessage());
            cleanup();
        }
    }

    private void cleanup() {
        if (chatManager != null) {
            chatManager.stop();
            chatManager = null;
        }
    }

    public void exit() throws RemoteException {
        server.unregisterPlayer(currentPlayer);
        cleanup();
    }
} 