package controllers;

import interfaces.GameServerInterface;
import models.GameRoom;
import models.GameState;
import models.Move;
import models.Player;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameServerImpl extends UnicastRemoteObject implements GameServerInterface {
    Map<UUID, Player> registeredPlayers = new HashMap<>();
    GameRoomManager roomManager;

    public GameServerImpl() throws RemoteException {
        roomManager = new GameRoomManager();
    }

    @Override
    public void registerPlayer(Player player) throws RemoteException {
        registeredPlayers.put(player.getPlayerID(), player);
    }

    @Override
    public void unregisterPlayer(Player player) throws RemoteException {
        registeredPlayers.remove(player.getPlayerID());
    }


    @Override
    public void createRoom(String roomName, Player ownerPlayer) throws RemoteException {
        roomManager.createGameRoom(roomName, ownerPlayer);
    }

    @Override
    public void removeRoom(UUID gameRoomID) throws RemoteException {
        roomManager.removeGameRoom(gameRoomID);
    }

    @Override
    public void joinRoom(UUID gameRoomID, Player player) throws RemoteException {
        roomManager.joinRoom(gameRoomID, player);
    }

    @Override
    public void leaveRoom(UUID gameRoomID, Player player) throws RemoteException {
        roomManager.leaveRoom(gameRoomID, player);
    }

    @Override
    public void startGame(UUID room) throws RemoteException {
        roomManager.startGame(room);
    }

    @Override
    public void makeMove(Move move) throws RemoteException {

    }

    @Override
    public GameState getGameState() throws RemoteException {
        return null;
    }

    @Override
    public ArrayList<GameRoom> getActiveRooms() throws RemoteException {
        return null;
    }

    @Override
    public GameRoom getRoomInfo(UUID roomID) throws RemoteException {
        return null;
    }
}
