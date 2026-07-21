package com.osuserverlist.bjar.server.scheudler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.osuserverlist.bjar.App;
import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.database.UserEntity;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.osu.Privileges;
import com.osuserverlist.bjar.repos.UserRepository;

public class PlayerCleanupTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(PlayerCleanupTask.class);

    private static final long OSU_CLIENT_MIN_PING_INTERVAL = 300_000L * 5;

    @Override
    public void run() {
        Server server = App.server;

        server.playerManager.getAll().forEach(player -> {
            if (player.isBot()) {
                return;
            }

            disconnectInactivePlayer(server, player);
            expireSilence(server, player);
            expireSupporter(server, player);
        });
    }

    private void disconnectInactivePlayer(Server server, Player player) {
        long lastPing = player.getLastPing();

        if (lastPing == 0) {
            return;
        }

        if (System.currentTimeMillis() - lastPing > OSU_CLIENT_MIN_PING_INTERVAL) {
            logger.info("Auto disconnected {} because of inactivity", player);
            server.playerManager.disconnect(player);
        }
    }

    private void expireSilence(Server server, Player player) {
        if (player.getSilenceEnd() == 0 || nowSeconds() <= player.getSilenceEnd()) {
            return;
        }

        UserEntity entity = player.getEntity();
        entity.setSilenceEnd(0);
        UserRepository.save(entity);

        server.playerManager.unsilence(player);

        logger.info("Silence expired for {}", player);
    }

    private void expireSupporter(Server server, Player player) {
        if (player.getDonorEnd() == 0 || nowSeconds() <= player.getDonorEnd()) {
            return;
        }

        server.playerManager.removePriv(player, Privileges.SUPPORTER);

        UserEntity entity = player.getEntity();
        entity.setDonorEnd(0);
        UserRepository.save(entity);

        logger.info("Supporter expired for {}", player);
    }

    private long nowSeconds() {
        return System.currentTimeMillis() / 1000L;
    }
}
