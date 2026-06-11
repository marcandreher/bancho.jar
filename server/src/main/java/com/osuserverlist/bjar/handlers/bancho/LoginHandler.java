package com.osuserverlist.bjar.handlers.bancho;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.bouncycastle.crypto.generators.OpenBSDBCrypt;
import org.slf4j.Logger;

import com.osuserverlist.bjar.models.config.ServerConfiguration.WelcomeMessage;
import com.osuserverlist.bjar.models.database.UserEntity;
import com.osuserverlist.bjar.models.essentials.ModeStats;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.osu.LoginResponse;
import com.osuserverlist.bjar.models.osu.OsuClientParser;
import com.osuserverlist.bjar.models.osu.Privileges;
import com.osuserverlist.bjar.modules.database.Database;
import com.osuserverlist.bjar.modules.database.MySQL;
import com.osuserverlist.bjar.modules.geo.Country;
import com.osuserverlist.bjar.modules.geo.GeoRegistry;
import com.osuserverlist.bjar.modules.geo.GeoResponse;
import com.osuserverlist.bjar.modules.logger.LoggerFactory;
import com.osuserverlist.bjar.packets.server.BanchoPacketWriter;
import com.osuserverlist.bjar.packets.server.PacketSender;
import com.osuserverlist.bjar.packets.server.ServerPackets;
import com.osuserverlist.bjar.packets.server.handlers.channel.ChannelAutojoinPacket;
import com.osuserverlist.bjar.packets.server.handlers.channel.ChannelInfoEndPacket;
import com.osuserverlist.bjar.packets.server.handlers.channel.ChannelInfoPacket;
import com.osuserverlist.bjar.packets.server.handlers.channel.ChannelJoinSuccessPacket;
import com.osuserverlist.bjar.packets.server.handlers.chat.SendMessagePacket;
import com.osuserverlist.bjar.packets.server.handlers.connect.LoginReplyPacket;
import com.osuserverlist.bjar.packets.server.handlers.connect.MenuIconPacket;
import com.osuserverlist.bjar.packets.server.handlers.connect.PermissionsPacket;
import com.osuserverlist.bjar.packets.server.handlers.connect.ProtocolVersionPacket;
import com.osuserverlist.bjar.packets.server.handlers.user.UserFriendListPacket;
import com.osuserverlist.bjar.packets.server.handlers.user.UserPresenceBundlePacket;
import com.osuserverlist.bjar.packets.server.handlers.user.UserPresencePacket;
import com.osuserverlist.bjar.packets.server.handlers.user.UserPresenceSinglePacket;
import com.osuserverlist.bjar.packets.server.handlers.user.UserStatsPacket;
import com.osuserverlist.bjar.packets.server.handlers.util.NotificationPacket;
import com.osuserverlist.bjar.repos.UserRepository;
import com.osuserverlist.bjar.server.Server;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public class LoginHandler {
    private static final Logger logger = LoggerFactory.getLogger(LoginHandler.class);

    public void handleLogin(Context ctx) throws IOException, SQLException {
        LoginResponse loginResponse = new LoginResponse(ctx);

        if (!loginResponse.isSuccess()) {
            sendLoginFailure(ctx, -1);
            return;
        }

        Server server = Server.getInstance();

        try (MySQL mysql = Database.getConnection()) {
            UserRepository userRepository = new UserRepository(mysql);

            UserEntity userEntity = userRepository.getUserByName(loginResponse.getUsername());
            if (userEntity == null) {
                sendLoginFailure(ctx, -1);
                return;
            }

            boolean passwordMatches = OpenBSDBCrypt.checkPassword(
                    userEntity.getPwBcrypt(),
                    loginResponse.getPasswordMd5().toCharArray());

            if (!passwordMatches) {
                logger.warn("Failed login attempt for user: {} from IP: {}",
                        loginResponse.getUsername(), loginResponse.getIp());
                sendLoginFailure(ctx, -1);
                return;
            }

            LocalDate osuVer = OsuClientParser.parseOsuVersionDate(loginResponse.getBuildName());
            String osuStream = loginResponse.getBuildName();

            userRepository.insertIngameLogin(userEntity.getId(), loginResponse.getIp(),
                    osuVer.format(DateTimeFormatter.ISO_LOCAL_DATE), osuStream);

            GeoResponse geoLocResponse = GeoRegistry.getProvider().getCountryCode(loginResponse.getIp());

            Player existingPlayer = server.playerManager.getById(userEntity.getId());
            if (existingPlayer != null) {
                server.playerManager.forceRemove(existingPlayer);
            }

            // Set country if missing (XX)
            if (userEntity.getCountry().equalsIgnoreCase("XX")) {
                Country country = Country.getById(geoLocResponse.getCountryId());
                userRepository.updateUserCountry(userEntity.getId(), country.name().toLowerCase());
            }

            Player player = new Player(userEntity.getId(), false, loginResponse.getUuid());
            player.setTimezone(Integer.parseInt(loginResponse.getUtcOffset()));
            player.setCountry((short) geoLocResponse.getCountryId());
            player.setLongitude(geoLocResponse.getLongitude());
            player.setLatitude(geoLocResponse.getLatitude());
            player.setDisplayCityLocation(loginResponse.isDisplayCityLocation());
            player.setFriendOnlyDms(loginResponse.isFriendOnlyDms());
            player.setUsername(userEntity.getName());
            player.setServerPrivileges(userEntity.getPriv());
            int newPrivs = Privileges.addPrivilege(userEntity.getPriv(), Privileges.SUPPORTER);
            player.setClientPrivileges(Privileges.toClientPrivileges(newPrivs));

            player.setApiIdent(String.format("%s|%s", player.getUsername(), loginResponse.getPasswordMd5()));

            player.sendPacket(new ProtocolVersionPacket());

            player.sendPacket(new LoginReplyPacket(player.getId()));
            player.sendPacket(new PermissionsPacket(player.getClientPrivileges()));

            for (int i = 0; i <= 8; i++) {
                if (i == 7)
                    continue;

                ResultSet statsRs = mysql
                        .query("SELECT * FROM `stats` WHERE `id` = ? AND `mode` = ?", player.getId(), i).executeQuery();
                if (!statsRs.next()) {
                    logger.warn("Stats not found for player ID={} mode={}", player.getId(), i);
                    continue;
                }

                ModeStats modeStats = ModeStats.fromResultSet(statsRs, i, player);
                player.getModeStats()[i] = modeStats;
            }

            player.sendPacket(new UserPresencePacket(player.getId()));
            player.sendPacket(new UserStatsPacket(player));

            player.sendPacket(new UserFriendListPacket(userRepository.getFriendIds(player.getId())));


            server.channelManager.getAll().forEach(channel -> {
                if ((channel.getReadPriv() > player.getServerPrivileges())) {
                    return; // Skip channels the player doesn't have access to
                }

                if(!channel.isVisible()) {
                    return;
                }

                if (channel.isAutoJoin()) {
                    player.sendPacket(new ChannelAutojoinPacket(channel.getName()));
                    player.sendPacket(new ChannelInfoPacket(channel.getName(), channel.getDescription(), channel.getPlayerCount()));
                    player.sendPacket(new ChannelJoinSuccessPacket(channel.getName()));
                    server.channelManager.forceJoinChannel(channel.getName(), player);
                } else {
                    player.sendPacket(new ChannelInfoPacket(channel.getName(), channel.getDescription(), channel.getPlayerCount()));
                }

            });

            player.sendPacket(new ChannelInfoEndPacket());

            player.sendPacket(new UserPresenceBundlePacket());

            server.playerManager.add(player);

            server.playerManager.getAll().forEach(p -> {
                if (p.getId() == player.getId())
                    return;
                if (p.isBot()) {
                    player.sendPacket(new UserPresencePacket(p.getId()));
                    return;
                }
                p.sendPacket(new UserPresenceSinglePacket(player.getId()));
            });

            server.achievementManager.loadForPlayer(player, mysql);

            WelcomeMessage welcomeConfig = server.config.getWelcomeMessage();
            if (welcomeConfig.isNotificationEnabled()) {
                player.sendPacket(new NotificationPacket(welcomeConfig.getNotificationMessage()));
            }
            if (welcomeConfig.isBotEnabled()) {
                player.sendPacket(new SendMessagePacket(server.botPlayer.getUsername(), welcomeConfig.getBotMessage(),
                        player.getUsername(), server.botPlayer.getId()));
            }

            player.sendPacket(new MenuIconPacket());

            logger.info("User {} logged in successfully from IP: {}", player.toString(), loginResponse.getIp());

            PacketSender packetSender = new PacketSender();

            ChoHandler.HandlePackets(packetSender.getPacketWriter(), player);

            ctx.header("cho-token", loginResponse.getUuid())
                    .status(HttpStatus.OK)
                    .contentType("application/octet-stream")
                    .result(packetSender.toBytes());
        }

    }

    private void sendLoginFailure(Context ctx, int loginState) throws IOException {
        PacketSender packetSender = new PacketSender();
        BanchoPacketWriter writer = packetSender.getPacketWriter();
        writer.startPacket(ServerPackets.LOGIN_REPLY.getValue());
        writer.writeInt(loginState);
        writer.endPacket();

        ctx.status(HttpStatus.OK)
                .header("cho-token", "")
                .contentType("application/octet-stream")
                .result(packetSender.toBytes());
    }
}
