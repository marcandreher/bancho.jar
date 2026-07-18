package com.osuserverlist.bjar.server.scheudler;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.osuserverlist.bjar.App;
import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.osu.Privileges;
import com.osuserverlist.bjar.modules.datastore.Database;
import com.osuserverlist.bjar.modules.datastore.MySQL;
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
        if (player.getSilenceEnd() <= nowSeconds()) {
            return;
        }

        try (MySQL mysql = Database.getConnection()) {
            UserRepository repo = new UserRepository(mysql);
            repo.updateUserSilence(player.getId(), 0);
            server.playerManager.unsilence(player);
        } catch (SQLException e) {
            logger.error("Failed to update silence for {}", player, e);
        }
    }

    private void expireSupporter(Server server, Player player) {
        if (player.getDonorEnd() <= nowSeconds()) {
            return;
        }

        server.playerManager.removePriv(player, Privileges.SUPPORTER);

        try (MySQL mysql = Database.getConnection()) {
            UserRepository repo = new UserRepository(mysql);
            repo.updateUserPrivileges(player.getId(), player.getServerPrivileges());
            repo.updateUserDonor(player.getId(), 0);
        } catch (SQLException e) {
            logger.error("Failed to update donor for {}", player, e);
        }
    }

    private long nowSeconds() {
        return System.currentTimeMillis() / 1000L;
    }
}
