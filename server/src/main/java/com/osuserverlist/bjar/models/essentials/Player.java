package com.osuserverlist.bjar.models.essentials;

import java.util.Stack;

import com.osuserverlist.bjar.packets.server.ServerPacketHandler;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Player {
    
    public Player(int id, boolean isBot, String osuToken) {
        this.id = id;
        this.isBot = isBot;
        this.osuToken = osuToken;
        this.lastPing = System.currentTimeMillis();
        if (!isBot)
            for (int i = 0; i < modeStats.length; i++) {
                modeStats[i] = new ModeStats();
            }

    }

    private int id;
    private boolean isBot = false;
    private String apiIdent = "";
    private boolean relaxEnabled = false;

    private ModeStats[] modeStats = new ModeStats[9];

    private String username;
    private String osuToken;

    private byte action = 0; // Idle
    private String actionText = ""; // No action description
    private String beatmapMd5 = ""; // No beatmap loaded
    private int mods = 0; // No mods
    private byte gameMode = 0; // osu! standard
    private int realGameMode = 0;
    private int beatmapId = 0; // No beatmap

    private int timezone = 3; // UTC+3
    private short country = 2; // Country ID (1 = US)
    private int privileges = 1; // Example privilege flags
    private int realPrivileges = 1; // Example privilege flags
    private float longitude = -122.4194f; // San Francisco
    private float latitude = 37.7749f; // San Francisco
    private final int rank = 0; // Example rank

    private long lastPing = System.currentTimeMillis();

    private Stack<ServerPacketHandler> packetStack = new Stack<>();
    private boolean inLobby = false;
    private boolean displayCityLocation = false;
    private boolean friendOnlyDms = false;

    public void sendPacket(ServerPacketHandler handler) {
        if(isBot) return;
        packetStack.push(handler);
    }

    @Override
    public String toString() {
        return String.format("<%s>(%d)", username, id);
    }

}
