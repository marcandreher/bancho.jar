package com.osuserverlist.models.essentials;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BanchoChannel {
    private String id;
    private String name;
    private String description;
    private boolean autoJoin;
    private final Set<Player> players = ConcurrentHashMap.newKeySet();

    public int getPlayerCount() {
        return players.size() + 1; // +1 for the bot
    }
}