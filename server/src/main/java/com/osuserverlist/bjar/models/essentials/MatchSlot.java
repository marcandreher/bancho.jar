package com.osuserverlist.bjar.models.essentials;

import com.osuserverlist.bjar.models.osu.match.SlotStatus;

import lombok.Data;

@Data
public class MatchSlot {
    byte status;
    byte team;
    int playerId;
    int mods;
    boolean loaded = false;
    boolean finished = false;
    boolean skipped = false;

    public void reset() {
        status = SlotStatus.OPEN.byteValue;
        team = 0;
        playerId = 0;
        mods = 0;
        loaded = false;
        skipped = false;
        finished = false;
    }
}
