package com.osuserverlist.bjar.packets.server.handlers.user;

import com.osuserverlist.bjar.models.essentials.ModeStats;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.redis.Redis;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.server.BanchoPacketWriter;
import com.osuserverlist.bjar.packets.server.ServerPacketHandler;
import com.osuserverlist.bjar.packets.server.ServerPackets;

public class UserStatsPacket implements ServerPacketHandler {
    private Player player;

    public UserStatsPacket(Player player) {
        this.player = player;
    }

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) {
        ModeStats playerStats = player.getModeStats()[player.getRealGameMode()];
        String beatmapMd5 = player.getBeatmapMd5() != null ? player.getBeatmapMd5() : "";
        float accuracy = playerStats.getAccuracy();
        if (accuracy > 1.0f) {
            accuracy /= 100.0f;
        }

        int pp = 0;
        long rankedScore = 0;
        if(playerStats.getPp() > Short.MAX_VALUE) {
            rankedScore = (int) playerStats.getPp();
        } else {
            pp = (int) playerStats.getPp();
            rankedScore = playerStats.getRankedScore();
        }

        writer.startPacket(ServerPackets.USER_STATS.getValue());
        writer.writeInt(player.getId());
        writer.writeByte((player.getAction() & 0xFF));
        writer.writeString(player.getActionText());
        writer.writeString(beatmapMd5);
        writer.writeInt(player.getMods());
        writer.writeByte(player.getGameMode());
        writer.writeInt(player.getBeatmapId());
        writer.writeLong(rankedScore);
        writer.writeFloat(accuracy);
        writer.writeInt(playerStats.getPlayCount());
        writer.writeLong(playerStats.getTotalScore());
        Long redisRank = Redis.getClient().zrevrank("bjar:leaderboard:" + player.getRealGameMode(), String.valueOf(player.getId()));
        writer.writeInt((redisRank != null ? Math.toIntExact(redisRank) : -1) + 1);
        writer.writeShort((short) Math.ceil(pp));
        writer.endPacket();
        return true;
    }
}