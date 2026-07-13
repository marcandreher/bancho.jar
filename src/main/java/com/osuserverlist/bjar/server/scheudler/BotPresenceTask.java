package com.osuserverlist.bjar.server.scheudler;

import java.util.Random;

import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.config.PresenceConfiguration;
import com.osuserverlist.bjar.models.config.PresenceConfiguration.PresenceInfo;

public class BotPresenceTask implements Runnable {
    
    private final PresenceConfiguration config;

    public BotPresenceTask() {
        config = PresenceConfiguration.load();
    }

    @Override
    public void run() {
        Server server = Server.getInstance();

        Random random = new Random();
        PresenceInfo presenceInfo = config.getPresenceInfos().get(random.nextInt(config.getPresenceInfos().size()));
        server.botPlayer.setAction((byte)presenceInfo.getActionStatus().id);
        server.botPlayer.setActionText(presenceInfo.getDetails());
    }

}
