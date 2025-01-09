package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final UUID gameID;
    private final List<Move> moves;
    private final char[][] board;
    private Player playerX;
    private Player playerO;
    private Player currentPlayerTurn;
    private Player winner;
    private boolean gameStarted;
    private boolean finished;

    public GameState(UUID gameID) {
        this.gameID = gameID;
        this.moves = new ArrayList<>();
        this.board = new char[3][3];
        this.gameStarted = false;
        this.finished = false;
        initializeBoard();
    }

    private void initializeBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = ' ';
            }
        }
    }

    public UUID getGameID() {
        return gameID;
    }

    public List<Move> getMoves() {
        return moves;
    }

    public char[][] getBoard() {
        return board;
    }

    public Player getPlayerX() {
        return playerX;
    }

    public void setPlayerX(Player player) {
        this.playerX = player;
    }

    public Player getPlayerO() {
        return playerO;
    }

    public void setPlayerO(Player player) {
        this.playerO = player;
    }

    public Player getCurrentPlayerTurn() {
        return currentPlayerTurn;
    }

    public void setCurrentPlayerTurn(Player player) {
        this.currentPlayerTurn = player;
    }

    public Player getWinner() {
        return winner;
    }

    public void setWinner(Player player) {
        this.winner = player;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void startGame() {
        this.gameStarted = true;
    }

    public boolean isFinished() {
        return finished;
    }

    public void finishGame() {
        this.finished = true;
    }

    public void addMove(Move move) {
        moves.add(move);
        int row = move.getRow();
        int col = move.getColumn();
        board[row][col] = move.getPlayer().equals(playerX) ? 'X' : 'O';
    }
}
