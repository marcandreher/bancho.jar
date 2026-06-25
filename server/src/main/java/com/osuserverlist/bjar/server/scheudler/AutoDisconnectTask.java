package com.osuserverlist.bjar.server.scheudler;

import org.slf4j.Logger;

import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.modules.logger.LoggerFactory;

public class AutoDisconnectTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(AutoDisconnectTask.class);

    public static final long OSU_CLIENT_MIN_PING_INTERVAL = 300_000L * 3; // 5 minutes

    @Override
    public void run() {
        Server server = Server.getInstance();

        server.playerManager.getAll().forEach(player -> {
            if (player.isBot())
                return;
            if (player.getLastPing() != 0 && System.currentTimeMillis() - player.getLastPing() > OSU_CLIENT_MIN_PING_INTERVAL) {
                logger.info("Auto disconnected {} cause of inactivity", player.toString());
                server.playerManager.disconnect(player);
            }
        });
    }
}
