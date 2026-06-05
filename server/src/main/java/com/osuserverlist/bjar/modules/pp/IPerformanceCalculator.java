package com.osuserverlist.bjar.modules.pp;

import com.osuserverlist.bjar.models.essentials.Score;

public interface IPerformanceCalculator {
    
    public double calculate(Score score, byte[] mapData);

}