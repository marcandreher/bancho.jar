package com.osuserverlist.main;

public class Server {
    private static final Server instance;

    static {
        instance = new Server();
    }

    public static Server getInstance() {
        return instance;
    }

    public PlayerManager playerManager = new PlayerManager();
    public ChannelManager channelManager = new ChannelManager();
}
