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

public class StatsRequestHandler implements BanchoPacketHandler {

    public Logger logger = LoggerFactory.getLogger(BanchoPacketHandler.class);
  
    @Override
    public boolean handle(BanchoPacket packet, PacketSender sender, BanchoPacketReader reader, int playerId) throws IOException {
        List<Integer> userIds = reader.readIntList();
        logger.info("Stats request for userIds: " + userIds + " from playerId: " + playerId);
        
        int processedCount = 0;
        for (int userId : userIds) {

            if(userId == playerId) {
                continue;
            }

            Player requestedPlayer = Server.players.values().stream()
                .filter(player -> player.getId() == userId)
                .findFirst()
                .orElse(null);

            if (requestedPlayer != null) {
                sender.sendUserStats(requestedPlayer);
                processedCount++;
                logger.info("Sent stats for user: " + userId);
            } else {
                logger.warn("Requested userId " + userId + " not found in server players.");
            }
        }

        // Acknowledge the request
        logger.info("Processed " + processedCount + " out of " + userIds.size() + " stats requests");
        return true;
    }
}
