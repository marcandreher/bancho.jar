package com.osuserverlist.bjar.server;

import com.osuserverlist.bjar.models.config.ServerConfiguration;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.osu.OsuAPIHandler;

public class Server {
    private static Server instance;

    public static Server getInstance() {
        return instance;
    }

    public Player botPlayer;
    public OsuAPIHandler osuAPIHandler;
    public ServerConfiguration config = ServerConfiguration.load();
    public PlayerManager playerManager = new PlayerManager();
    public ChannelManager channelManager = new ChannelManager();

    public static Server start() {
        instance = new Server();
        return instance;
    }
}
