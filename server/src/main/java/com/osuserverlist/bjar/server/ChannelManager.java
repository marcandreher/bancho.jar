package com.osuserverlist.bjar.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;

import com.osuserverlist.bjar.models.database.ChannelEntity;
import com.osuserverlist.bjar.models.essentials.BanchoChannel;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.database.MySQL;
import com.osuserverlist.bjar.modules.logger.LoggerFactory;

public class ChannelManager {
    private final Map<String, BanchoChannel> channels = new ConcurrentHashMap<>();
    private final static Logger logger = LoggerFactory.getLogger(ChannelManager.class);

    public void populate(MySQL mysql) throws SQLException {
        ResultSet channelRs = mysql.query("SELECT * FROM `channels`").executeQuery();
        
        int channelCount = 0;
        
        while (channelRs.next()) {
            ChannelEntity defaultChannel = ChannelEntity.fromResultSet(channelRs);
        
            BanchoChannel channel = BanchoChannel.builder()
                    .id(String.valueOf(defaultChannel.getId()))
                    .name(defaultChannel.getName())
                    .alias(defaultChannel.getAlias())
                    .description(defaultChannel.getTopic())
                    .autoJoin(defaultChannel.isAutoJoin())
                    .readPriv(defaultChannel.getReadPriv())
                    .writePriv(defaultChannel.getWritePriv())
                    .visible(true)
                    .build();
            if(channel.getName().equalsIgnoreCase("#lobby")) {
                channel.setVisible(false);
            }
            this.add(channel);
            channelCount++;
        }
        logger.info("Loaded <{}> channels from SQL", channelCount);
    }

    public void add(BanchoChannel channel) {
        channels.put(channel.getName(), channel);
    }

    public BanchoChannel get(String channelName) {
        return channels.get(channelName);
    }

    public void joinChannel(String channelName, Player player) {
        BanchoChannel channel = channels.get(channelName);
        if (channel != null) {
            channel.getPlayers().add(player);
        }

        channel.setDirty(true);
    }

    public void forceJoinChannel(String channelName, Player player) {
        BanchoChannel channel = channels.get(channelName);
        if (channel != null) {
            channel.getPlayers().add(player);
        }
    }

    public void forceLeaveChannel(String channelName, Player player) {
        BanchoChannel channel = channels.get(channelName);
        if (channel != null) {
            channel.getPlayers().remove(player);
        }
    }

    public void leaveChannel(String channelName, Player player) {
        BanchoChannel channel = channels.get(channelName);
        if (channel != null) {
            channel.getPlayers().remove(player);
        }

        channel.setDirty(true);
    }

    public void removeChannel(String channelName) {
        channels.remove(channelName);
    }

    public Collection<BanchoChannel> getAll() {
        return channels.values();
    }

}
