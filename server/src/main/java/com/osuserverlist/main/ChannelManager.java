package com.osuserverlist.main;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.osuserverlist.models.essentials.BanchoChannel;
import com.osuserverlist.models.essentials.Player;

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
    }

    public void leaveChannel(String channelName, Player player) {
        BanchoChannel channel = channels.get(channelName);
        if (channel != null) {
            channel.getPlayers().remove(player);
        }
    }
    
    public Collection<BanchoChannel> getAll() {
        return channels.values();
    }

}
