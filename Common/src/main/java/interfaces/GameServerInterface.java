package interfaces;

import models.GameRoom;
import models.GameState;
import models.Move;
import models.Player;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GameServerInterface extends Remote {

    void registerPlayer(Player player) throws RemoteException;

    boolean createRoom() throws RemoteException;

    boolean joinRoom(GameRoom room) throws RemoteException;

    boolean leaveRoom(GameRoom room) throws RemoteException;

    boolean startGame() throws RemoteException;

    boolean makeMove(Move move) throws RemoteException;

    GameState getGameState() throws RemoteException;

}
