package com.osuserverlist.bjar.handlers.cho;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.crypto.generators.OpenBSDBCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.osuserverlist.bjar.App;
import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.ConfigModels.ServerConfiguration.WelcomeMessage;
import com.osuserverlist.bjar.models.database.UserEntity;
import com.osuserverlist.bjar.models.essentials.ModeStats;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.osu.LoginResponse;
import com.osuserverlist.bjar.models.osu.Privileges;
import com.osuserverlist.bjar.modules.datastore.Database;
import com.osuserverlist.bjar.modules.datastore.MySQL;
import com.osuserverlist.bjar.modules.main.Application.BuildInfo;
import com.osuserverlist.bjar.modules.main.GeoLocation;
import com.osuserverlist.bjar.modules.main.GeoLocation.Country;
import com.osuserverlist.bjar.modules.main.GeoLocation.GeoResponse;
import com.osuserverlist.bjar.modules.packets.BanchoPacketWriter;
import com.osuserverlist.bjar.modules.packets.ServerPacketEngine;
import com.osuserverlist.bjar.packets.server.ChatServerPackets.ChannelAutojoinPacket;
import com.osuserverlist.bjar.packets.server.ChatServerPackets.ChannelInfoEndPacket;
import com.osuserverlist.bjar.packets.server.ChatServerPackets.ChannelInfoPacket;
import com.osuserverlist.bjar.packets.server.ChatServerPackets.ChannelJoinSuccessPacket;
import com.osuserverlist.bjar.packets.server.ChatServerPackets.SendMessagePacket;
import com.osuserverlist.bjar.packets.server.LoginServerPackets.LoginReplyPacket;
import com.osuserverlist.bjar.packets.server.LoginServerPackets.MenuIconPacket;
import com.osuserverlist.bjar.packets.server.LoginServerPackets.PrivilegesPacket;
import com.osuserverlist.bjar.packets.server.LoginServerPackets.ProtocolVersionPacket;
import com.osuserverlist.bjar.packets.server.LoginServerPackets.SilenceInfoPacket;
import com.osuserverlist.bjar.packets.server.UserServerPackets.AccountRestrictedPacket;
import com.osuserverlist.bjar.packets.server.UserServerPackets.FriendsListPacket;
import com.osuserverlist.bjar.packets.server.UserServerPackets.UserPresenceBundlePacket;
import com.osuserverlist.bjar.packets.server.UserServerPackets.UserPresencePacket;
import com.osuserverlist.bjar.packets.server.UserServerPackets.UserStatsPacket;
import com.osuserverlist.bjar.packets.server.UtilServerPackets.NotificationPacket;
import com.osuserverlist.bjar.repos.UserRepository;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

/**
 * Handles the initial Bancho login request (no {@code osu-token} header):
 * authentication, player construction, stat/channel/friend bootstrapping,
 * and all the one-time packets sent on connect.
 */
public class LoginHandler {

    private static final Logger logger = LoggerFactory.getLogger(LoginHandler.class);

    private static final String RESTRICTED_MSG = """
            Your account is currently in restricted mode.
            If you believe this is a mistake, or have waited a period
            greater than 3 months, you may appeal via discord.""";

    private static final int[] STATS_MODES = { 0, 1, 2, 3, 4, 5, 6, 8 };

    private static final int LOGIN_FAILED = -1;

    public void handle(Context ctx) throws IOException, SQLException {
        LoginResponse loginResponse = LoginResponse.parse(ctx);

        if (!loginResponse.isSuccess()) {
            sendLoginFailure(ctx, LOGIN_FAILED);
            return;
        }

        // TODO: fix issue where client is sending login request twice even after login to c4 or any other subdom

        Server server = App.server;

        try (MySQL mysql = Database.getConnection()) {
            UserRepository userRepository = new UserRepository(mysql);

            UserEntity userEntity = authenticate(userRepository, loginResponse);
            if (userEntity == null) {
                sendLoginFailure(ctx, LOGIN_FAILED);
                return;
            }

            GeoResponse geoLocResponse = resolveGeoLocation(userRepository, userEntity, loginResponse);

            disconnectExistingSession(server, userEntity);

            Player player = buildPlayer(userEntity, loginResponse, geoLocResponse);

            player.sendPacket(new ProtocolVersionPacket());
            player.sendPacket(new LoginReplyPacket(player.getId()));
            player.sendPacket(new PrivilegesPacket(player.getClientPrivileges()));

            loadPlayerStats(mysql, player);

            player.sendPacket(new UserPresencePacket(player));
            player.sendPacket(new UserStatsPacket(player));
            player.sendPacket(new FriendsListPacket(userRepository.getFriendIds(player.getId())));

            for (int blockId : userRepository.getBlockedIds(player.getId())) {
                player.getBlocks().add(blockId);
            }

            joinAvailableChannels(server, player);

            player.sendPacket(new ChannelInfoEndPacket());
            player.sendPacket(new UserPresenceBundlePacket());

            server.playerManager.add(player);

            notifyOtherPlayers(server, player);

            sendWelcomeMessages(server, player);
            sendRestrictionNoticeIfNeeded(server, player);
            player.sendPacket(new MenuIconPacket());

            sendSilenceInfoIfNeeded(server, player);

            logger.info("User {} logged in successfully from IP: {}", player, loginResponse.getIp());

            scheduleBackgroundLoginTasks(server, player, userEntity, loginResponse);

            BanchoPacketWriter writer = new BanchoPacketWriter();
            ChoHandler.handlePendingPackets(writer, player);

            ctx.header("cho-token", loginResponse.getUuid())
                    .status(HttpStatus.OK)
                    .contentType("application/octet-stream")
                    .result(writer.getPackets());
        }
    }

