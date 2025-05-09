package com.banchojar.models;

import lombok.Data;

@Data
public class Score {
    private int id;
    private int beatmapId = 1;
    private int playerId = 1;
    private int n300 = 0;
    private int n100 = 0;
    private int n50 = 0;
    private int ngeki = 0;
    private int nkatu = 0;
    private int nmiss = 0;
    private long score = 0;
    private int max_combo = 0;
    private boolean perfect = false;
    private int rank = 0;
    private boolean passed = false;
    private String grade = "A";
    private int mode = 0;
    private int playtime = 60000;
    private long flags = 0;
    private int mods = 0;
    private String username = "unknown";
    private String mapMd5 = "";
    private double pp = 0.0;
    private double accuracy = 0.0;
    private String checksum = "";

    public int getScore() {
        return (int) this.score;
    }

    public static String buildScoreWebString(Score s, int scoreId, int submitted) {
        return "\n" + s.getId() + "|" + s.getUsername() + "|" + s.getScore() + "|" + s.getMax_combo() + "|"
                + s.getN50() + "|" + s.getN100() + "|" + s.getN300() + "|" + s.getNmiss() + "|" + s.getNkatu() + "|"
                + s.getNgeki() + "|" + (s.isPerfect() ? 1 : 0) + "|" + s.getMods() + "|" + s.getPlayerId() + "|"
                + s.getRank() + "|" + s.getPlaytime() + "|" + 1;
    }
}
