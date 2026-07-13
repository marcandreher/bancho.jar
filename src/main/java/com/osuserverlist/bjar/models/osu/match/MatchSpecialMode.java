package com.osuserverlist.bjar.models.osu.match;

public enum MatchSpecialMode {
    NONE((byte)0),
    FREE_MOD((byte)1);

    public final byte value;

    MatchSpecialMode(byte value) {
        this.value = value;
    }

    public static final MatchSpecialMode byByte(byte specialMode) {
        for (MatchSpecialMode mode : MatchSpecialMode.values()) {
            if (mode.value == specialMode) {
                return mode;
            }
        }
        return null;
    }
}
