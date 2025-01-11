package models;

import java.io.Serializable;
import java.util.UUID;

public class Player implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String name;
    private final UUID playerId;
    private final PlayerStats playerStats;
    private String ipAddress;
    private int CHAT_PORT;

    public Player(String name) {
        this.name = name;
        this.playerId = UUID.randomUUID();
        this.playerStats = new PlayerStats();
    }

    public String getName() {
        return name;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public PlayerStats getPlayerStats() {
        return playerStats;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getCHAT_PORT() {
        return CHAT_PORT;
    }

    public void setCHAT_PORT(int CHAT_PORT) {
        this.CHAT_PORT = CHAT_PORT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return playerId.equals(player.playerId);
    }

    @Override
    public int hashCode() {
        return playerId.hashCode();
    }
}
