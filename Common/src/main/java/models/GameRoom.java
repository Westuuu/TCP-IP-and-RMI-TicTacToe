package models;

import java.io.Serializable;
import java.util.UUID;

public class GameRoom implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum RoomStatus {
        WAITING,
        PLAYING,
        FINISHED
    }

    private final UUID gameRoomID;
    private final String gameRoomName;
    private final Player owner;
    private Player playerX;
    private Player playerO;
    private RoomStatus roomStatus;
    private GameState gameState;

    public GameRoom(String gameRoomName, Player owner) {
        this.gameRoomID = UUID.randomUUID();
        this.gameRoomName = gameRoomName;
        this.owner = owner;
        this.playerX = owner;
        this.roomStatus = RoomStatus.WAITING;
        this.gameState = new GameState(this.gameRoomID);
        this.gameState.setPlayerX(owner);
    }

    public UUID getGameRoomID() {
        return gameRoomID;
    }

    public String getGameRoomName() {
        return gameRoomName;
    }

    public Player getOwner() {
        return owner;
    }

    public RoomStatus getRoomStatus() {
        return roomStatus;
    }

    public void setRoomStatus(RoomStatus status) {
        this.roomStatus = status;
    }

    public GameState getGameState() {
        return gameState;
    }

    public boolean isRoomFull() {
        return playerX != null && playerO != null;
    }

    public void addPlayer(Player player) {
        if (playerO == null && !player.equals(playerX)) {
            playerO = player;
            gameState.setPlayerO(player);
        }
    }

    public void removePlayer(Player player) {
        if (player.equals(playerX)) {
            playerX = null;
            gameState.setPlayerX(null);
        } else if (player.equals(playerO)) {
            playerO = null;
            gameState.setPlayerO(null);
        }
    }

    public void startGame() {
        if (isRoomFull()) {
            roomStatus = RoomStatus.PLAYING;
            gameState.startGame();
            gameState.setCurrentPlayerTurn(playerX);
        }
    }
}
