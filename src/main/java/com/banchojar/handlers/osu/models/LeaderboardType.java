package com.banchojar.handlers.osu.models;

public enum LeaderboardType {
    NORMAL(4),
    GLOBAL(1),
    GLOBAL_MODS(2),
    FRIENDS(3);

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
        return NORMAL; // Default to NORMAL if not found
    }

}