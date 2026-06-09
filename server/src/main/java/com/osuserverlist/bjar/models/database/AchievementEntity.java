package com.osuserverlist.bjar.models.database;

import java.sql.ResultSet;

import lombok.Data;

@Data
public class AchievementEntity {
    private int id;
    private String file;
    private String name;
    private String description;
    private String condition;

    public static AchievementEntity fromResultSet(ResultSet achievementResult) throws java.sql.SQLException {
        AchievementEntity achievement = new AchievementEntity();
        achievement.setId(achievementResult.getInt("id"));
        achievement.setFile(achievementResult.getString("file"));
        achievement.setName(achievementResult.getString("name"));
        achievement.setDescription(achievementResult.getString("desc"));
        achievement.setCondition(achievementResult.getString("cond"));
        return achievement;
    }
}
