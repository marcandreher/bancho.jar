package com.osuserverlist;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.osuserverlist.models.essentials.Player;

public class Server {
    private static final Server instance;

    static {
        instance = new Server();
    }

    public static Server getInstance() {
        return instance;
    }

    private final Map<String, Player> onlinePlayers = new ConcurrentHashMap<>();

    public Map<String, Player> getOnlinePlayers() {
        return onlinePlayers;
    }

    public void addOnlinePlayer(Player player) {
        onlinePlayers.put(player.getOsuToken(), player);
    }
}
