package com.osuserverlist.bjar.models.database;

import java.sql.ResultSet;
import lombok.Data;

@Data
public class DbUser {
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

    public DbUser(ResultSet userResult) {
        try {
            this.id = userResult.getInt("id");
            this.name = userResult.getString("name");
            this.safeName = userResult.getString("safe_name");
            this.email = userResult.getString("email");
            this.priv = userResult.getInt("priv");
            this.pwBcrypt = userResult.getString("pw_bcrypt");
            this.country = userResult.getString("country");
            this.silenceEnd = userResult.getInt("silence_end");
            this.donorEnd = userResult.getInt("donor_end");
            this.creationTime = userResult.getInt("creation_time");
            this.latestActivity = userResult.getInt("latest_activity");
            this.clanId = userResult.getInt("clan_id");
            this.clanPriv = userResult.getBoolean("clan_priv");
            this.preferredMode = userResult.getInt("preferred_mode");
            this.playStyle = userResult.getInt("play_style");
            this.customBadgeName = userResult.getString("custom_badge_name");
            this.customBadgeIcon = userResult.getString("custom_badge_icon");
            this.userpageContent = userResult.getString("userpage_content");
            this.apiKey = userResult.getString("api_key");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
