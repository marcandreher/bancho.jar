package com.banchojar;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.banchojar.packets.client.BanchoPacketHandler;
import com.banchojar.packets.client.BanchoPacketReader;
import com.banchojar.packets.client.ClientPackets;
import com.banchojar.packets.client.handlers.ChannelJoinHandler;
import com.banchojar.packets.client.handlers.PingHandler;
import com.banchojar.packets.client.handlers.PresenceRequestHandler;
import com.banchojar.packets.client.handlers.StatsRequestHandler;
import com.banchojar.packets.server.PacketSender;
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
        App.logger.info("Login request");

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
        packetSender.sendLoginReply(playerId);
        packetSender.sendPermissions(4);

        // Set initial state
        player.setLoginState(LoginState.CONNECTING);
        App.logger.info("Created player with ID={}, username={}, state={}", 
                       playerId, player.getUsername(), player.getLoginState());

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
        LoginState loginState = player.getLoginState();
        App.logger.info("Processing packets for player: ID={}, username={}, state={}", 
                      player.getId(), player.getUsername(), loginState);

        PacketSender packetSender = new PacketSender();

        // Handle initial connection states
        if (loginState == LoginState.CONNECTING) {
            // Send channel info in the first response
            packetSender.sendChannelInfo("#osu", "Main Channel for osu", 20);
            packetSender.sendChannelInfo("#taiko", "Taiko channel", 5);
            packetSender.sendChannelAutojoinAvailable("#osu");
            packetSender.sendChannelAutojoinAvailable("#taiko");
            packetSender.sendChannelJoinSuccess("#osu");
            packetSender.sendChannelJoinSuccess("#taiko");

            // Update state after sending channel info
            player.setLoginState(LoginState.PRESENCE);
            App.logger.info("Updated player state to PRESENCE: ID={}", player.getId());
        }

        if (loginState == LoginState.PRESENCE) {
            // Send presence data for all players
            App.logger.info("Sending presence/stats for {} players", Server.players.size());
            for (Player p : Server.players.values()) {
                packetSender.sendUserPresence(p);
                packetSender.sendUserStats(p);
            }

            packetSender.sendChannelInfoEnd();
            packetSender.sendNotification("Welcome to bancho.jar!");

            // Update state after sending presence data
            player.setLoginState(LoginState.LOGGED_IN);
            player.setPlayerState(PlayerState.ONLINE);
            App.logger.info("Updated player state to LOGGED_IN: ID={}", player.getId());
        }

        // Process incoming packets for logged-in users
        if (loginState == LoginState.LOGGED_IN) {
            byte[] requestBody = ctx.bodyAsBytes();
            if (requestBody.length > 0) {
                App.logger.debug("Received {} bytes of data for processing", requestBody.length);
                
                try {
                    BanchoPacketReader reader = new BanchoPacketReader(
                        requestBody, packetSender, player.getId());
                    
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
                    
                    App.logger.debug("Processed {} packets successfully", packetsProcessed);
                } catch (Exception e) {
                    App.logger.error("Unexpected error processing packets: {}", e.getMessage(), e);
                }
            }
          
        }

        // Send the response packets
        byte[] responseBytes = packetSender.toBytes();
        App.logger.debug("Sending response with {} bytes", responseBytes.length);
        
        ctx.result(responseBytes)
           .status(HttpStatus.OK)
           .contentType("application/octet-stream");
    }
}