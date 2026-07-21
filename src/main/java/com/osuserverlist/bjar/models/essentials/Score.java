package com.osuserverlist.bjar.models.essentials;

import java.time.ZoneOffset;

import com.osuserverlist.bjar.models.database.BeatmapEntity;
import com.osuserverlist.bjar.models.database.ScoreEntity;
import com.osuserverlist.bjar.modules.calculations.AccuracyCalculator;

import lombok.Data;

@Data
public class Score {
    private long id;
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

    public static Score fromEntity(ScoreEntity scoreEntity, BeatmapEntity beatmap) {
        Score s = new Score();
        s.setId(scoreEntity.getId().intValue());
        s.setPlayerId(scoreEntity.getUser().getId());
        s.setN300(scoreEntity.getN300());
        s.setN100(scoreEntity.getN100());
        s.setN50(scoreEntity.getN50());
        s.setNgeki(scoreEntity.getNgeki());
        s.setNkatu(scoreEntity.getNkatu());
        s.setNmiss(scoreEntity.getNmiss());
        s.setScore(scoreEntity.getScore());
        s.setMax_combo(scoreEntity.getMaxCombo());
        s.setPerfect(scoreEntity.getPerfect());
        s.setGrade(scoreEntity.getGrade());
        s.setMode(scoreEntity.getMode());
        s.setPlaytime(scoreEntity.getPlayTime().toInstant(ZoneOffset.UTC).toEpochMilli());
        s.setFlags(scoreEntity.getClientFlags());
        s.setMods(scoreEntity.getMods());
        s.setMapMd5(beatmap.getMd5());
        s.setUsername(scoreEntity.getUser().getName());
        s.setAccuracy(scoreEntity.getAcc());

        return s;
    }

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
        s.setAccuracy(AccuracyCalculator.calculateAccuracy(s));
        return s;
    }


    public static String buildScoreWebString(Score s, long scoreId, int submitted) {
        return "\n" + s.getId() + "|" + s.getUsername() + "|" + s.getScore() + "|" + s.getMax_combo() + "|"
                + s.getN50() + "|" + s.getN100() + "|" + s.getN300() + "|" + s.getNmiss() + "|" + s.getNkatu() + "|"
                + s.getNgeki() + "|" + (s.isPerfect() ? 1 : 0) + "|" + s.getMods() + "|" + s.getPlayerId() + "|"
                + s.getRank() + "|" + s.getPlaytime() + "|" + 1;
    }
}