package com.osuserverlist.bjar.modules.achievements;

import com.osuserverlist.bjar.models.database.BeatmapEntity;
import com.osuserverlist.bjar.models.essentials.Score;

import lombok.Data;

@Data
public class MevlScore {
    public long score;
    public boolean perfect;
    public int mods;
    public float sr;
    public long max_combo;

    public static MevlScore from(Score score, BeatmapEntity beatmap) {
        MevlScore j = new MevlScore();
        j.score = score.getScore();
        j.perfect = score.isPerfect();
        j.mods = score.getMods();
        j.sr = beatmap.getDiff();
        j.max_combo = score.getMax_combo();
        return j;
    }
}