    /**
     * Looks up the user and verifies their password. Returns {@code null}
     * (and logs a warning) if either step fails.
     */
    private UserEntity authenticate(UserRepository userRepository, LoginResponse loginResponse) throws SQLException {
        UserEntity userEntity = userRepository.getUserByName(loginResponse.getUsername());
        if (userEntity == null) {
            return null;
        }

        boolean passwordMatches = OpenBSDBCrypt.checkPassword(
                userEntity.getPwBcrypt(),
                loginResponse.getPasswordMd5().toCharArray());

        if (!passwordMatches) {
            logger.warn("Failed login attempt for user: {} from IP: {}",
                    loginResponse.getUsername(), loginResponse.getIp());
            return null;
        }

        return userEntity;
    }

    /**
     * Resolves the player's geo-location and backfills the user's stored
     * country if it hasn't been set yet (i.e. is still {@code XX}).
     */
    private GeoResponse resolveGeoLocation(UserRepository userRepository, UserEntity userEntity,
            LoginResponse loginResponse) throws SQLException {
        GeoResponse geoLocResponse = GeoLocation.provider.getCountryCode(loginResponse.getIp());

        if (userEntity.getCountry().equalsIgnoreCase("XX")) {
            Country country = Country.getById(geoLocResponse.getCountryId());
            userRepository.updateUserCountry(userEntity.getId(), country.name().toLowerCase());
        }

        return geoLocResponse;
    }

    private void disconnectExistingSession(Server server, UserEntity userEntity) {
        Player existingPlayer = server.playerManager.getById(userEntity.getId());
        if (existingPlayer != null) {
            server.playerManager.disconnect(existingPlayer);
        }
    }

    private Player buildPlayer(UserEntity userEntity, LoginResponse loginResponse, GeoResponse geoLocResponse) {
        Player player = new Player(userEntity.getId(), false, loginResponse.getUuid());

        player.setTimezone(Integer.parseInt(loginResponse.getUtcOffset()));
        player.setCountry((short) geoLocResponse.getCountryId());
        player.setLongitude(geoLocResponse.getLongitude());
        player.setLatitude(geoLocResponse.getLatitude());
        player.setDisplayCityLocation(loginResponse.isDisplayCityLocation());
        player.setFriendOnlyDms(loginResponse.isFriendOnlyDms());
        player.setUsername(userEntity.getName());
        player.setServerPrivileges(userEntity.getPriv());

        if(!Privileges.hasAny(userEntity.getPriv(), Privileges.VERIFIED)) {
            player.setServerPrivileges(userEntity.getPriv() | Privileges.VERIFIED.getValue());
            updatePrivsSoon(player);
        }

        player.setSilenceEnd(userEntity.getSilenceEnd());
        player.setDonorEnd(userEntity.getDonorEnd());

        int clientPrivs = Privileges.addPrivilege(userEntity.getPriv(), Privileges.SUPPORTER);
        player.setClientPrivileges(Privileges.toClientPrivileges(clientPrivs));

        player.setApiIdent(String.format("%s|%s", player.getUsername(), loginResponse.getPasswordMd5()));

        return player;
    }

    private void updatePrivsSoon(Player player) {
        App.server.executor.submit(() -> {
            try (MySQL mysql = Database.getConnection()) {
                UserRepository userRepo = new UserRepository(mysql);
                userRepo.updateUserPrivileges(player.getId(), player.getServerPrivileges());
            } catch (SQLException e) {
                logger.error("Failed to update privileges for {}", player, e);
            }
        });
    }

    private void sendSilenceInfoIfNeeded(Server server, Player player) {
        if (player.getSilenceEnd() > 0) {
            if (player.getSilenceEnd() > (System.currentTimeMillis() / 1000L)) {
                int silenceSecondsRemaining = (int) (player.getSilenceEnd() - (System.currentTimeMillis() / 1000L));
                player.sendPacket(new SilenceInfoPacket(silenceSecondsRemaining));
                player.sendPacket(new SendMessagePacket(server.botPlayer.getUsername(), "You are currently silenced.", player.getUsername(), server.botPlayer.getId()));
            } else {
                player.sendPacket(new SilenceInfoPacket(0));
            }
        }
    }

