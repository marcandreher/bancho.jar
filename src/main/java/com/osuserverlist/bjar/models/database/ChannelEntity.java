package com.osuserverlist.bjar.models.database;

import java.sql.ResultSet;
import java.sql.SQLException;

import lombok.Data;

@Data
public class ChannelEntity {
    private int id;
    private String name;
    private String alias;
    private String topic;
    private int readPriv;
    private int writePriv;
    private boolean autoJoin;

    public static ChannelEntity fromResultSet(ResultSet channelResult) throws SQLException {
        ChannelEntity channel = new ChannelEntity();
        
        channel.id = channelResult.getInt("id");
        channel.name = channelResult.getString("name");
        channel.alias = channel.name;
        channel.topic = channelResult.getString("topic");
        channel.readPriv = channelResult.getInt("read_priv");
        channel.writePriv = channelResult.getInt("write_priv");
        channel.autoJoin = channelResult.getBoolean("auto_join");

        return channel;
    }

}
