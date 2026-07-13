package com.osuserverlist.bjar.models.osu.match;

public enum MatchScoringType {
    SCORE((byte)0),
    ACCURACY((byte)1),
    COMBO((byte)2),
    SCOREV2((byte)3);

    public final byte value;

    MatchScoringType(byte value) {
        this.value = value;
    }

    public static final MatchScoringType byByte(byte scoringType) {
        for (MatchScoringType type : MatchScoringType.values()) {
            if (type.value == scoringType) {
                return type;
            }
        }
        return null;
    }

}
