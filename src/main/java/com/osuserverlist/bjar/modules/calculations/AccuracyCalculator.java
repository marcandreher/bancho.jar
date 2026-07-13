package com.osuserverlist.bjar.modules.calculations;

import com.osuserverlist.bjar.models.essentials.Score;

public class AccuracyCalculator {
    public static double calculateAccuracy(Score s) {
        // TODO: make more accurate and match other gamemodes logic
        int n300 = s.getN300();
        int n100 = s.getN100();
        int n50 = s.getN50();
        int nMiss = s.getNmiss(); // Make sure you have this

        int totalHits = n300 + n100 + n50 + nMiss;
        if (totalHits == 0)
            return 0f;

        float acc = (n50 * 50 + n100 * 100 + n300 * 300) / (float) (totalHits * 300);
        return acc;
    }
}
