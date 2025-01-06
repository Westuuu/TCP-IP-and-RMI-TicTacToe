package models;

import java.io.Serializable;
import java.util.Arrays;
import java.util.UUID;

public class GameState implements Serializable {
    private char[][] board;
    private final UUID gameID;
    private Player[] players;
    private Player currentPlayerTurn;
    private boolean isFinished;
    private Player winner;


    public GameState(Player[] players) {
        initializeBoard();
        this.gameID = UUID.randomUUID();
        this.players = players;
        this.isFinished = false;
        this.winner = null;
        this.currentPlayerTurn = null;
    }

    private void initializeBoard() {
        this.board = new char[3][3];
        for (char[] chars : this.board) {
            Arrays.fill(chars, ' ');
        }
    }

    public void finishGame() {
        this.isFinished = true;
    }

    public void setWinner(Player winner) {
        this.winner = winner;
    }

    public void setCurrentPlayerTurn(Player currentPlayerTurn) {
        this.currentPlayerTurn = currentPlayerTurn;
    }

    public Player getPlayerTurn() {
        return this.currentPlayerTurn;
    }

    public char[][] getBoard() {
        return this.board;
    }

    public UUID getGameID() {
        return this.gameID;
    }

    public Player getWinner() {
        return this.winner;
    }

    public Player getCurrentPlayerTurn() {
        return this.currentPlayerTurn;
    }

    public boolean isFinished() {
        return this.isFinished;
    }

}
