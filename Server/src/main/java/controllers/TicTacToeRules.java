package controllers;

import models.GameState;
import models.Move;
import models.Player;

public class TicTacToeRules {
    public static final int BOARD_SIZE = 3;
    private static final int WINNING_LENGTH = 3;

    public static boolean isValidMove(GameState gameState, Move move) {
        if (!isPlayerTurn(gameState, move.getPlayer())) {
            return false;
        }

        if (!isWithinBounds(move.getRow(), move.getColumn())) {
            return false;
        }

        return isCellEmpty(gameState, move.getRow(), move.getColumn());
    }


    public static boolean isPlayerTurn(GameState gameState, Player player) {
        if (gameState.getMoves().isEmpty()) {
            return player.equals(gameState.getPlayerX());
        }

        Move lastMove = gameState.getMoves().get(gameState.getMoves().size() - 1);
        return !lastMove.getPlayer().equals(player);
    }


    private static boolean isWithinBounds(int row, int column) {
        return row >= 0 && row < BOARD_SIZE && column >= 0 && column < BOARD_SIZE;
    }


    private static boolean isCellEmpty(GameState gameState, int row, int column) {
        return gameState.getMoves().stream()
                .noneMatch(move -> move.getRow() == row && move.getColumn() == column);
    }


    public static Player checkWinner(GameState gameState) {
        if (gameState.getMoves().size() < WINNING_LENGTH) {
            return null;
        }

        Move lastMove = gameState.getMoves().get(gameState.getMoves().size() - 1);
        Player lastPlayer = lastMove.getPlayer();

        if (checkRow(gameState, lastMove.getRow(), lastPlayer) ||
                checkColumn(gameState, lastMove.getColumn(), lastPlayer) ||
                checkDiagonal(gameState, lastPlayer) ||
                checkAntiDiagonal(gameState, lastPlayer)) {
            return lastPlayer;
        }

        return null;
    }


    public static boolean isDraw(GameState gameState) {
        return gameState.getMoves().size() == BOARD_SIZE * BOARD_SIZE && checkWinner(gameState) == null;
    }


    private static boolean checkRow(GameState gameState, int row, Player player) {
        return gameState.getMoves().stream()
                .filter(move -> move.getRow() == row && move.getPlayer().equals(player))
                .count() == BOARD_SIZE;
    }


    private static boolean checkColumn(GameState gameState, int column, Player player) {
        return gameState.getMoves().stream()
                .filter(move -> move.getColumn() == column && move.getPlayer().equals(player))
                .count() == BOARD_SIZE;
    }


    private static boolean checkDiagonal(GameState gameState, Player player) {
        return gameState.getMoves().stream()
                .filter(move -> move.getRow() == move.getColumn() && move.getPlayer().equals(player))
                .count() == BOARD_SIZE;
    }


    private static boolean checkAntiDiagonal(GameState gameState, Player player) {
        return gameState.getMoves().stream()
                .filter(move -> move.getRow() + move.getColumn() == BOARD_SIZE - 1 && move.getPlayer().equals(player))
                .count() == BOARD_SIZE;
    }
}
