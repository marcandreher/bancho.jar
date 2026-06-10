package com.osuserverlist.bjar.models.osu.replay;

import lombok.Data;

@Data
public class ScoreFrame {
    private int time;
    private int id;
    private int num300;
    private int num100;
    private int num50;
    private int numGeki;
    private int numKatu;
    private int numMiss;
    private int totalScore;
    private int maxCombo;
    private int currentCombo;
    private boolean perfect;
    private int hp;
    private int tagByte;
    private boolean scoreVersion2;

    // if score v2
    private double comboPortion;
    private double bonusPortion;
}
