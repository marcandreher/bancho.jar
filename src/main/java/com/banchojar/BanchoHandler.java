package com.banchojar;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.banchojar.packets.BanchoPacket;
import com.banchojar.packets.client.BanchoPacketHandler;
import com.banchojar.packets.client.BanchoPacketReader;
import com.banchojar.packets.client.ClientPackets;
import com.banchojar.packets.client.handlers.ChannelJoinHandler;
import com.banchojar.packets.client.handlers.LogoutHandler;
import com.banchojar.packets.client.handlers.PingHandler;
import com.banchojar.packets.client.handlers.PresenceRequestHandler;
import com.banchojar.packets.client.handlers.StatsRequestHandler;
import com.banchojar.packets.server.BanchoChannel;
import com.banchojar.packets.server.BanchoPacketWriter;
import com.banchojar.packets.server.PacketSender;
import com.banchojar.packets.server.ServerPacketHandler;
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

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public class BanchoHandler {

    public static final Map<ClientPackets, BanchoPacketHandler> packetHandlers = new HashMap<>();

    public static void registerRoutes(Javalin app) {
        app.post("/", BanchoHandler::handleRequest);
        packetHandlers.put(ClientPackets.PING, new PingHandler());
        packetHandlers.put(ClientPackets.USER_PRESENCE_REQUEST, new PresenceRequestHandler());
        packetHandlers.put(ClientPackets.USER_STATS_REQUEST, new StatsRequestHandler());
        packetHandlers.put(ClientPackets.CHANNEL_JOIN, new ChannelJoinHandler());
        packetHandlers.put(ClientPackets.LOGOUT, new LogoutHandler());

        Server.channels.put("#lobby", new BanchoChannel("1", "#lobby", "Main Lobby", true));
        Server.channels.put("#taiko", new BanchoChannel("2", "#taiko", "Taiko channel", false));
        Server.channels.put("#osu", new BanchoChannel("3", "#osu", "Osu channel", true));

        Player bot = new Player(1);
        UUID uuid = UuidCreator.getTimeOrderedEpoch();
        bot.setId(1);
        bot.setLoginState(LoginState.LOGGED_IN);
        Server.players.put(uuid.toString(), bot);
    }

    private static void handleRequest(Context ctx) {
        String osuToken = ctx.header("osu-token");

        if (osuToken == null) {
            // If token is missing, handle login
            handleLogin(ctx);
        } else {
            // Handle other packets
            handlePackets(ctx);
        }
    }

    private static void handleLogin(Context ctx) {
        UUID uuid = UuidCreator.getTimeOrderedEpoch();
        String osuToken = uuid.toString();
        App.logger.info("Login from " + ctx.ip() + " with token: " + osuToken);

        // Create new player with random ID between 1000-9999 to avoid collisions
        int playerId = 1000 + (int)(Math.random() * 9000);
        Player player = new Player(playerId);
        player.setUsername("User" + playerId); // Set a unique username
        Server.players.put(osuToken, player);

        // Set up the response packets
        PacketSender packetSender = new PacketSender();
        player.addPacketToStack(new LoginReplyHandler(playerId));
        player.addPacketToStack(new PermissionsHandler(4));

        // Set initial state
        player.setLoginState(LoginState.CONNECTING);
        App.logger.info("Created player with ID={}, username={}, state={}", 
                       playerId, player.getUsername(), player.getLoginState());

        HandlePackets(packetSender.getPacketWriter(), player);

        ctx.header("cho-token", osuToken)
           .status(HttpStatus.OK)
           .contentType("application/octet-stream")
           .result(packetSender.toBytes());
    }

    private static void handlePackets(Context ctx) {
        String osuToken = ctx.header("osu-token");


        // Validate player exists
        if (!Server.players.containsKey(osuToken)) {
            App.logger.warn("Player with token {} is not logged in, ignoring packets", osuToken);
            ctx.status(HttpStatus.UNAUTHORIZED).result("Unauthorized");
            return;
        }

        

        Player player = Server.players.get(osuToken);

        PacketSender packetSender = new PacketSender();
        BanchoPacketWriter packetWriter = packetSender.getPacketWriter();
        
        LoginState loginState = player.getLoginState();
        App.logger.info("Processing packets for player: ID={}, username={}, state={}", 
                      player.getId(), player.getUsername(), loginState);

       
        // Handle initial connection states
        if (loginState == LoginState.CONNECTING) {

            for (BanchoChannel channel : Server.channels.values()) {
                
                if(channel.isAutoJoin()) {
                    player.addPacketToStack(new ChannelAutojoinHandler(channel.getName()));
                }

                player.addPacketToStack(new ChannelInfoHandler(channel.getName(), channel.getDescription(), channel.getPlayers().size()));

                player.addPacketToStack(new ChannelJoinSuccessHandler(channel.getName()));
            }

            // Update state after sending channel info
            player.setLoginState(LoginState.PRESENCE);
        }

        if (loginState == LoginState.PRESENCE) {
            // Send presence data for all players
            for (Player p : Server.players.values()) {
                player.addPacketToStack(new UserPresenceHandler(p.getId()));
                player.addPacketToStack(new UserStatsHandler(p.getId()));
            }

            player.addPacketToStack(new ChannelInfoEndHandler());
            player.addPacketToStack(new NotificationHandler("Welcome to bancho.jar!"));
  
            // Update state after sending presence data
            player.setLoginState(LoginState.LOGGED_IN);
            player.setPlayerState(PlayerState.ONLINE);

        }


        // Process incoming packets for logged-in users
        if (loginState == LoginState.LOGGED_IN) {
            byte[] requestBody = ctx.bodyAsBytes();
            if (requestBody.length > 0) {

                try {
                    BanchoPacketReader reader = new BanchoPacketReader(requestBody, player);
                    
                    // Process all packets in the request
                    int packetsProcessed = 0;
                    while (reader.hasMorePackets()) {
                        try {
                            boolean success = reader.nextPacket();
                            if (success) {
                                packetsProcessed++;
                            } else {
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
        // Send response packets
        while(!player.getPacketStack().isEmpty()) {
            ServerPacketHandler packetHandler = player.getPacketStack().pop();
            try {
                
                packetHandler.handle(null, writer, player);
            } catch (IOException e) {
                App.logger.error("Error sending packet: {}", e.getMessage(), e);
            }
        }
    }
}