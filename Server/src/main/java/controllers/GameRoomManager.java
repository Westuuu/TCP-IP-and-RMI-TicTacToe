package controllers;

import models.GameRoom;
import models.GameState;
import models.Player;

import java.util.logging.Logger;

public class GameRoomManager {
    private static final Logger LOGGER = Logger.getLogger(GameRoomManager.class.getName());


    public boolean joinRoom(GameRoom gameRoom, Player player) {
        if (player == null) {
            LOGGER.warning("Player is null!");
            return false;
        }

        if (player.equals(gameRoom.getPlayers()[0]) || player.equals(gameRoom.getPlayers()[1])) {
            LOGGER.warning("Player " + player.getName() + " is already in the room!");
            return false;
        }

        if (gameRoom.getPlayers()[0] == null) {
            gameRoom.getPlayers()[0] = player;
        } else if (gameRoom.getPlayers()[1] == null) {
            gameRoom.getPlayers()[1] = player;
        } else {
            LOGGER.warning("Room is already full!");
            return false;
        }

        LOGGER.info("Player " + player.getName() + " joined the room!");
        return true;
    }

    public boolean leaveRoom(GameRoom gameRoom, Player player) {
        if (player == null) {
            LOGGER.warning("Player is null!");
            return false;
        }

        if (gameRoom.getPlayers()[0] == player) {
            gameRoom.getPlayers()[0] = null;
        } else if (gameRoom.getPlayers()[1] == player) {
            gameRoom.getPlayers()[1] = null;
        } else {
            LOGGER.warning("Player " + player.getName() + " is not in the room!");
            return false;
        }

        LOGGER.info("Player " + player.getName() + " left the room!");
        return true;
    }

    public boolean startGame(GameRoom gameRoom) {
        Player[] players = gameRoom.getPlayers();

        if (players[0] == null || players[1] == null) {
            LOGGER.warning("Room is not full!");
            return false;
        }

        GameState newGameState = new GameState(players);
        gameRoom.setGameState(newGameState);
        return true;
    }
}
