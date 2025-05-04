package com.banchojar.packets.client.handlers;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.banchojar.Player;
import com.banchojar.Server;
import com.banchojar.packets.BanchoPacket;
import com.banchojar.packets.client.BanchoPacketHandler;
import com.banchojar.packets.client.BanchoPacketReader;
import com.banchojar.packets.server.PacketSender;

public class PresenceRequestHandler implements BanchoPacketHandler {

    public Logger logger = LoggerFactory.getLogger(BanchoPacketHandler.class);
  
    @Override
    public boolean handle(BanchoPacket packet, PacketSender sender, BanchoPacketReader reader, int playerId) throws IOException {
        List<Integer> userIds = reader.readIntList();
        logger.info("Presence request for userIds: " + userIds + " from playerId: " + playerId);

        // Process each user ID in the request
        int processedCount = 0;
        for (int userId : userIds) {
            Player requestedPlayer = Server.players.values().stream()
                .filter(player -> player.getId() == userId)
                .findFirst()
                .orElse(null);

            // If player is found, send their presence and stats
            if (requestedPlayer != null) {
                sender.sendUserPresence(requestedPlayer);
                sender.sendUserStats(requestedPlayer);
                processedCount++;
                logger.info("Sent presence for user: " + userId);
            } else {
                logger.warn("Requested userId " + userId + " not found in server players.");
            }
        }

        // Log success and return true to indicate the request was handled
        logger.info("Processed " + processedCount + " out of " + userIds.size() + " presence requests");
        return true;
    }
}
