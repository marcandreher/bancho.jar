package com.osuserverlist.models.database;

import de.marcandreher.fusionkit.core.database.Column;
import lombok.Data;

@Data
public class DbUser {
    @Column("id")
    private int id;

    @Column("name")
    private String name;

    @Column("safe_name")
    private String safeName;

    @Column("email")
    private String email;

    @Column("priv")
    private int priv;

    @Column("pw_bcrypt")
    private String pwBcrypt;

    @Column("country")
    private String country;

    @Column("silence_end")
    private int silenceEnd;

    @Column("donor_end")
    private int donorEnd;

    @Column("creation_time")
    private int creationTime;

    @Column("latest_activity")
    private int latestActivity;

    @Column("clan_id")
    private int clanId;

    @Column("clan_priv")
    private boolean clanPriv;

    @Column("preferred_mode")
    private int preferredMode;

    @Column("play_style")
    private int playStyle;

    @Column("custom_badge_name")
    private String customBadgeName;

    @Column("custom_badge_icon")
    private String customBadgeIcon;

    @Column("userpage_content")
    private String userpageContent;

    @Column("api_key")
    private String apiKey;
}
