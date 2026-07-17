package com.osuserverlist.bjar.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.osuserverlist.bjar.models.database.ChannelEntity;
import com.osuserverlist.bjar.models.essentials.Channel;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.datastore.MySQL;

public class ChannelManager {
    private final static Logger logger = LoggerFactory.getLogger(ChannelManager.class);
    private final Map<String, Channel> channels = new ConcurrentHashMap<>();

    public void populate(MySQL mysql) throws SQLException {
        ResultSet channelRs = mysql.query("SELECT * FROM `channels`").executeQuery();

        while (channelRs.next()) {
            ChannelEntity defaultChannel = ChannelEntity.fromResultSet(channelRs);
        
            Channel channel = Channel.builder()
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
        }

        // Only for allowing joining
        Channel highlightChannel = Channel.builder()
                .id("highlight")
                .name("#highlight")
                .alias("#highlight")
                .description("Highlight Channel")
                .autoJoin(false)
                .readPriv(0)
                .writePriv(0)
                .visible(false)
                .build();
        this.add(highlightChannel);

        channels.get("#lobby").setVisible(false);

        logger.info("Loaded <{}> channels", channels.size());
    }

    public void add(Channel channel) {
        channels.put(channel.getName(), channel);
    }

    public Channel get(String channelName) {
        return channels.get(channelName);
    }

    public void removeChannel(String channelName) {
        channels.remove(channelName);
    }

    public Collection<Channel> getAll() {
        return channels.values();
    }

    /**
     * Gracefully joins a channel.
     *
     * <p>Updates the channel info to players by marking it as dirty</p>
     *
     * @param channelName the name of the channel to join
     * @param player the player joining the channel
     */
    public void joinChannel(String channelName, Player player) {
        Channel channel = channels.get(channelName);
        if (channel != null) {
            channel.getPlayers().add(player);
        }

        channel.setDirty(true);
    }

    /**
     * Gracefully leaves a channel.
     *
     * <p>Updates the channel info to players by marking it as dirty</p>
     *
     * @param channelName the name of the channel to leave
     * @param player the player leaving the channel
     */
    public void leaveChannel(String channelName, Player player) {
        Channel channel = channels.get(channelName);
        if (channel != null) {
            channel.getPlayers().remove(player);
        }

        channel.setDirty(true);
    }

    /**
     * Forcefully joins a channel.
     *
     * <p>Never updates the channel info to players</p>
     *
     * @param channelName the name of the channel to join
     * @param player the player joining the channel
     */
    public void forceJoinChannel(String channelName, Player player) {
        Channel channel = channels.get(channelName);
        if (channel != null) {
            channel.getPlayers().add(player);
        }
    }

    /**
     * Forcefully leaves a channel.
     *
     * <p>Never updates the channel info to players</p>
     *
     * @param channelName the name of the channel to leave
     * @param player the player leaving the channel
     */
    public void forceLeaveChannel(String channelName, Player player) {
        Channel channel = channels.get(channelName);
        if (channel != null) {
            channel.getPlayers().remove(player);
        }
    }

}
