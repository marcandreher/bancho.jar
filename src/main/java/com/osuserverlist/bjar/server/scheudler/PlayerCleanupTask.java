package com.osuserverlist.bjar.server.scheudler;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.osuserverlist.bjar.App;
import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.modules.datastore.Database;
import com.osuserverlist.bjar.modules.datastore.MySQL;
import com.osuserverlist.bjar.repos.UserRepository;

public class PlayerCleanupTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(PlayerCleanupTask.class);

    public static final long OSU_CLIENT_MIN_PING_INTERVAL = 300_000L * 4; // 5 minutes

    @Override
    public void run() {
        Server server = App.server;

        server.playerManager.getAll().forEach(player -> {
            if (player.isBot())
                return;
            if (player.getLastPing() != 0 && System.currentTimeMillis() - player.getLastPing() > OSU_CLIENT_MIN_PING_INTERVAL) {
                logger.info("Auto disconnected {} cause of inactivity", player.toString());
                server.playerManager.disconnect(player);
            }
            if(player.getSilenceEnd() > 0 && player.getSilenceEnd() < (System.currentTimeMillis() / 1000L)) {
                server.playerManager.unsilence(player);

                try (MySQL mysql = Database.getConnection()) {
                    UserRepository userRepo = new UserRepository(mysql);
                    userRepo.updateUserSilence(player.getId(), 0);
                } catch (SQLException e) {
                    logger.error("Failed to update user silence for player {}", player.toString(), e);
                }
            }
        });
    }
}
