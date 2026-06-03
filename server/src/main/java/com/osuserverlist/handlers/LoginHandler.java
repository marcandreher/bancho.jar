package com.osuserverlist.handlers;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;

import com.osuserverlist.main.Server;
import com.osuserverlist.models.database.DbUser;
import com.osuserverlist.models.engine.LoginResponse;
import com.osuserverlist.models.essentials.BanchoChannel;
import com.osuserverlist.models.essentials.ModeStats;
import com.osuserverlist.models.essentials.Player;
import com.osuserverlist.modules.geo.GeoRegistry;
import com.osuserverlist.modules.geo.GeoResponse;
import com.osuserverlist.modules.logger.LoggerFactory;
import com.osuserverlist.packets.server.BanchoPacketWriter;
import com.osuserverlist.packets.server.PacketSender;
import com.osuserverlist.packets.server.ServerPackets;
import com.osuserverlist.packets.server.handlers.channel.ChannelAutojoinHandler;
import com.osuserverlist.packets.server.handlers.channel.ChannelInfoEndHandler;
import com.osuserverlist.packets.server.handlers.channel.ChannelInfoHandler;
import com.osuserverlist.packets.server.handlers.channel.ChannelJoinSuccessHandler;
import com.osuserverlist.packets.server.handlers.connect.LoginReplyHandler;
import com.osuserverlist.packets.server.handlers.connect.PermissionsHandler;
import com.osuserverlist.packets.server.handlers.connect.SendProtocolVersion;
import com.osuserverlist.packets.server.handlers.user.UserPresenceBundle;
import com.osuserverlist.packets.server.handlers.user.UserPresenceHandler;
import com.osuserverlist.packets.server.handlers.user.UserPresenceSingle;
import com.osuserverlist.packets.server.handlers.user.UserStatsHandler;

import de.marcandreher.fusionkit.core.database.Database;
import de.marcandreher.fusionkit.core.database.MySQL;
import de.marcandreher.fusionkit.core.database.ResultSetMapper;
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

        try (MySQL mysql = Database.getConnection()) {
            ResultSet userRs = mysql.query("SELECT * FROM `users` WHERE `name` = ?", loginResponse.getUsername())
                    .executeQuery();
            if (!userRs.next()) {
                sendLoginFailure(ctx, -1);
                return;
            }

            DbUser dbUser = ResultSetMapper.map(userRs, DbUser.class);
            // TODO BCrypt
            if ((dbUser == null || !dbUser.getPwBcrypt().equals(loginResponse.getPasswordMd5()))) {
                logger.warn("Failed login attempt for user: {} from IP: {}",
                        loginResponse.getUsername(), loginResponse.getIp());
                sendLoginFailure(ctx, -1);
                return;
            }

            LocalDate osuVer = parseOsuVersionDate(loginResponse.getBuildName());
            String osuStream = loginResponse.getBuildName();
            mysql.exec(
                    "INSERT INTO `ingame_logins` (`userid`, `ip`, `osu_ver`, `osu_stream`, `datetime`) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)",
                    dbUser.getId(),
                    loginResponse.getIp(),
                    osuVer.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    osuStream);

            GeoResponse geoLocResponse = GeoRegistry.getProvider().getCountryCode(loginResponse.getIp());

            Player player = new Player(dbUser.getId(), false, loginResponse.getUuid());
            player.setTimezone(Integer.parseInt(loginResponse.getUtcOffset()));
            player.setCountry((short) geoLocResponse.getCountryId());
            player.setLongitude(geoLocResponse.getLongitude());
            player.setLatitude(geoLocResponse.getLatitude());
            player.setDisplayCityLocation(loginResponse.isDisplayCityLocation());
            player.setFriendOnlyDms(loginResponse.isFriendOnlyDms());
            player.setUsername(dbUser.getName());

            player.sendPacket(new SendProtocolVersion());

            player.sendPacket(new LoginReplyHandler(player.getId()));
            player.sendPacket(new PermissionsHandler(4));

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
                player.getModeStats()[i] = modeStats;
            }

            player.sendPacket(new UserPresenceHandler(player.getId()));
            player.sendPacket(new UserStatsHandler(player));

            for (BanchoChannel channel : Server.getInstance().channelManager.getAll()) {
                if (channel.isAutoJoin()) {
                    player.sendPacket(new ChannelAutojoinHandler(channel.getName()));
                    player.sendPacket(new ChannelInfoHandler(channel.getName(), channel.getDescription(),
                            (short) (0 + 1)));
                    player.sendPacket(new ChannelJoinSuccessHandler(channel.getName()));
                    Server.getInstance().channelManager.joinChannel(channel.getName(), player);
                }
            }

            player.sendPacket(new ChannelInfoEndHandler());

            player.sendPacket(new UserPresenceBundle());

            Server.getInstance().playerManager.add(player);

            for (Player p : Server.getInstance().playerManager.getAll()) {
                if (p.getId() == player.getId())
                    continue;
                if (p.isBot()) {
                    player.sendPacket(new UserPresenceHandler(p.getId()));
                    continue;
                }
                p.sendPacket(new UserPresenceSingle(p.getId()));
            }

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
