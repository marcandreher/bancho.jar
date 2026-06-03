package com.osuserverlist.bjar.models.database;

import java.sql.ResultSet;

import lombok.Data;

@Data
public class DbChannel {
    private int id;
    private String name;
    private String topic;
    private int readPriv;
    private int writePriv;
    private boolean autoJoin;

    public DbChannel(ResultSet channelResult) {
        try {
            this.id = channelResult.getInt("id");
            this.name = channelResult.getString("name");
            this.topic = channelResult.getString("topic");
            this.readPriv = channelResult.getInt("read_priv");
            this.writePriv = channelResult.getInt("write_priv");
            this.autoJoin = channelResult.getBoolean("auto_join");
        } catch (Exception e) {
            e.printStackTrace();
        }
    } 
}
