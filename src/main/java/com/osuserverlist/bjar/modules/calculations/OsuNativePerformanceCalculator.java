package com.osuserverlist.bjar.modules.calculations;

import com.osuserverlist.bjar.models.essentials.Score;
import com.osuserverlist.bjar.models.osu.Mods;

import io.github.nanamochi.osu_native.wrapper.attributes.difficulty.DifficultyAttributes;
import io.github.nanamochi.osu_native.wrapper.attributes.performance.PerformanceAttributes;
import io.github.nanamochi.osu_native.wrapper.factories.DifficultyCalculatorFactory;
import io.github.nanamochi.osu_native.wrapper.factories.PerformanceCalculatorFactory;
import io.github.nanamochi.osu_native.wrapper.objects.Beatmap;
import io.github.nanamochi.osu_native.wrapper.objects.Mod;
import io.github.nanamochi.osu_native.wrapper.objects.ModsCollection;
import io.github.nanamochi.osu_native.wrapper.objects.Ruleset;
import io.github.nanamochi.osu_native.wrapper.objects.ScoreInfo;

public class OsuNativePerformanceCalculator implements IPerformanceCalculator {

    @Override
    public double calculate(Score s, byte[] mapData) {
        Beatmap beatmap = Beatmap.fromBytes(mapData);
        Ruleset ruleset = Ruleset.fromId(s.getMode());
        var ppCalculator = PerformanceCalculatorFactory.create(Ruleset.fromId(s.getMode()));
        var diffCalculator = DifficultyCalculatorFactory.create(ruleset, beatmap);

        ScoreInfo scoreInfo = new ScoreInfo();
        scoreInfo.setAccuracy(s.getAccuracy());
        scoreInfo.setCountMiss(s.getNmiss());
        scoreInfo.setMaxCombo(s.getMax_combo());
        scoreInfo.setCountGreat(s.getN300());
        scoreInfo.setCountOk(s.getN100());
        scoreInfo.setCountMeh(s.getN50());
        scoreInfo.setCountPerfect(s.getNgeki());
        scoreInfo.setCountGood(s.getNkatu());
        
        ModsCollection mods = ModsCollection.create();

        for(String mod : Mods.convertMods(s.getMods()))
            mods.add(Mod.create(mod));

        mods.add(Mod.create("CL"));

        DifficultyAttributes difficultyAttributes = diffCalculator.calculate(mods);
        PerformanceAttributes attributes = ppCalculator.calculate(ruleset, beatmap, mods, scoreInfo,
                difficultyAttributes);

        beatmap.close();
        return attributes.getTotal();
    }

}
