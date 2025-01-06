package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;

public class GameRoom implements Serializable {
    private final static Logger LOGGER = Logger.getLogger(GameRoom.class.getName());

    private final UUID gameRoomID;
    private String gameRoomName;
    private GameState gameState;
    private Player[] players;
    private RoomStatus roomStatus;
    private Player ownerPlayer;
    private ArrayList<GameState> gamesPlayed = new ArrayList<>();

    public enum RoomStatus {
        OPEN,
        CLOSED
    }

    public GameRoom(String gameRoomName, Player ownerPlayer) {
        this.gameRoomID = UUID.randomUUID();
        this.gameRoomName = gameRoomName;
        this.players = new Player[2];
        this.ownerPlayer = ownerPlayer;
    }

    public UUID getGameRoomID() {
        return gameRoomID;
    }

    public String getGameRoomName() {
        return gameRoomName;
    }

    public GameState getGameState() {
        return gameState;
    }

    public Player[] getPlayers() {
        return players;
    }

    public ArrayList<GameState> getGamesPlayed() {
        return gamesPlayed;
    }

    public RoomStatus getRoomStatus() {
        return roomStatus;
    }

    public void setRoomStatus(RoomStatus roomStatus) {
        this.roomStatus = roomStatus;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public void setPlayers(Player[] players) {
        this.players = players;
    }

    public void setGameRoomName(String gameRoomName) {
        this.gameRoomName = gameRoomName;
    }

    public Player getOwnerPlayer() {
        return ownerPlayer;
    }
}
