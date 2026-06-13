package com.osuserverlist.bjar.models.essentials;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.osuserverlist.bjar.modules.redis.Redis;

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
    private int pp = 0;
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

    public static ModeStats fromResultSet(ResultSet modeResult, int i, Player player) throws SQLException {
        ModeStats stats = new ModeStats();
        stats.setPlayCount(modeResult.getInt("plays"));
        stats.setTotalScore(modeResult.getLong("tscore"));
        stats.setRankedScore(modeResult.getLong("rscore"));
        stats.setAccuracy(modeResult.getFloat("acc"));
        stats.setMaxCombo(modeResult.getInt("max_combo"));
        stats.setPp(modeResult.getInt("pp"));
        stats.setTotalHits(modeResult.getInt("total_hits"));
        Long rank = Redis.getClient().zrevrank(
                "bjar:leaderboard:" + i,
                String.valueOf(player.getId()));

        stats.setGlobalRank(rank != null ? Math.toIntExact(rank) + 1 : 0);
        return stats;
    }

    public void addUnrankedScore(Score s) {
        totalScore += s.getScore();
        playCount++;
        totalHits += s.getN300()
                + s.getN100()
                + s.getN50()
                + s.getNmiss();
    }

    public void addRankedScore(Score s, double pp) {
        totalScore += s.getScore();

        rankedScore += s.getScore();

        playCount++;
        maxCombo = Math.max(maxCombo, s.getMax_combo());
        totalHits += s.getN300()
                + s.getN100()
                + s.getN50()
                + s.getNmiss();
        if (accuracy == 0) {
            accuracy = (float) s.getAccuracy();
        } else {
            accuracy = calculateAccuracy((float) s.getAccuracy(), accuracy);
        }
        this.pp = (int) pp;
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
