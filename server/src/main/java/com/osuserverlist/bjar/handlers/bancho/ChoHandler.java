package com.osuserverlist.bjar.handlers.bancho;

import java.io.IOException;
import java.util.Deque;
import java.util.Iterator;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.logger.LoggerFactory;
import com.osuserverlist.bjar.modules.web.engine.Host;
import com.osuserverlist.bjar.modules.web.engine.HttpMethod;
import com.osuserverlist.bjar.modules.web.engine.Path;
import com.osuserverlist.bjar.packets.client.engine.BanchoPacketReader;
import com.osuserverlist.bjar.packets.server.BanchoPacketWriter;
import com.osuserverlist.bjar.packets.server.PacketSender;
import com.osuserverlist.bjar.packets.server.ServerPacketHandler;
import com.osuserverlist.bjar.packets.server.ServerPackets;

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
            loginHandler.handleLogin(ctx);
            return;
        }

        handlePackets(ctx, osuToken);
    }

    private static void handlePackets(Context ctx, String osuToken) {
        PacketSender packetSender = new PacketSender();
        BanchoPacketWriter packetWriter = packetSender.getPacketWriter();

        Server server = Server.getInstance();

        Player player = server.playerManager.get(osuToken);

        if (player == null) {
            packetWriter.startPacket(ServerPackets.SWITCH_SERVER.getValue());
            packetWriter.endPacket();
            ctx.status(HttpStatus.OK).result(packetSender.toBytes());
            return;
        }

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
        }

        HandlePackets(packetWriter, player);

        // Send the response packets
        byte[] responseBytes = packetSender.toBytes();

        ctx.result(responseBytes)
                .status(HttpStatus.OK)
                .contentType("application/octet-stream");
    }

    public static void HandlePackets(BanchoPacketWriter writer, Player player) {
        Deque<ServerPacketHandler> packetStack = player.getPacketStack();

        if (packetStack.isEmpty()) {
            return;
        }

        Iterator<ServerPacketHandler> it = packetStack.descendingIterator();
        while (it.hasNext()) {
            it.next().handle(null, writer, player);
        }

        packetStack.clear();
    }

}