package com.osuserverlist.bjar.models.osu;

public enum PresenceFilter {
    NONE(0),
    ALL(1),
    FRIENDS(2);

    private final int id;

    private PresenceFilter(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
