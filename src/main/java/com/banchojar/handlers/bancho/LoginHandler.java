package com.banchojar.handlers.bancho;

import java.sql.Timestamp;
import java.time.Instant;

import org.jooq.impl.DSL;

import com.banchojar.App;
import com.banchojar.Player;
import com.banchojar.Server;
import com.banchojar.Server.LoginState;
import com.banchojar.Server.PlayerState;
import com.banchojar.db.models.UserRecord;
import com.banchojar.packets.server.BanchoChannel;
import com.banchojar.packets.server.PacketSender;
import com.banchojar.packets.server.handlers.ChannelAutojoinHandler;
import com.banchojar.packets.server.handlers.ChannelInfoEndHandler;
import com.banchojar.packets.server.handlers.ChannelInfoHandler;
import com.banchojar.packets.server.handlers.ChannelJoinSuccessHandler;
import com.banchojar.packets.server.handlers.LoginReplyHandler;
import com.banchojar.packets.server.handlers.NotificationHandler;
import com.banchojar.packets.server.handlers.PermissionsHandler;
import com.banchojar.packets.server.handlers.UserPresenceHandler;
import com.banchojar.packets.server.handlers.UserStatsHandler;
import com.github.f4b6a3.uuid.UuidCreator;

import io.javalin.http.Context;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

public class LoginHandler implements ILoginHandler {

    @Getter
    @ToString
    public static class LoginResponse {
        private boolean success = false;

        @Setter
        private String uuid;

        private String ip;

        private String username;
        private String passwordMd5;

        private String buildName;
        private String utcOffset;
        private boolean displayCityLocation;
        private boolean friendOnlyDms;

        private String executeableNameHash;
        private String networkInterfacesHash;
        private String registryKeyHash;
        private String diskDriveHash;

        public LoginResponse(Context ctx) {
            String[] body = ctx.body().split("\n");

            if (body.length < 3) {
                return;
            }

            uuid = UuidCreator.getTimeOrderedEpoch().toString();
            username = body[0];
            passwordMd5 = body[1];
            ip = ctx.ip();

            String[] clientInfo = body[2].split("\\|");

            if (clientInfo.length < 5) {
                return;
            }

            buildName = clientInfo[0];
            utcOffset = clientInfo[1];
            displayCityLocation = Integer.parseInt(clientInfo[2]) == 1;
            friendOnlyDms = Integer.parseInt(clientInfo[4]) == 1;

            String[] clientHashes = clientInfo[3].split(":");
            if (clientHashes.length < 5) {
                return;
            }

            executeableNameHash = clientHashes[0];
            networkInterfacesHash = clientHashes[1];
            registryKeyHash = clientHashes[2];
            diskDriveHash = clientHashes[3];
            success = true;
        }
    }

    @Override
    public Player handleLogin(PacketSender sender, LoginResponse loginResponse, UserRecord dbUser) {
        if ((dbUser == null || !dbUser.password_hash().equals(loginResponse.getPasswordMd5()))) {
            App.logger.warn("[BANCHO] Failed login attempt for user: {} from IP: {}",
                    loginResponse.getUsername(), loginResponse.getIp());
            return null;
        }

        Server.dsl.insertInto(DSL.table("logins"))
                .columns(DSL.field("user_id"), DSL.field("ip"), DSL.field("timestamp"), DSL.field("ver"))
                .values(dbUser.id(), loginResponse.getIp(), Timestamp.from(Instant.now()), loginResponse.getBuildName())
                .execute();

        Player player = new Player(dbUser.id(), false);
        player.setTimezone(Integer.parseInt(loginResponse.getUtcOffset()));
        player.setDisplayCityLocation(loginResponse.isDisplayCityLocation());
        player.setFriendOnlyDms(loginResponse.isFriendOnlyDms());
        player.setUsername(dbUser.username());

        Server.players.put(loginResponse.getUuid(), player);

        App.logger.info("[BANCHO] Player (" + player.getId() + ") <" + loginResponse.getUsername()
                + "> has logged in.");

        player.addPacketToStack(new LoginReplyHandler(player.getId()));
        player.addPacketToStack(new PermissionsHandler(4));

        player.addPacketToStack(new ChannelAutojoinHandler("#osu"));
        player.addPacketToStack(new ChannelJoinSuccessHandler("#osu"));

        for (BanchoChannel channel : Server.channels.values()) {
            if (channel.isAutoJoin()) {
                player.addPacketToStack(new ChannelAutojoinHandler(channel.getName()));
            }

            player.addPacketToStack(new ChannelInfoHandler(channel.getName(), channel.getDescription(),
                    (short) (channel.getPlayerCount() + 1)));
            if (channel.isAutoJoin()) {
                player.addPacketToStack(new ChannelJoinSuccessHandler(channel.getName()));
            }
        }

        for(int i = 0; i <= 3; i++) {
            final int modeIndex = i;
            Server.dsl.selectFrom("users_stats").where("user_id = ?", player.getId())
            .and(DSL.field("mode").eq(modeIndex)).fetchMaps().forEach(row -> {
                player.getModeStats()[modeIndex]
                        .setAccuracy(((Double) row.get("accuracy")).floatValue());
                player.getModeStats()[modeIndex].setPlayCount((int) row.get("play_count"));
                player.getModeStats()[modeIndex].setTotalScore((long) row.get("total_score"));
                player.getModeStats()[modeIndex].setRankedScore((long) row.get("ranked_score"));

                player.getModeStats()[modeIndex].setPp((short) ((int) (long) row.get("pp")));
            });
        }

    

        player.addPacketToStack(new ChannelInfoEndHandler());

        for (Player p : Server.players.values()) {
            player.addPacketToStack(new UserPresenceHandler(p.getId()));
            if (p.isBot()) {
                continue;
            }
            player.addPacketToStack(new UserStatsHandler(p.getId()));
        }

        player.setLoginState(LoginState.LOGGED_IN);
        player.setPlayerState(PlayerState.ONLINE);
        player.addPacketToStack(new NotificationHandler("Welcome to bancho.jar!"));

        return player;
    }

    @Override
    public UserRecord getUserRecord(LoginResponse loginResponse) {
        return Server.dsl.selectFrom("users")
                .where("username = ?", loginResponse.getUsername())
                .fetchOneInto(UserRecord.class);
    }

}
