package com.osuserverlist.bjar.repos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.osuserverlist.bjar.models.database.AchievementEntity;
import com.osuserverlist.bjar.modules.achievements.PythonMevlRewriter;
import com.osuserverlist.bjar.modules.database.MySQL;

public class AchievementRepository {

    private final MySQL mysql;

    public AchievementRepository(MySQL mysql) {
        this.mysql = mysql;
    }

    public List<AchievementEntity> getAll() throws SQLException {
        List<AchievementEntity> achievements = new ArrayList<>();

        ResultSet achievementResult = mysql.query(GET_ALL_ACHIEVEMENTS_QUERY).executeQuery();
        while (achievementResult.next()) {
            AchievementEntity achievement = AchievementEntity.fromResultSet(achievementResult);
            achievement.setCondition(PythonMevlRewriter.rewrite(achievement.getCondition()));
            achievements.add(achievement);
        }

        return achievements;
    }

    public void addAchievementToPlayer(int playerId, int achievementId) throws SQLException {
        mysql.exec(INSERT_ACHIEVEMENT_TO_PLAYER_QUERY, playerId, achievementId);
    }

    public void getAllAchievementsForPlayer(int playerId, Consumer<Integer> achievementIdConsumer) throws SQLException {
        ResultSet achievementResult = mysql.query(GET_ALL_ACHIEVEMENTS_FOR_PLAYER_QUERY, playerId).executeQuery();
        while (achievementResult.next()) {
            achievementIdConsumer.accept(achievementResult.getInt("achid"));
        }
    }

    private static final String INSERT_ACHIEVEMENT_TO_PLAYER_QUERY = "INSERT INTO `user_achievements`(`userid`, `achid`) VALUES (?,?)";
    private static final String GET_ALL_ACHIEVEMENTS_QUERY = "SELECT * FROM `achievements`";
    private static final String GET_ALL_ACHIEVEMENTS_FOR_PLAYER_QUERY = "SELECT * FROM `user_achievements` WHERE `userid` = ?";
}
