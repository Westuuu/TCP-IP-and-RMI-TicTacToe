package models;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Player {
    private final UUID playerID;
    private final String name;
    private InetSocketAddress socketAddress; // for tcp/ip
    private Map<String, Integer> playerStats = new HashMap<>(); // win-loss-draw

    public Player(String name) {
        this.playerID = UUID.randomUUID();
        initializePlayerStats();
        this.name = name;
    }

    private void initializePlayerStats() {
        playerStats.put("win", 0);
        playerStats.put("loss", 0);
        playerStats.put("draw", 0);
    }

    public boolean updatePlayerStats(String gameResultKey) {
        return switch (gameResultKey) {
            case "win" -> {
                playerStats.put("win", playerStats.get("win") + 1);
                yield true;
            }
            case "loss" -> {
                playerStats.put("loss", playerStats.get("loss") + 1);
                yield true;
            }
            case "draw" -> {
                playerStats.put("draw", playerStats.get("draw") + 1);
                yield true;
            }
            default -> false;
        };
    }

    public UUID getPlayerID() {
        return playerID;
    }

    public Map<String, Integer> getPlayerStats() {
        return playerStats;
    }

    public String getName() {
        return name;
    }
}
