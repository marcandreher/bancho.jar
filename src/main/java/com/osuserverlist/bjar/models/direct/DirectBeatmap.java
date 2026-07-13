package com.osuserverlist.bjar.models.direct;

import lombok.Data;

@Data
public class DirectBeatmap {
    private double DifficultyRating;
    private String DiffName;
    private float CS;
    private float OD;
    private float AR;
    private float HP;
    private int Mode;
}
