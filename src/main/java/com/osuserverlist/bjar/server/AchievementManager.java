package com.osuserverlist.bjar.server;

import java.util.Collection;
import java.util.List;

import org.mvel2.MVEL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.osuserverlist.bjar.models.database.AchievementEntity;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.util.MevlParser.PythonMevlRewriter;
import com.osuserverlist.bjar.repos.AchievementRepository;

public class AchievementManager {
    private final static Logger logger = LoggerFactory.getLogger(AchievementManager.class);
    private List<AchievementEntity> achievements;

    public void populate() {
        achievements = AchievementRepository.findAll();
        achievements.forEach(achievement -> {
            achievement.setCondition(PythonMevlRewriter.rewrite(achievement.getCondition()));
            achievement.setConditionCompiled(MVEL.compileExpression(achievement.getCondition()));
        });

        logger.info("Loaded <{}> achievements", achievements.size());
    }

    public void loadForPlayer(Player player) {
        AchievementRepository.findByUser(player.getEntity()).forEach(ach -> {
            player.getUnlockedAchievements().add(ach.getId());
        });
    }

    public Collection<AchievementEntity> getAll() {
        return achievements;
    }

}
