package com.banchojar.packets.server.handlers;

import java.io.IOException;

import com.banchojar.App;
import com.banchojar.Player;
import com.banchojar.Player.ModeStats;
import com.banchojar.Server;
import com.banchojar.packets.BanchoPacket;
import com.banchojar.packets.server.BanchoPacketWriter;
import com.banchojar.packets.server.ServerPacketHandler;
import com.banchojar.packets.server.ServerPackets;

public class UserStatsHandler implements ServerPacketHandler {

    private Player player;
   private String userId;

    public UserStatsHandler(int userId) {

        Player player = Server.players.values().stream()
                .filter(p -> p.getId() == userId)
                .findFirst()
                .orElse(null);
        this.userId = String.valueOf(userId);
       this.player = player;

    }

    public UserStatsHandler(Player player) {
        this.player = player;
        this.userId = String.valueOf(player.getId());
    }

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) throws IOException {
  
        if (player == null) {
            App.logger.warn("Player not found. ID=" + userId);
            return false;
        }

        ModeStats playerStats = player.getModeStats()[player.getGameMode()];
        String beatmapMd5 = player.getBeatmapMd5() != null ? player.getBeatmapMd5() : "";
        float accuracy = playerStats.getAccuracy();
        if (accuracy > 1.0f) {
            accuracy /= 100.0f;
        }

        writer.startPacket(ServerPackets.USER_STATS.getValue());
        writer.writeInt(player.getId());
        writer.writeByte((byte) (player.getAction() & 0xFF));
        writer.writeString(player.getActionText());
        writer.writeString(beatmapMd5);
        writer.writeInt((short)player.getMods());
        writer.writeByte((byte) ( player.getGameMode()));
        writer.writeInt(player.getBeatmapId());
        writer.writeLong(playerStats.getRankedScore());
        writer.writeFloat(accuracy);
        writer.writeInt(playerStats.getPlayCount());
        writer.writeLong(playerStats.getTotalScore());  
        writer.writeInt(playerStats.getGlobalRank());
        writer.writeShort((short) Math.ceil(playerStats.getPp()));
        writer.endPacket();
        return true;
    }
}