    private void sendRestrictionNoticeIfNeeded(Server server, Player player) {
        if (Privileges.hasAny(player.getServerPrivileges(), Privileges.UNRESTRICTED)) {
            return;
        }
        player.setRestricted(true);

        player.sendPacket(new AccountRestrictedPacket());
        player.sendPacket(new SendMessagePacket(server.botPlayer.getUsername(), RESTRICTED_MSG,
                player.getUsername(), server.botPlayer.getId()));
    }

    /**
     * Queues non-critical, response-blocking work (login logging, achievement
     * loading) to run after the login response has already been sent to the
     * client, since neither is needed before the client can start playing.
     */
    private void scheduleBackgroundLoginTasks(Server server, Player player, UserEntity userEntity,
            LoginResponse loginResponse) {
        server.executor.submit(() -> {
            try (MySQL con = Database.getConnection()) {
                UserRepository userRepo = new UserRepository(con);
                userRepo.insertIngameLogin(userEntity.getId(), loginResponse.getIp(),
                        LoginResponse.parseOsuVersionDate(loginResponse.getBuildName())
                                .format(DateTimeFormatter.ISO_LOCAL_DATE),
                        loginResponse.getBuildName());

                server.achievementManager.loadForPlayer(player, con);
            } catch (SQLException e) {
                logger.error("Error running background login tasks for user {}: {}", player, e.getMessage());
            }
        });
    }

    private void loadPlayerStats(MySQL mysql, Player player) throws SQLException {
        ResultSet statsRs = mysql
                .query("SELECT * FROM `stats` WHERE `id` = ? AND `mode` IN (0,1,2,3,4,5,6,8)", player.getId())
                .executeQuery();

        boolean[] statsFound = new boolean[9];

        while (statsRs.next()) {
            int mode = statsRs.getInt("mode");
            ModeStats modeStats = ModeStats.fromResultSet(statsRs, mode, player);
            player.getModeStats()[mode] = modeStats;
            statsFound[mode] = true;
        }

        for (int mode : STATS_MODES) {
            if (!statsFound[mode]) {
                logger.warn("Stats not found for player ID={} mode={}", player.getId(), mode);
            }
        }
    }

    private void joinAvailableChannels(Server server, Player player) {
        server.channelManager.getAll().forEach(channel -> {
            if (channel.getReadPriv() > player.getServerPrivileges() || !channel.isVisible()) {
                return;
            }

            if (channel.isAutoJoin()) {
                player.sendPacket(new ChannelAutojoinPacket(channel.getName()));
                player.sendPacket(new ChannelInfoPacket(channel.getName(), channel.getDescription(),
                        channel.getPlayerCount() + 1));
                player.sendPacket(new ChannelJoinSuccessPacket(channel.getName()));
                server.channelManager.joinChannel(channel.getName(), player);
            } else {
                player.sendPacket(new ChannelInfoPacket(channel.getName(), channel.getDescription(),
                        channel.getPlayerCount()));
            }
        });
    }

    /**
     * Sends the newly-connected player's presence to every other online,
     * non-bot player, off the request thread.
     */
    private void notifyOtherPlayers(Server server, Player player) {
        List<Player> toNotify = new ArrayList<>();

        server.playerManager.getAll().forEach(p -> {
            if (p.getId() == player.getId()) {
                return;
            }
            if (p.isBot()) {
                player.sendPacket(new UserPresencePacket(p));
                return;
            }
            toNotify.add(p);
        });

        if (toNotify.isEmpty()) {
            return;
        }

        server.executor.submit(() -> {
            for (Player p : toNotify) {
                p.sendPacket(new UserPresencePacket(player));
            }
        });
    }

    private void sendWelcomeMessages(Server server, Player player) {
        WelcomeMessage welcomeConfig = server.config.getWelcomeMessage();

        if (welcomeConfig.isNotificationEnabled()) {
            String notificationMessage = welcomeConfig.getNotificationMessage()
                    .replace("%version%", BuildInfo.VERSION);
            player.sendPacket(new NotificationPacket(notificationMessage));
        }

        if (welcomeConfig.isBotEnabled()) {
            String botMessage = welcomeConfig.getBotMessage().replace("%version%", BuildInfo.VERSION);
            player.sendPacket(new SendMessagePacket(server.botPlayer.getUsername(), botMessage,
                    player.getUsername(), server.botPlayer.getId()));
        }
    }

    private void sendLoginFailure(Context ctx, int loginState) throws IOException {
        BanchoPacketWriter writer = new BanchoPacketWriter();
        writer.startPacket(ServerPacketEngine.ServerPackets.LOGIN_REPLY);
        writer.writeInt(loginState);
        writer.endPacket();

        ctx.status(HttpStatus.OK)
                .header("cho-token", "")
                .contentType("application/octet-stream")
                .result(writer.getPackets());
    }
}