package models;

import java.io.Serializable;
import java.util.UUID;

public class Move implements Serializable {
    private final int row;
    private final int column;
    private final int moveID;
    private final UUID gameID;


    public Move(int row, int column, int moveID, UUID gameID) {
        this.row = row;
        this.column = column;
        this.moveID = moveID;
        this.gameID = gameID;
    }


    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public int getMoveID() {
        return moveID;
    }

    public UUID getGameID() {
        return gameID;
    }
}

