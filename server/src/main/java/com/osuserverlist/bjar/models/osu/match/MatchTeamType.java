package com.osuserverlist.bjar.models.osu.match;

public enum MatchTeamType {
    HEAT_TO_HEAD((byte)0),
	TAG_COOP((byte)1),
	TEAM_VS((byte)2),
	TAG_TEAM_VS((byte)3);

    public final byte value;

    MatchTeamType(byte value) {
        this.value = value;
    }

    public static final MatchTeamType byByte(byte teamType) {
        for (MatchTeamType type : MatchTeamType.values()) {
            if (type.value == teamType) {
                return type;
            }
        }
        return null;
    }
}
