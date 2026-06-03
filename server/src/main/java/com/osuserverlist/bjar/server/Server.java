package com.osuserverlist.bjar.server;

import com.osuserverlist.bjar.models.config.ServerConfiguration;
import com.osuserverlist.bjar.models.essentials.Player;

public class Server {
    private static final Server instance;

    static {
        instance = new Server();

    }

    public static Server getInstance() {
        return instance;
    }

    public Player botPlayer;
    public ServerConfiguration config = ServerConfiguration.load();
    public PlayerManager playerManager = new PlayerManager();
    public ChannelManager channelManager = new ChannelManager();
}
