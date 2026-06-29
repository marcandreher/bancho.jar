package com.osuserverlist.bjar.models.osu;

public enum LeaderboardType {
    GLOBAL(1),
    GLOBAL_MODS(2),
    FRIENDS(3),
    COUNTRY(4);

    private final int id;

    LeaderboardType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static LeaderboardType getById(int id) {
        for (LeaderboardType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        return GLOBAL; // Default to global if unknown type received
    }
}