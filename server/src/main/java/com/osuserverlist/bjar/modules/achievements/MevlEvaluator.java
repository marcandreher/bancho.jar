package com.osuserverlist.bjar.modules.achievements;

import java.util.HashMap;
import java.util.Map;

import org.mvel2.MVEL;

import com.osuserverlist.bjar.models.database.BeatmapEntity;
import com.osuserverlist.bjar.models.essentials.Score;

public class MevlEvaluator {
    
    public boolean evaluate(String condition, Score score, BeatmapEntity beatmap) {
        condition = PythonMevlRewriter.rewrite(condition);

        Map<String, Object> vars = new HashMap<>();

        vars.put("score", MevlScore.from(score, beatmap));
        vars.put("mode_vn", score.getMode());

        Object result = MVEL.eval(condition, vars);

        return Boolean.TRUE.equals(result);
    }

}
