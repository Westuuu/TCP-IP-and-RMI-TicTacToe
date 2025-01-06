package controllers;

import interfaces.GameServerInterface;
import models.GameRoom;
import models.GameState;
import models.Move;
import models.Player;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class GameServerImpl extends UnicastRemoteObject implements GameServerInterface {
    private static final Logger LOGGER = Logger.getLogger(GameServerImpl.class.getName());

    private final Map<UUID, Player> registeredPlayers;
    private final GameRoomManager roomManager;

    public GameServerImpl() throws RemoteException {
        this.registeredPlayers = new ConcurrentHashMap<>();
        this.roomManager = new GameRoomManager();
    }

    @Override
    public void registerPlayer(Player player) throws RemoteException {
        if (player == null || player.getPlayerID() == null) {
            throw new RemoteException("Invalid player data");
        }
        LOGGER.info("Registering player " + player.getPlayerID());
        registeredPlayers.put(player.getPlayerID(), player);
    }

    @Override
    public void unregisterPlayer(Player player) throws RemoteException {
        if (player == null || player.getPlayerID() == null) {
            throw new RemoteException("Invalid player data");
        }
        LOGGER.info("Unregistering player " + player.getPlayerID());
        registeredPlayers.remove(player.getPlayerID());
    }


    @Override
    public void createRoom(String roomName, Player ownerPlayer) throws RemoteException {
        if (roomName == null || roomName.trim().isEmpty()) {
            throw new RemoteException("Room name cannot be empty");
        }
        if (!registeredPlayers.containsKey(ownerPlayer.getPlayerID())) {
            throw new RemoteException("Player not registered");
        }
        if (ownerPlayer.getOwnedGameRoom() != null) {
            throw new RemoteException("Player already has a game room created");
        }
        LOGGER.info("Creating room " + roomName + " for player " + ownerPlayer.getPlayerID());
        roomManager.createGameRoom(roomName, ownerPlayer);
    }

    @Override
    public void removeRoom(UUID gameRoomID, Player player) throws RemoteException {
        if (gameRoomID == null) {
            throw new RemoteException("Invalid room ID");
        }
        if (player.getOwnedGameRoom() != gameRoomID) {
            throw new RemoteException("Player " + player.getPlayerID() + " is not the owner of the room " + gameRoomID);
        }
        LOGGER.info("Removing room: " + gameRoomID);
        roomManager.removeGameRoom(gameRoomID);
    }

    @Override
    public void joinRoom(UUID gameRoomID, Player player) throws RemoteException {
        if (!registeredPlayers.containsKey(player.getPlayerID())) {
            throw new RemoteException("Player not registered");
        }
        GameRoom room = roomManager.getActiveGameRooms().get(gameRoomID);
        if (room == null) {
            throw new RemoteException("Room not found");
        }
        if (room.getRoomStatus() == GameRoom.RoomStatus.CLOSED) {
            throw new RemoteException("Game already in progress");
        }

        LOGGER.info("Player " + player.getPlayerID() + " has joined room: " + gameRoomID);
        roomManager.joinRoom(gameRoomID, player);
    }

    @Override
    public void leaveRoom(UUID gameRoomID, Player player) throws RemoteException {
        if (gameRoomID == null || player == null) {
            throw new RemoteException("Invalid parameters");
        }
        LOGGER.info("Player " + player.getPlayerID() + " has left room: " + gameRoomID);
        roomManager.leaveRoom(gameRoomID, player);
    }

    @Override
    public void startGame(UUID gameRoomID) throws RemoteException {
        GameRoom room = roomManager.getActiveGameRooms().get(gameRoomID);
        if (room == null) {
            throw new RemoteException("Room not found");
        }
        if (!room.isRoomFull()) {
            throw new RemoteException("Need exactly 2 players to start the game");
        }
        LOGGER.info("Starting game in room: " + gameRoomID);
        roomManager.startGame(gameRoomID);
    }

    @Override
    public void makeMove(UUID gameRoomID, Move move) throws RemoteException {
        GameRoom room = roomManager.getActiveGameRooms().get(gameRoomID);
        if (room == null) {
            throw new RemoteException("Room not found");
        }
        if (!room.getGameState().isGameStarted()) {
            throw new RemoteException("Game has not started yet");
        }
        if (!TicTacToeRules.isValidMove(room.getGameState(), move)) {
            throw new RemoteException("Invalid move");
        }
        LOGGER.info("Making move in room " + gameRoomID + ": " + move);
        room.getGameState().addMove(move);

        Player winner = TicTacToeRules.checkWinner(room.getGameState());
        if (winner != null) {
            handleGameEnd(room, winner);
        } else if (TicTacToeRules.isDraw(room.getGameState())) {
            handleGameDraw(room);
        }
    }

    private void handleGameEnd(GameRoom room, Player winner) {
        Player loser = room.getPlayers()[0].equals(winner) ? room.getPlayers()[1] : room.getPlayers()[0];
        
        winner.updatePlayerStats("win");
        loser.updatePlayerStats("loss");
        
        LOGGER.info(String.format("Game ended in room %s. Winner: %s, Loser: %s", 
            room.getGameRoomID(), winner.getName(), loser.getName()));
        LOGGER.info(String.format("Updated stats - Winner %s: %s, Loser %s: %s", 
            winner.getName(), winner.getPlayerStats(), 
            loser.getName(), loser.getPlayerStats()));
    }

    private void handleGameDraw(GameRoom room) {
        Player[] players = room.getPlayers();
        players[0].updatePlayerStats("draw");
        players[1].updatePlayerStats("draw");
        
        LOGGER.info(String.format("Game ended in draw in room %s. Players: %s vs %s", 
            room.getGameRoomID(), players[0].getName(), players[1].getName()));
        LOGGER.info(String.format("Updated stats - Player %s: %s, Player %s: %s", 
            players[0].getName(), players[0].getPlayerStats(), 
            players[1].getName(), players[1].getPlayerStats()));
    }

    @Override
    public GameState getGameState(UUID gameRoomID) throws RemoteException {
        GameRoom room = roomManager.getActiveGameRooms().get(gameRoomID);
        if (room == null) {
            throw new RemoteException("Room not found");
        }
        return room.getGameState();
    }

    @Override
    public ArrayList<GameRoom> getActiveRooms() throws RemoteException {
        return new ArrayList<>(roomManager.getActiveGameRooms().values());
    }

    @Override
    public GameRoom getRoomInfo(UUID gameRoomID) throws RemoteException {
        GameRoom room = roomManager.getActiveGameRooms().get(gameRoomID);
        if (room == null) {
            throw new RemoteException("Room not found");
        }
        return room;
    }

    public GameRoomManager getRoomManager() {
        return roomManager;
    }
}
