package interfaces;

import models.GameRoom;
import models.GameState;
import models.Move;
import models.Player;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.UUID;

public interface GameServerInterface extends Remote {

    void registerPlayer(Player player) throws RemoteException;

    void unregisterPlayer(Player player) throws RemoteException;

    void createRoom(String roomName, Player ownerPlayer) throws RemoteException;

    void removeRoom(UUID gameRoomID) throws RemoteException;

    void joinRoom(UUID gameRoomID, Player player) throws RemoteException;

    void leaveRoom(UUID gameRoomID, Player player) throws RemoteException;

    void startGame(UUID gameRoomID) throws RemoteException;

    void makeMove(Move move) throws RemoteException;

    GameState getGameState() throws RemoteException;

    ArrayList<GameRoom> getActiveRooms() throws RemoteException;

    GameRoom getRoomInfo(UUID roomID) throws RemoteException;

}
