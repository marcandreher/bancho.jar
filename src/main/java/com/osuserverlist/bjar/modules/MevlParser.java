package com.osuserverlist.bjar.modules;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mvel2.MVEL;

import com.osuserverlist.bjar.models.database.BeatmapEntity;
import com.osuserverlist.bjar.models.essentials.Score;

import lombok.Data;

public class MevlParser {
    public static boolean evaluate(Serializable condition, Score score, BeatmapEntity beatmap) {
        Map<String, Object> vars = new HashMap<>();

        vars.put("score", MevlScore.from(score, beatmap));
        vars.put("mode_vn", score.getMode());

        Object result = MVEL.executeExpression(condition, vars);

        return Boolean.TRUE.equals(result);
    }

    @Data
    public static class MevlScore {
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

    public static class PythonMevlRewriter {
        private static final Pattern CHAINED_COMPARISON = Pattern.compile(
                "(\\S+)\\s*(<=|<|>=|>)\\s*(\\S+)\\s*(<=|<|>=|>)\\s*(\\S+)");

        private static final Pattern BITWISE_COMPARE = Pattern.compile(
                "(\\S+)\\s*&\\s*(\\d+)\\s*(==|!=)\\s*(\\d+)");

        public static String rewrite(String expr) {
            Matcher m = CHAINED_COMPARISON.matcher(expr);

            while (m.find()) {
                String left = m.group(1);
                String op1 = m.group(2);
                String middle = m.group(3);
                String op2 = m.group(4);
                String right = m.group(5);

                String replacement = "(" + left + " " + op1 + " " + middle + ")" +
                        " && " +
                        "(" + middle + " " + op2 + " " + right + ")";

                expr = expr.replace(m.group(0), replacement);
                m = CHAINED_COMPARISON.matcher(expr);
            }

            m = BITWISE_COMPARE.matcher(expr);

            while (m.find()) {
                String value = m.group(1);
                String mask = m.group(2);
                String op = m.group(3);
                String compare = m.group(4);

                String replacement = "((" + value + " & " + mask + ") " +
                        op + " " + compare + ")";

                expr = expr.replace(m.group(0), replacement);
                m = BITWISE_COMPARE.matcher(expr);
            }

            expr = expr.replaceAll("\\band\\b", "&&");
            expr = expr.replaceAll("\\bor\\b", "||");
            expr = expr.replaceAll("\\bnot\\b", "!");

            return expr;
        }
    }

}
