package controllers;

import interfaces.GameServerInterface;
import models.*;

import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class GameServerImpl extends UnicastRemoteObject implements GameServerInterface {
    private static final Logger LOGGER = Logger.getLogger(GameServerImpl.class.getName());
    private final Map<UUID, GameRoom> activeRooms;
    private final Map<UUID, Player> activePlayers;
    private final GameRoomManager roomManager;

    public GameServerImpl() throws RemoteException {
        super();
        this.activeRooms = new ConcurrentHashMap<>();
        this.activePlayers = new ConcurrentHashMap<>();
        this.roomManager = new GameRoomManager();
    }

    @Override
    public void registerPlayer(Player player) throws RemoteException {
        try {
            String clientIP = RemoteServer.getClientHost();
            LOGGER.info("Raw client IP from RMI: " + clientIP);
            
            // For localhost testing, normalize the IP
            if (clientIP.startsWith("127.") || clientIP.equals("0:0:0:0:0:0:0:1") || 
                clientIP.equals("localhost") || clientIP.equals("::1")) {
                clientIP = "localhost";
                LOGGER.info("Normalized localhost IP to: " + clientIP);
            }
            
            player.setIpAddress(clientIP);
            activePlayers.put(player.getPlayerId(), player);
            LOGGER.info("Player registered: " + player.getName() + " from IP: " + clientIP);
        } catch (ServerNotActiveException e) {
            // If we can't get the client host, default to localhost for testing
            LOGGER.warning("Could not get client IP, defaulting to localhost: " + e.getMessage());
            player.setIpAddress("localhost");
            activePlayers.put(player.getPlayerId(), player);
        }
    }

    @Override
    public void unregisterPlayer(Player player) throws RemoteException {
        activePlayers.remove(player.getPlayerId());
        LOGGER.info("Player unregistered: " + player.getName());
    }

    @Override
    public UUID createRoom(String roomName, Player ownerPlayer) throws RemoteException {
        // Get the stored player instance from activePlayers
        Player storedPlayer = activePlayers.get(ownerPlayer.getPlayerId());
        if (storedPlayer == null) {
            throw new RemoteException("Player not registered");
        }
        
        GameRoom room = roomManager.createRoom(roomName, storedPlayer);
        activeRooms.put(room.getGameRoomID(), room);
        return room.getGameRoomID();
    }

    @Override
    public void joinRoom(UUID gameRoomID, Player player) throws RemoteException {
        GameRoom room = activeRooms.get(gameRoomID);
        if (room == null) {
            throw new RemoteException("Room not found");
        }
        
        Player storedPlayer = activePlayers.get(player.getPlayerId());
        if (storedPlayer == null) {
            throw new RemoteException("Player not registered");
        }
        
        try {
            String clientIP = RemoteServer.getClientHost();
            if (clientIP.startsWith("127.") || clientIP.equals("0:0:0:0:0:0:0:1") || 
                clientIP.equals("localhost") || clientIP.equals("::1")) {
                clientIP = "localhost";
            }
            storedPlayer.setIpAddress(clientIP);
            LOGGER.info("Updated player IP before joining room: " + storedPlayer.getName() + " IP: " + clientIP);
        } catch (ServerNotActiveException e) {
            LOGGER.warning("Could not update player IP, using localhost");
            storedPlayer.setIpAddress("localhost");
        }
        
        roomManager.joinRoom(room, storedPlayer);
    }

    @Override
    public void startGame(UUID gameRoomID) throws RemoteException {
        GameRoom room = activeRooms.get(gameRoomID);
        if (room == null) {
            throw new RemoteException("Room not found");
        }
        
        Player playerX = room.getGameState().getPlayerX();
        Player playerO = room.getGameState().getPlayerO();
        
        if (playerX != null && playerX.getIpAddress() == null) {
            playerX.setIpAddress("localhost");
            LOGGER.info("Set missing IP for player X: " + playerX.getName());
        }
        if (playerO != null && playerO.getIpAddress() == null) {
            playerO.setIpAddress("localhost");
            LOGGER.info("Set missing IP for player O: " + playerO.getName());
        }
        
        roomManager.startGame(room);
    }

    @Override
    public void makeMove(UUID gameRoomID, Move move) throws RemoteException {
        GameRoom room = activeRooms.get(gameRoomID);
        if (room == null) {
            throw new RemoteException("Room not found");
        }
        
        Player storedPlayer = activePlayers.get(move.getPlayer().getPlayerId());
        if (storedPlayer == null) {
            throw new RemoteException("Player not registered");
        }
        
        Move updatedMove = new Move(
            move.getRow(),
            move.getColumn(),
            move.getMoveID(),
            move.getGameID(),
            storedPlayer
        );
        
        roomManager.makeMove(room, updatedMove);
    }

    @Override
    public GameState getGameState(UUID gameRoomID) throws RemoteException {
        GameRoom room = activeRooms.get(gameRoomID);
        if (room == null) {
            throw new RemoteException("Room not found");
        }
        return room.getGameState();
    }

    @Override
    public ArrayList<GameRoom> getActiveRooms() throws RemoteException {
        return new ArrayList<>(activeRooms.values());
    }

    @Override
    public GameRoom getRoomInfo(UUID roomID) throws RemoteException {
        return activeRooms.get(roomID);
    }

    @Override
    public String getOpponentIP(UUID roomId, Player player) throws RemoteException {
        GameRoom room = activeRooms.get(roomId);
        if (room == null) {
            throw new RemoteException("Room not found");
        }

        Player playerX = room.getGameState().getPlayerX();
        Player playerO = room.getGameState().getPlayerO();
        
        if (playerX == null || playerO == null) {
            throw new RemoteException("Both players must be connected");
        }

        String ipX = playerX.getIpAddress();
        String ipO = playerO.getIpAddress();
        
        LOGGER.info("Player X (" + playerX.getName() + ") IP: " + ipX);
        LOGGER.info("Player O (" + playerO.getName() + ") IP: " + ipO);
        
        if (ipX == null) {
            ipX = "localhost";
            playerX.setIpAddress(ipX);
            LOGGER.info("Set missing IP for player X to localhost");
        }
        if (ipO == null) {
            ipO = "localhost";
            playerO.setIpAddress(ipO);
            LOGGER.info("Set missing IP for player O to localhost");
        }

        if (ipX.startsWith("127.") || ipX.equals("0:0:0:0:0:0:0:1") ||
            ipX.equals("localhost") || ipX.equals("::1") ||
            ipO.startsWith("127.") || ipO.equals("0:0:0:0:0:0:0:1") || 
            ipO.equals("localhost") || ipO.equals("::1")) {
            LOGGER.info("Local game detected, using localhost");
            return "localhost";
        }

        if (player.equals(playerX)) {
            return ipO;
        } else if (player.equals(playerO)) {
            return ipX;
        } else {
            throw new RemoteException("Player not in this game");
        }
    }

    @Override
    public int getPort(UUID gameRoomID, Player player) throws RemoteException {
        GameRoom room = activeRooms.get(gameRoomID);
        if (room == null) {
            throw new RemoteException("Room not found");
        }
        Player playerX = room.getGameState().getPlayerX();
        int port = playerX.getCHAT_PORT();
        LOGGER.info("Returning chat port: " + port + " for room: " + gameRoomID);
        return port;
    }

    @Override
    public void setOwnerPort(UUID roomId, Player owner, int port) throws RemoteException {
        GameRoom room = activeRooms.get(roomId);
        if (room == null) {
            throw new RemoteException("Room not found");
        }
        if (!room.getOwner().equals(owner)) {
            throw new RemoteException("Only room owner can set port");
        }
        
        Player serverOwner = room.getGameState().getPlayerX();
        serverOwner.setCHAT_PORT(port);
        LOGGER.info("Set chat port: " + port + " for room: " + roomId + " owner: " + serverOwner.getName());
    }

    @Override
    public Player getUpdatedPlayer(UUID playerId) throws RemoteException {
        Player player = activePlayers.get(playerId);
        if (player == null) {
            throw new RemoteException("Player not found");
        }
        return player;
    }
}
