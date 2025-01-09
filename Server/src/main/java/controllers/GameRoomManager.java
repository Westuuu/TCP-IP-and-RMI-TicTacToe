package controllers;

import models.GameRoom;
import models.GameState;
import models.Move;
import models.Player;

import java.rmi.RemoteException;
import java.util.logging.Logger;

public class GameRoomManager {
    private static final Logger LOGGER = Logger.getLogger(GameRoomManager.class.getName());

    public GameRoom createRoom(String roomName, Player owner) throws RemoteException {
        GameRoom room = new GameRoom(roomName, owner);
        LOGGER.info("Created room: " + roomName + " with owner: " + owner.getName());
        return room;
    }

    public void removeRoom(GameRoom room, Player player) throws RemoteException {
        if (!room.getOwner().equals(player)) {
            throw new RemoteException("Only room owner can remove the room");
        }
        LOGGER.info("Removed room: " + room.getGameRoomName());
    }

    public void joinRoom(GameRoom room, Player player) throws RemoteException {
        if (room.isRoomFull()) {
            throw new RemoteException("Room is full");
        }
        if (room.getRoomStatus() != GameRoom.RoomStatus.WAITING) {
            throw new RemoteException("Game already in progress");
        }
        room.addPlayer(player);
        LOGGER.info("Player " + player.getName() + " joined room: " + room.getGameRoomName());
    }

    public void leaveRoom(GameRoom room, Player player) throws RemoteException {
        room.removePlayer(player);
        LOGGER.info("Player " + player.getName() + " left room: " + room.getGameRoomName());
    }

    public void startGame(GameRoom room) throws RemoteException {
        if (!room.isRoomFull()) {
            throw new RemoteException("Need exactly 2 players to start the game");
        }
        room.startGame();
        LOGGER.info("Started game in room: " + room.getGameRoomName());
    }

    public void makeMove(GameRoom room, Move move) throws RemoteException {
        GameState state = room.getGameState();
        if (!state.isGameStarted()) {
            throw new RemoteException("Game has not started yet");
        }
        if (!TicTacToeRules.isValidMove(state, move)) {
            throw new RemoteException("Invalid move");
        }

        state.addMove(move);
        LOGGER.info("Move made in room " + room.getGameRoomName() + ": " + move);

        Player nextPlayer = move.getPlayer().equals(state.getPlayerX()) ?
            state.getPlayerO() : state.getPlayerX();
        state.setCurrentPlayerTurn(nextPlayer);

        Player winner = TicTacToeRules.checkWinner(state);
        if (winner != null) {
            handleGameEnd(room, winner);
        } else if (TicTacToeRules.isDraw(state)) {
            handleGameDraw(room);
        }
    }

    private void handleGameEnd(GameRoom room, Player winner) {
        GameState state = room.getGameState();
        Player loser = winner.equals(state.getPlayerX()) ? state.getPlayerO() : state.getPlayerX();
        
        winner.getPlayerStats().incrementWins();
        loser.getPlayerStats().incrementLosses();
        
        state.setWinner(winner);
        state.finishGame();
        room.setRoomStatus(GameRoom.RoomStatus.FINISHED);
        
        LOGGER.info(String.format("Game ended in room %s. Winner: %s, Loser: %s", 
            room.getGameRoomName(), winner.getName(), loser.getName()));
    }

    private void handleGameDraw(GameRoom room) {
        GameState state = room.getGameState();
        state.getPlayerX().getPlayerStats().incrementDraws();
        state.getPlayerO().getPlayerStats().incrementDraws();
        
        state.finishGame();
        room.setRoomStatus(GameRoom.RoomStatus.FINISHED);
        
        LOGGER.info(String.format("Game ended in draw in room %s", room.getGameRoomName()));
    }
}
