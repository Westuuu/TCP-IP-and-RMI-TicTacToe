package models;

import java.io.Serializable;

public class PlayerStats implements Serializable {
    private static final long serialVersionUID = 1L;
    private int wins;
    private int losses;
    private int draws;

    public PlayerStats() {
        this.wins = 0;
        this.losses = 0;
        this.draws = 0;
    }

    public void incrementWins() {
        wins++;
    }

    public void incrementLosses() {
        losses++;
    }

    public void incrementDraws() {
        draws++;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public int getDraws() {
        return draws;
    }

    @Override
    public String toString() {
        return String.format("Wins: %d, Losses: %d, Draws: %d", wins, losses, draws);
    }
} 