package com.osuserverlist.bjar.handlers;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.logger.LoggerFactory;
import com.osuserverlist.bjar.modules.web.engine.Host;
import com.osuserverlist.bjar.modules.web.engine.HttpMethod;
import com.osuserverlist.bjar.modules.web.engine.Path;
import com.osuserverlist.bjar.packets.client.BanchoPacketReader;
import com.osuserverlist.bjar.packets.server.BanchoPacketWriter;
import com.osuserverlist.bjar.packets.server.PacketSender;
import com.osuserverlist.bjar.packets.server.ServerPacketHandler;
import com.osuserverlist.bjar.packets.server.ServerPackets;
import com.osuserverlist.bjar.server.Server;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;

@Host({ "c.", "c4." })
@Path("/")
@HttpMethod("POST")
public class ChoHandler implements Handler {

    private static final Logger logger = LoggerFactory.getLogger(ChoHandler.class);
    private final LoginHandler loginHandler = new LoginHandler();

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        String osuToken = ctx.header("osu-token");

        if (osuToken == null) {
            // If token is missing, handle login
            loginHandler.handleLogin(ctx);
        } else {
            // Handle other packets
            handlePackets(ctx);
        }
    }

     private static void handlePackets(Context ctx) {
        String osuToken = ctx.header("osu-token");

        PacketSender packetSender = new PacketSender();
        BanchoPacketWriter packetWriter = packetSender.getPacketWriter();

        if (Server.getInstance().playerManager.get(osuToken) == null) {
            packetSender.getPacketWriter().startPacket(ServerPackets.SWITCH_SERVER.getValue());
            packetSender.getPacketWriter().endPacket();
            ctx.status(HttpStatus.OK).result(packetSender.toBytes());
            return;
        }
        Player player = Server.getInstance().playerManager.get(osuToken);

        // if (player.getLoginState() == LoginState.LOGGED_IN) {
            byte[] requestBody = ctx.bodyAsBytes();
            if (requestBody.length > 0) {

                try {
                    BanchoPacketReader reader = new BanchoPacketReader(requestBody, player);

                    while (reader.hasMorePackets()) {
                        try {
                            boolean success = reader.nextPacket();
                            if (!success) {
                                logger.warn("Failed to process packet for player ID={}", player.getId());
                            }
                        } catch (IOException e) {
                            logger.error("Error reading packet: {}", e.getMessage());
                            // Continue processing other packets even if one fails
                        }
                    }

                } catch (Exception e) {
                    logger.error("Unexpected error processing packets: {}", e.getMessage(), e);
                }
            // }

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
                logger.error("Error sending packet: {}", e.getMessage(), e);
            }
        }
    }

}
