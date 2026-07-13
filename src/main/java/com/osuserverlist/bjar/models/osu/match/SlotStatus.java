package com.osuserverlist.bjar.models.osu.match;

public enum SlotStatus {
    OPEN(1),
    LOCKED(2),
    NOT_READY(4),
    READY(8),
    NO_MAP_LOADED(16),
    PLAYING(32),
    COMPLETE(64),
    QUIT(128);

    public final int value;
    public final byte byteValue;

    SlotStatus(int value) {
        this.value = value;
        this.byteValue = (byte) value;
    }
}
