package com.osuserverlist.models.essentials;

import lombok.Data;

@Data
public class ModeStats {
    private int mode = 0;
    private long rankedScore = 0;
    private float accuracy = 0;
    private int playCount = 0;
    private long totalScore = 0;
    private int maxCombo = 0;
    private long globalRank = 0;
    private short pp = 0;
    private int totalHits = 0;

    public ModeStats() {
        this.mode = 0;
        this.rankedScore = 0;
        this.accuracy = 0;
        this.playCount = 0;
        this.totalScore = 0;
        this.maxCombo = 0;
        this.globalRank = 0;
        this.pp = 0;
        this.totalHits = 0;
    }

    public ModeStats(ModeStats stats) {
        this.mode = stats.mode;
        this.rankedScore = stats.rankedScore;
        this.accuracy = stats.accuracy;
        this.playCount = stats.playCount;
        this.totalScore = stats.totalScore;
        this.maxCombo = stats.maxCombo;
        this.globalRank = stats.globalRank;
        this.pp = stats.pp;
        this.totalHits = stats.totalHits;
    }

    public void addScore(Score s, short pp) {
        totalScore += s.getScore();
        rankedScore += s.getScore();
        playCount++;
        maxCombo = Math.max(maxCombo, s.getMax_combo());
        totalHits += s.getN300() + s.getN300() + s.getN50() + s.getNmiss();
        if (accuracy == 0) {
            accuracy = (float) s.getAccuracy();
        } else {
            accuracy = calculateAccuracy((float) s.getAccuracy(), accuracy);
        }
        this.pp = pp;
    }

    private float calculateAccuracy(float scoreAcc, float totalAcc) {
        if (scoreAcc > 1.0f) {
            scoreAcc /= 100.0f; // Convert from percentage to decimal if needed
        }
        if (totalAcc > 1.0f) {
            totalAcc /= 100.0f; // Convert from percentage to decimal if needed
        }
        return (scoreAcc + totalAcc) / 2;
    }
}
