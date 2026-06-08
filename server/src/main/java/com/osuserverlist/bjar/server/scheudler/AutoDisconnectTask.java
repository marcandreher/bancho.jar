package com.osuserverlist.bjar.server.scheudler;

import org.slf4j.Logger;

import com.osuserverlist.bjar.modules.logger.LoggerFactory;
import com.osuserverlist.bjar.server.Server;

public class AutoDisconnectTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(AutoDisconnectTask.class);

    @Override
    public void run() {
        Server server = Server.getInstance();

        long timeout = 140 * 1000; // 2 minutes

        server.playerManager.getAll().forEach(player -> {
            if (player.isBot())
                return;
            if (player.getLastPing() != 0 && System.currentTimeMillis() - player.getLastPing() > timeout) {
                logger.info("Auto disconnected {} cause of inactivity", player.toString());
                server.playerManager.disconnect(player);
            }
        });
    }
}
