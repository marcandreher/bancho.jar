package com.banchojar.handlers.bancho;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jooq.DSLContext;
import org.slf4j.Logger;

import com.banchojar.App;
import com.banchojar.Player;
import com.banchojar.Server;
import com.banchojar.Server.LoginState;
import com.banchojar.commands.AbstractBanchoCommandHandler;
import com.banchojar.commands.RankMapCommand;
import com.banchojar.db.models.UserRecord;
import com.banchojar.handlers.bancho.LoginHandler.LoginResponse;
import com.banchojar.packets.client.BanchoPacketHandler;
import com.banchojar.packets.client.BanchoPacketReader;
import com.banchojar.packets.client.ClientPackets;
import com.banchojar.packets.client.handlers.LogoutHandler;
import com.banchojar.packets.client.handlers.PingHandler;
import com.banchojar.packets.client.handlers.PresenceRequestHandler;
import com.banchojar.packets.client.handlers.StatsRequestHandler;
import com.banchojar.packets.client.handlers.StatusUpdateHandler;
import com.banchojar.packets.client.handlers.channels.ChannelJoinHandler;
import com.banchojar.packets.client.handlers.channels.ChannelLeaveHandler;
import com.banchojar.packets.client.handlers.channels.SendPublicMessageHandler;
import com.banchojar.packets.client.handlers.mp.JoinLobbyHandler;
import com.banchojar.packets.client.handlers.mp.PartLobbyHandler;
import com.banchojar.packets.server.BanchoChannel;
import com.banchojar.packets.server.BanchoPacketWriter;
import com.banchojar.packets.server.PacketSender;
import com.banchojar.packets.server.ServerPacketHandler;
import com.banchojar.packets.server.ServerPackets;
import com.github.f4b6a3.uuid.UuidCreator;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public class BanchoHandler {

    public static final Logger logger = App.logger;
    public static final Map<ClientPackets, BanchoPacketHandler> packetHandlers = new HashMap<>();
    public static final List<AbstractBanchoCommandHandler> commandHandlers = new ArrayList<>();
    public static void registerRoutes(Javalin app) {
        app.post("/", BanchoHandler::handleRequest);
        packetHandlers.put(ClientPackets.PING, new PingHandler());
        packetHandlers.put(ClientPackets.USER_PRESENCE_REQUEST, new PresenceRequestHandler());
        packetHandlers.put(ClientPackets.USER_STATS_REQUEST, new StatsRequestHandler());
        packetHandlers.put(ClientPackets.CHANNEL_JOIN, new ChannelJoinHandler());
        packetHandlers.put(ClientPackets.LOGOUT, new LogoutHandler());
        packetHandlers.put(ClientPackets.CHANNEL_PART, new ChannelLeaveHandler());
        packetHandlers.put(ClientPackets.JOIN_LOBBY, new JoinLobbyHandler());
        packetHandlers.put(ClientPackets.PART_LOBBY, new PartLobbyHandler());
        packetHandlers.put(ClientPackets.CHANGE_ACTION, new StatusUpdateHandler());
        packetHandlers.put(ClientPackets.SEND_PUBLIC_MESSAGE, new SendPublicMessageHandler());
        
        commandHandlers.add(new RankMapCommand());

        Server.channels.put("#taiko", new BanchoChannel("2", "#taiko", "Taiko channel", true));
     
        DSLContext dsl = Server.dsl;
        // Get player with id 1 from the database
        UserRecord botRecord = dsl.selectFrom("users")
                .where("id = ?", 1)
                .fetchOneInto(UserRecord.class);

        Player bot = new Player(1, true);
        UUID uuid = UuidCreator.getTimeOrderedEpoch();
        bot.setId(botRecord.id());
        bot.setUsername(botRecord.username());
        bot.setLoginState(LoginState.LOGGED_IN);
        bot.setBot(true);
        Server.players.put(uuid.toString(), bot);
    }

    // App.logger.info("[BANCHO] Creating new user: " +
    // loginResponse.getUsername());
    // dsl.insertInto(DSL.table("users"))
    // .columns(DSL.field("username"), DSL.field("password_hash"),
    // DSL.field("email"),
    // DSL.field("country"))
    // .values(loginResponse.getUsername(), loginResponse.getPasswordMd5(),
    // "mail@test", "US")
    // .execute();

    // // Get ID of the newly created user
    // userRecord = dsl.selectFrom("users")
    // .where("username = ?", loginResponse.getUsername())
    // .fetchOneInto(UserRecord.class);
    // loginState = userRecord.id();

    // dsl.insertInto(DSL.table("client_hashes"))
    // .columns(DSL.field("user_id"), DSL.field("executable_hash"),
    // DSL.field("network_interface_hash"), DSL.field("registry_hash"),
    // DSL.field("disk_drive_hash"))
    // .values(userRecord.id(), loginResponse.getExecuteableNameHash(),
    // loginResponse.getNetworkInterfacesHash(), loginResponse.getRegistryKeyHash(),
    // loginResponse.getDiskDriveHash())
    // .execute();

    // for (int mode = 0; mode <= 3; mode++) {
    // dsl.insertInto(DSL.table("users_stats"))
    // .columns(DSL.field("user_id"), DSL.field("mode"), DSL.field("ranked_score"),
    // DSL.field("accuracy"), DSL.field("play_count"), DSL.field("total_score"),
    // DSL.field("global_rank"), DSL.field("pp"))
    // .values(userRecord.id(), mode, 0, 0.0, 0, 0, 0, 0)
    // .execute();
    // }

    private static void handleRequest(Context ctx) throws IOException {
        String osuToken = ctx.header("osu-token");

        if (osuToken == null) {
            // If token is missing, handle login
            handleLogin(ctx);
        } else {
            // Handle other packets
            handlePackets(ctx);
        }
    }

    private static void handleLogin(Context ctx) throws IOException {
        LoginResponse loginResponse = new LoginResponse(ctx);

        if (!loginResponse.isSuccess()) {
            sendLoginFailure(ctx, -1);
            return;
        }

        
        LoginHandler loginHandler = new LoginHandler();

        UserRecord userRecord = loginHandler.getUserRecord(loginResponse);
        PacketSender packetSender = new PacketSender();
        Player player = loginHandler.handleLogin(packetSender, loginResponse, userRecord);

        if(player == null) {
            sendLoginFailure(ctx, -1);
            return;
        }

        HandlePackets(packetSender.getPacketWriter(), player);


        ctx.header("cho-token", loginResponse.getUuid())
                .status(HttpStatus.OK)
                .contentType("application/octet-stream")
                .result(packetSender.toBytes());

    }

    // Helper method to send login failure response
    private static void sendLoginFailure(Context ctx, int loginState) throws IOException {
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

    private static void handlePackets(Context ctx) {
        String osuToken = ctx.header("osu-token");

        PacketSender packetSender = new PacketSender();
        BanchoPacketWriter packetWriter = packetSender.getPacketWriter();

        if (!Server.players.containsKey(osuToken)) {
            packetSender.getPacketWriter().startPacket(ServerPackets.SWITCH_SERVER.getValue());
            packetSender.getPacketWriter().endPacket();
            ctx.status(HttpStatus.OK).result(packetSender.toBytes());
            return;
        }
        Player player = Server.players.get(osuToken);

        if (player.getLoginState() == LoginState.LOGGED_IN) {
            byte[] requestBody = ctx.bodyAsBytes();
            if (requestBody.length > 0) {

                try {
                    BanchoPacketReader reader = new BanchoPacketReader(requestBody, player);

                    while (reader.hasMorePackets()) {
                        try {
                            boolean success = reader.nextPacket();
                            if (!success) {
                                App.logger.warn("Failed to process packet for player ID={}", player.getId());
                            }
                        } catch (IOException e) {
                            App.logger.error("Error reading packet: {}", e.getMessage());
                            // Continue processing other packets even if one fails
                        }
                    }

                } catch (Exception e) {
                    App.logger.error("Unexpected error processing packets: {}", e.getMessage(), e);
                }
            }

        }

        HandlePackets(packetWriter, player);

        // Send the response packets
        byte[] responseBytes = packetSender.toBytes();

        ctx.result(responseBytes)
                .status(HttpStatus.OK)
                .contentType("application/octet-stream");
    }

    public static void HandlePackets(BanchoPacketWriter writer, Player player) {
        Deque<ServerPacketHandler> reversedStack = new ArrayDeque<>();
        while (!player.getPacketStack().isEmpty()) {
            reversedStack.push(player.getPacketStack().pop());
        }

        while (!reversedStack.isEmpty()) {
            ServerPacketHandler packetHandler = reversedStack.pop();
            try {
                packetHandler.handle(null, writer, player);
            } catch (IOException e) {
                App.logger.error("Error sending packet: {}", e.getMessage(), e);
            }
        }
    }

}