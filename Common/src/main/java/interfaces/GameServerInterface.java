package interfaces;

import models.*;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.UUID;

public interface GameServerInterface extends Remote {

    void registerPlayer(Player player) throws RemoteException;

    void unregisterPlayer(Player player) throws RemoteException;

    UUID createRoom(String roomName, Player ownerPlayer) throws RemoteException;

    void joinRoom(UUID gameRoomID, Player player) throws RemoteException;

    void startGame(UUID gameRoomID) throws RemoteException;

    void makeMove(UUID gameRoomID, Move move) throws RemoteException;

    GameState getGameState(UUID gameID) throws RemoteException;

    ArrayList<GameRoom> getActiveRooms() throws RemoteException;

    GameRoom getRoomInfo(UUID roomID) throws RemoteException;

    String getOpponentIP(UUID roomId, Player player) throws RemoteException;

    int getPort(UUID gameRoomID, Player player) throws RemoteException;

    void setOwnerPort(UUID gameRoomID, Player player, int port) throws RemoteException;

    Player getUpdatedPlayer(UUID playerId) throws RemoteException;
}
