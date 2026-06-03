package com.osuserverlist.bjar.server;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.osuserverlist.bjar.models.essentials.BanchoChannel;
import com.osuserverlist.bjar.models.essentials.Player;

public class ChannelManager {
    private final Map<String, BanchoChannel> channels = new ConcurrentHashMap<>();

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

        // TODO: notify players in channel with new channel info packet
    }

    public void leaveChannel(String channelName, Player player) {
        BanchoChannel channel = channels.get(channelName);
        if (channel != null) {
            channel.getPlayers().remove(player);
        }

        // TODO: notify players in channel with new channel info packet
    }
    
    public Collection<BanchoChannel> getAll() {
        return channels.values();
    }

}
