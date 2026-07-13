package com.osuserverlist.bjar.modules.calculations;

import com.osuserverlist.bjar.models.essentials.Score;

public interface IPerformanceCalculator {
    
    public double calculate(Score score, byte[] mapData);

}