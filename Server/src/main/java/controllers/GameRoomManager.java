package controllers;

import models.GameRoom;
import models.GameState;
import models.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class GameRoomManager {
    private static final Logger LOGGER = Logger.getLogger(GameRoomManager.class.getName());

    private Map<UUID, GameRoom> activeGameRooms;
    private Map<GameRoom, ArrayList<Player>> playerGameRooms;

    public GameRoomManager() {
        this.activeGameRooms = new HashMap<>();
        this.playerGameRooms = new HashMap<>();
    }

    public void createGameRoom(String roomName, Player ownerPlayer) {
        GameRoom gameRoom = new GameRoom(roomName, ownerPlayer);
        activeGameRooms.put(gameRoom.getGameRoomID(), gameRoom);
    }

    public boolean deleteGameRoom(UUID gameRoomID) {
        return activeGameRooms.remove(gameRoomID) != null;
    }

    public boolean joinRoom(UUID gameRoomID, Player player) {
        if (player == null) {
            LOGGER.warning("Player is null!");
            return false;
        }

        if (player.equals(activeGameRooms.get(gameRoomID).getPlayers()[0]) || player.equals(activeGameRooms.get(gameRoomID).getPlayers()[1])) {
            LOGGER.warning("Player " + player.getName() + " is already in the room!");
            return false;
        }

        if (activeGameRooms.get(gameRoomID).getPlayers()[0] == null) {
            activeGameRooms.get(gameRoomID).getPlayers()[0] = player;
        } else if (activeGameRooms.get(gameRoomID).getPlayers()[1] == null) {
            activeGameRooms.get(gameRoomID).getPlayers()[1] = player;
        } else {
            LOGGER.warning("Room is already full!");
            return false;
        }

        LOGGER.info("Player " + player.getName() + " joined the room!");
        return true;
    }

    public boolean leaveRoom(UUID gameRoomID, Player player) {
        if (player == null) {
            LOGGER.warning("Player is null!");
            return false;
        }

        if (activeGameRooms.get(gameRoomID).getPlayers()[0] == player) {
            activeGameRooms.get(gameRoomID).getPlayers()[0] = null;
        } else if (activeGameRooms.get(gameRoomID).getPlayers()[1] == player) {
            activeGameRooms.get(gameRoomID).getPlayers()[1] = null;
        } else {
            LOGGER.warning("Player " + player.getName() + " is not in the room!");
            return false;
        }

        LOGGER.info("Player " + player.getName() + " left the room!");
        return true;
    }

    public boolean startGame(UUID gameRoomID) {
        Player[] players = activeGameRooms.get(gameRoomID).getPlayers();

        if (players[0] == null || players[1] == null) {
            LOGGER.warning("Room is not full!");
            return false;
        }

        GameState newGameState = new GameState(players);
        activeGameRooms.get(gameRoomID).setGameState(newGameState);
        return true;
    }

    public Map<UUID, GameRoom> getActiveGameRooms() {
        return activeGameRooms;
    }

    public void removeGameRoom(UUID gameRoomID) {
        activeGameRooms.remove(gameRoomID);
    }

    public Map<GameRoom, ArrayList<Player>> getPlayerGameRooms() {
        return playerGameRooms;
    }

    public void removePlayerFromGameRoom(UUID gameRoomID, Player player) {
        ArrayList<Player> playersInRoom = playerGameRooms.get(activeGameRooms.get(gameRoomID));
        playersInRoom.remove(player);
        playerGameRooms.put(activeGameRooms.get(gameRoomID), playersInRoom);
    }
}
