package com.banchojar.packets.server.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.banchojar.Player;
import com.banchojar.Server;
import com.banchojar.packets.BanchoPacket;
import com.banchojar.packets.server.BanchoPacketWriter;
import com.banchojar.packets.server.ServerPacketHandler;
import com.banchojar.packets.server.ServerPackets;

public class UserStatsHandler implements ServerPacketHandler {

    public Logger logger = LoggerFactory.getLogger(ServerPacketHandler.class);

    final ServerPackets type = ServerPackets.USER_STATS;  // Define the packet type for USER_STATS

    private int userId;  // User ID to be sent
 
    public UserStatsHandler(int userId) {
        this.userId = userId;  // Initialize the user ID
    }

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) throws java.io.IOException {
        
        Player player = Server.players.values().stream()
            .filter(p -> p.getId() == userId) // Find the player by ID
            .findFirst()
            .orElse(null); // If not found, return null

        writer.startPacket(type.getValue()); // Start new packet

        // Write player ID
        writer.writeInt(player.getId());

        // Write player status
        writer.writeByte((byte) (player.getAction() & 0xFF)); // uint8
        writer.writeString(player.getActionText()); // string
        writer.writeString(player.getBeatmapMd5()); // string
        writer.writeInt(player.getMods()); // int32
        writer.writeByte((byte) (player.getGameMode() & 0xFF)); // uint8
        writer.writeInt(player.getBeatmapId()); // int32
        
        // Write player stats
        writer.writeLong(player.getRankedScore()); // int64
        writer.writeFloat(player.getAccuracy() / 100.0f); // float32 (0.0 - 1.0)
        writer.writeInt(player.getPlayCount()); // int32
        writer.writeLong(player.getTotalScore()); // int64
        writer.writeInt(player.getGlobalRank()); // int32

        // Clamp and write PP as int16
        int pp = (int) Math.ceil(player.getPp());
        if (pp > Short.MAX_VALUE) pp = Short.MAX_VALUE;
        if (pp < Short.MIN_VALUE) pp = Short.MIN_VALUE;
        writer.writeShort((short) pp); // int16

        // Log the stats being sent
        logger.info("Sending stats for player {}: ID={}, RankedScore={}, Accuracy={}, PlayCount={}, TotalScore={}, GlobalRank={}, PP={}",
            player.getUsername(), player.getId(), player.getRankedScore(), player.getAccuracy(), player.getPlayCount(),
            player.getTotalScore(), player.getGlobalRank(), pp);

            writer.endPacket(); // Finalize packet
        return true;
    }
    
}
