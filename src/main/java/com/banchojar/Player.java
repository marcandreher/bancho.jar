package com.banchojar;

import lombok.Data;

@Data
public class Player {

    public Player(int id) {
        this.id = id;
        this.username = "User" + id;
    }


    private int id;
    private PlayerState playerState = PlayerState.CONNECTING;
    private LoginState loginState = LoginState.CONNECTING;
    private String username;

    // --- StatusUpdate fields --- 0B 00 00 2E 00 00 00 E8 03 00 00 00 00 00 00 00 00 00 00 00 00 00 00 F4 01 00 00 00 00 00 00 00 00 80 3F 2B 02 00 00 B3 15 00 00 00 00 00 00 01 00 00 00 0F 27
    private byte action = 0;                         // Idle
    private String actionText = "";                  // No action description
    private String beatmapMd5 = "";                  // No beatmap loaded
    private int mods = 0;                            // No mods
    private byte gameMode = 0;                       // osu! standard
    private int beatmapId = 0;                       // No beatmap

    private int timezone = 3;           // UTC+3
    private short country = 1;          // Country ID (1 = US)
    private byte privileges = 4;        // Example privilege flags
    private byte mode = 0;              // osu!standard (0)
    private float longitude = -122.4194f; // San Francisco
    private float latitude = 37.7749f;    // San Francisco
    private int rank = 1;            // Example rank

  

    // --- Stats fields ---
    private long rankedScore = 0;
    private float accuracy = 0;
    private int playCount = 0;
    private long totalScore = 0;
    private int globalRank = 0;                // Unranked
    private short pp = 0;
}
