package com.osuserverlist.bjar.handlers;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bouncycastle.crypto.generators.OpenBSDBCrypt;
import org.slf4j.Logger;

import com.osuserverlist.bjar.models.config.ServerConfiguration.WelcomeMessage;
import com.osuserverlist.bjar.models.database.UserEntity;
import com.osuserverlist.bjar.models.essentials.BanchoChannel;
import com.osuserverlist.bjar.models.essentials.ModeStats;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.osu.LoginResponse;
import com.osuserverlist.bjar.models.osu.Privileges;
import com.osuserverlist.bjar.modules.database.Database;
import com.osuserverlist.bjar.modules.database.MySQL;
import com.osuserverlist.bjar.modules.geo.Country;
import com.osuserverlist.bjar.modules.geo.GeoRegistry;
import com.osuserverlist.bjar.modules.geo.GeoResponse;
import com.osuserverlist.bjar.modules.logger.LoggerFactory;
import com.osuserverlist.bjar.modules.redis.Redis;
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

            LocalDate osuVer = parseOsuVersionDate(loginResponse.getBuildName());
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
            player.setRealPrivileges(userEntity.getPriv());
            int newPrivs = Privileges.addPrivilege(userEntity.getPriv(), Privileges.SUPPORTER);
            player.setPrivileges(Privileges.toClientPrivileges(newPrivs));

            player.setApiIdent(String.format("%s|%s", player.getUsername(), loginResponse.getPasswordMd5()));

            player.sendPacket(new ProtocolVersionPacket());

            player.sendPacket(new LoginReplyPacket(player.getId()));
            player.sendPacket(new PermissionsPacket(player.getPrivileges()));
            
            for (int i = 0; i <= 8; i++) {
                if (i == 7)
                    continue;

                ResultSet statsRs = mysql
                        .query("SELECT * FROM `stats` WHERE `id` = ? AND `mode` = ?", player.getId(), i).executeQuery();
                if (!statsRs.next()) {
                    logger.warn("Stats not found for player ID={} mode={}", player.getId(), i);
                    continue;
                }

                ModeStats modeStats = new ModeStats();
                modeStats.setPlayCount(statsRs.getInt("plays"));
                modeStats.setTotalScore(statsRs.getLong("tscore"));
                modeStats.setRankedScore(statsRs.getLong("rscore"));
                modeStats.setAccuracy(statsRs.getFloat("acc"));
                modeStats.setMaxCombo(statsRs.getInt("max_combo"));
                modeStats.setPp(statsRs.getShort("pp"));
                modeStats.setTotalHits(statsRs.getInt("total_hits"));

                Long rank = Redis.getClient().zrevrank(
                        "bjar:leaderboard:" + i,
                        String.valueOf(player.getId()));

                modeStats.setGlobalRank(rank != null ? Math.toIntExact(rank) + 1 : 0);
                player.getModeStats()[i] = modeStats;
            }

            player.sendPacket(new UserPresencePacket(player.getId()));
            player.sendPacket(new UserStatsPacket(player));

            player.sendPacket(new UserFriendListPacket(userRepository.getFriendIds(player.getId())));

            for (BanchoChannel channel : server.channelManager.getAll()) {
                if ((channel.getReadPriv() > player.getRealPrivileges())) {
                    continue; // Skip channels the player doesn't have access to
                }
                if (channel.isAutoJoin()) {
                    player.sendPacket(new ChannelAutojoinPacket(channel.getName()));
                    player.sendPacket(new ChannelInfoPacket(channel.getName(), channel.getDescription(),
                            (short) (channel.getPlayerCount() + 1)));
                    player.sendPacket(new ChannelJoinSuccessPacket(channel.getName()));
                    server.channelManager.forceJoinChannel(channel.getName(), player);
                } else {
                    if(channel.getName().equals("#lobby")) continue;
                    player.sendPacket(new ChannelInfoPacket(channel.getName(), channel.getDescription(),
                            (short) (channel.getPlayerCount() + 1)));
                }
            }

            player.sendPacket(new ChannelInfoEndPacket());

            player.sendPacket(new UserPresenceBundlePacket());

            server.playerManager.add(player);

            for (Player p : server.playerManager.getAll()) {
                if (p.getId() == player.getId())
                    continue;
                if (p.isBot()) {
                    player.sendPacket(new UserPresencePacket(p.getId()));
                    continue;
                }
                p.sendPacket(new UserPresenceSinglePacket(player.getId()));
            }

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

    private LocalDate parseOsuVersionDate(String buildName) {
        if (buildName == null) {
            return LocalDate.now();
        }

        Matcher matcher = Pattern.compile("(\\d{4})(\\d{2})(\\d{2})").matcher(buildName);
        if (!matcher.find()) {
            return LocalDate.now();
        }

        int year = Integer.parseInt(matcher.group(1));
        int month = Integer.parseInt(matcher.group(2));
        int day = Integer.parseInt(matcher.group(3));
        try {
            return LocalDate.of(year, month, day);
        } catch (RuntimeException ex) {
            return LocalDate.now();
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
