package com.osuserverlist.bjar.server;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import com.osuserverlist.bjar.models.essentials.BanchoChannel;
import com.osuserverlist.bjar.models.essentials.Player;

public class PlayerManager {
    private final Map<String, Player> onlinePlayers = new ConcurrentHashMap<>();

    public void add(Player player) {
        onlinePlayers.put(player.getOsuToken(), player);
    }

    public Player get(String osuToken) {
        return onlinePlayers.get(osuToken);
    }

    public Player getByFilter(Predicate<Player> filter) {
        return onlinePlayers.values().stream().filter(filter).findFirst().orElse(null);
    }

    public Player getById(int id) {
        return getByFilter(p -> p.getId() == id);
    }

    public Collection<Player> getAll() {
        return onlinePlayers.values();
    }

    public void disconnect(Player player) {
        for(BanchoChannel channel : Server.getInstance().channelManager.getAll()) {
            if(channel.getPlayers().contains(player)) {
                Server.getInstance().channelManager.leaveChannel(channel.getName(), player);
            }
        }
        
        onlinePlayers.remove(player.getOsuToken());
    }
}
