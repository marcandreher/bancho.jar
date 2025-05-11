package com.banchojar;

import java.util.Stack;


import com.banchojar.Server.LoginState;
import com.banchojar.Server.PlayerState;
import com.banchojar.models.Score;
import com.banchojar.packets.server.ServerPacketHandler;

import lombok.Data;

@Data
public class Player {
    public Player(int id, boolean isBot) {
        this.id = id;
        this.isBot = isBot;
        if(!isBot)
            for(int i = 0; i < modeStats.length; i++) {
                modeStats[i] = new ModeStats();
            }

    }

    private Stack <ServerPacketHandler> packetStack = new Stack<>();
    private boolean inLobby = false;
    private boolean displayCityLocation = false;
    private boolean friendOnlyDms = false;
    private boolean isBot = false;

    private int lastNpBeatmapId = 0;

    private int id;
    private PlayerState playerState = PlayerState.CONNECTING;
    private LoginState loginState = LoginState.CONNECTING;
    private String username;

    private byte action = 0;                         // Idle
    private String actionText = "";                  // No action description
    private String beatmapMd5 = "";                  // No beatmap loaded
    private int mods = 0;                            // No mods
    private byte gameMode = 0;                       // osu! standard
    private int beatmapId = 0;                       // No beatmap

    private int timezone = 3;           // UTC+3
    private short country = 2;          // Country ID (1 = US)
    private byte privileges = 4;        // Example privilege flags 
    private float longitude = -122.4194f; // San Francisco
    private float latitude = 37.7749f;    // San Francisco
    private final int rank = 0;            // Example rank

    // Rank infos
    private int maxCombo = 0;

    // Mode Stats map
    private ModeStats[] modeStats = new ModeStats[5]; // 0: osu!, 1: osu!taiko, 2: osu!catch, 3: osu!mania, 4: osu!standard

    public void addPacketToStack(ServerPacketHandler packet) {
        if(isBot) return;

        packetStack.push(packet);
    }

    public ServerPacketHandler getPacketFromStack() {
        ServerPacketHandler packetStackResponse = packetStack.pop();
        return packetStackResponse;
    }

    public boolean isPacketStackEmpty() {
        return packetStack.isEmpty();
    }

    @Data
    public static class ModeStats {
        private int mode = 0;
        private long rankedScore = 0;
        private float accuracy = 0;
        private int playCount = 0;
        private long totalScore = 0;
        public int maxCombo = 0;
        private long globalRank = 0;
        private short pp = 0;
        private int totalHits = 0;

        public ModeStats() {
            this.mode = 0;
            this.rankedScore = 0;
            this.accuracy = 0;
            this.playCount = 0;
            this.totalScore = 0;
            this.maxCombo = 0;
            this.globalRank = 0;
            this.pp = 0;
            this.totalHits = 0;
        }

        public ModeStats(ModeStats stats) {
            this.mode = stats.mode;            
            this.rankedScore = stats.rankedScore;
            this.accuracy = stats.accuracy;
            this.playCount = stats.playCount;
            this.totalScore = stats.totalScore;
            this.maxCombo = stats.maxCombo;
            this.globalRank = stats.globalRank;
            this.pp = stats.pp;
            this.totalHits = stats.totalHits;
        }



        public void addScore(Score s, short pp) {
            totalScore += s.getScore();
            rankedScore += s.getScore();
            playCount++;
            maxCombo = Math.max(maxCombo, s.getMax_combo());
            totalHits += s.getN300() + s.getN300() + s.getN50() + s.getNmiss();
            if (accuracy == 0) {
                accuracy = (float)s.getAccuracy();
            } else {
                accuracy = calculateAccuracy((float) s.getAccuracy(), accuracy);
            }
            this.pp = pp;
        }

        private float calculateAccuracy(float scoreAcc, float totalAcc) {
            if (scoreAcc > 1.0f) {
                scoreAcc /= 100.0f; // Convert from percentage to decimal if needed
            }
            if (totalAcc > 1.0f) {
                totalAcc /= 100.0f; // Convert from percentage to decimal if needed
            }
            return (scoreAcc + totalAcc) / 2;
        }
    }
}
