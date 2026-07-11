package com.osuserverlist.bjar.models.osu.match;

public enum MatchType {
    STANDARD((byte)0),
    POWERPLAY((byte)1);

    public final byte value;

    MatchType(byte value) {
        this.value = value;
    }

    public static final MatchType byByte(byte matchType) {
        for (MatchType type : MatchType.values()) {
            if (type.value == matchType) {
                return type;
            }
        }
        return null;
    }
}
