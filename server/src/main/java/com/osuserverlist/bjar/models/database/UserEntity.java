package com.osuserverlist.bjar.models.database;

import java.sql.ResultSet;
import java.sql.SQLException;

import lombok.Data;

@Data
public class UserEntity {
    private int id;
    private String name;
    private String safeName;
    private String email;
    private int priv;
    private String pwBcrypt;
    private String country;
    private int silenceEnd;
    private int donorEnd;
    private int creationTime;
    private int latestActivity;
    private int clanId;
    private boolean clanPriv;
    private int preferredMode;
    private int playStyle;
    private String customBadgeName;
    private String customBadgeIcon;
    private String userpageContent;
    private String apiKey;

    public static UserEntity fromResultSet(ResultSet userResult) throws SQLException {
        UserEntity user = new UserEntity();

        user.id = userResult.getInt("id");
        user.name = userResult.getString("name");
        user.safeName = userResult.getString("safe_name");
        user.email = userResult.getString("email");
        user.priv = userResult.getInt("priv");
        user.pwBcrypt = userResult.getString("pw_bcrypt");
        user.country = userResult.getString("country");
        user.silenceEnd = userResult.getInt("silence_end");
        user.donorEnd = userResult.getInt("donor_end");
        user.creationTime = userResult.getInt("creation_time");
        user.latestActivity = userResult.getInt("latest_activity");
        user.clanId = userResult.getInt("clan_id");
        user.clanPriv = userResult.getBoolean("clan_priv");
        user.preferredMode = userResult.getInt("preferred_mode");
        user.playStyle = userResult.getInt("play_style");
        user.customBadgeName = userResult.getString("custom_badge_name");
        user.customBadgeIcon = userResult.getString("custom_badge_icon");
        user.userpageContent = userResult.getString("userpage_content");
        user.apiKey = userResult.getString("api_key");

        return user;
    }
}
