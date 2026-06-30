package com.osuserverlist.bjar.server;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;

import com.osuserverlist.bjar.models.database.AchievementEntity;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.database.MySQL;
import com.osuserverlist.bjar.modules.logger.LoggerFactory;
import com.osuserverlist.bjar.repos.AchievementRepository;

public class AchievementManager {

    private final static Logger logger = LoggerFactory.getLogger(AchievementManager.class);
    private List<AchievementEntity> achievements;

    public void populate(MySQL mysql) throws SQLException {
        AchievementRepository achievementRepo = new AchievementRepository(mysql);
        
        achievements = achievementRepo.getAll();

        logger.info("Loaded <{}> achievements", achievements.size());
    }

    public void loadForPlayer(Player player, MySQL mysql) throws SQLException {
        AchievementRepository achievementRepo = new AchievementRepository(mysql);
        achievementRepo.getAllAchievementsForPlayer(player.getId(), achId -> {
            player.getUnlockedAchievements().add(achId);
        });
    }

    public Collection<AchievementEntity> getAll() {
        return achievements;
    }

}
