package com.osuserverlist.bjar.models.essentials;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.osuserverlist.bjar.models.database.DbMap;

import lombok.Data;

@Data
public class Score {
    private int id;
    private long beatmapId = 1;
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
    private long playtime = 60000;
    private int flags = 0;
    private int mods = 0;
    private String username = "unknown";
    private String mapMd5 = "";
    private double pp = 0.0;
    private double accuracy = 0.0;
    private String checksum = "";

    public static Score fromSubmission(String[] data, Player p) {
        Score s = new Score();
        s.setPlayerId(p.getId());
        s.setN300(Integer.parseInt(data[3]));
        s.setN100(Integer.parseInt(data[4]));
        s.setN50(Integer.parseInt(data[5]));
        s.setNgeki(Integer.parseInt(data[6]));
        s.setNkatu(Integer.parseInt(data[7]));
        s.setNmiss(Integer.parseInt(data[8]));
        s.setScore(Integer.parseInt(data[9]));
        s.setMax_combo(Integer.parseInt(data[10]));
        s.setPerfect(Boolean.parseBoolean(data[11]));
        s.setGrade(data[12]);
        s.setMods(Integer.parseInt(data[13]));
        s.setPassed(Boolean.parseBoolean(data[14]));
        s.setMode(Integer.parseInt(data[15]));
        s.setPlaytime((System.currentTimeMillis()));
        s.setFlags((int) data[15].chars().filter(c -> c == ' ').count() & ~4);
        s.setAccuracy(calcAccFromScore(s));
        return s;
    }

    private static float calcAccFromScore(Score s) {
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

    public int getScore() {
        return (int) this.score;
    }

    public static Score fromResultSet(ResultSet scoreResult, DbMap beatmap) throws SQLException {
        Score s = new Score();
        s.setId(scoreResult.getInt("id"));
        s.setBeatmapId(beatmap.getId());
        s.setPlayerId(scoreResult.getInt("userid"));
        s.setN300(scoreResult.getInt("n300"));
        s.setN100(scoreResult.getInt("n100"));
        s.setN50(scoreResult.getInt("n50"));
        s.setNgeki(scoreResult.getInt("ngeki"));
        s.setNkatu(scoreResult.getInt("nkatu"));
        s.setNmiss(scoreResult.getInt("nmiss"));
        s.setScore(scoreResult.getLong("score"));
        s.setMax_combo(scoreResult.getInt("max_combo"));
        s.setPerfect(scoreResult.getBoolean("perfect"));
        s.setGrade(scoreResult.getString("grade"));
        s.setMode(scoreResult.getInt("mode"));
        s.setPlaytime(
            scoreResult.getTimestamp("play_time")
                .toInstant()
                .getEpochSecond()
        );
        s.setFlags(scoreResult.getInt("client_flags"));
        s.setMods(scoreResult.getInt("mods"));
        s.setUsername(scoreResult.getString("name"));
        s.setMapMd5(beatmap.getMd5());
        s.setChecksum(scoreResult.getString("online_checksum"));
        
        return s;
    }

    public static String buildScoreWebString(Score s, int scoreId, int submitted) {
        return "\n" + s.getId() + "|" + s.getUsername() + "|" + s.getScore() + "|" + s.getMax_combo() + "|"
                + s.getN50() + "|" + s.getN100() + "|" + s.getN300() + "|" + s.getNmiss() + "|" + s.getNkatu() + "|"
                + s.getNgeki() + "|" + (s.isPerfect() ? 1 : 0) + "|" + s.getMods() + "|" + s.getPlayerId() + "|"
                + s.getRank() + "|" + s.getPlaytime() + "|" + 1;
    }
}