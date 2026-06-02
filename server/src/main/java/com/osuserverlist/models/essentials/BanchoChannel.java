package com.osuserverlist.models.essentials;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BanchoChannel {
    private String id;
    private String name;
    private String description;
    private boolean autoJoin;
    private final List<Player> players = new ArrayList<>();

    public int getPlayerCount() {
        return players.size() + 1; // +1 for the bot
    }
}