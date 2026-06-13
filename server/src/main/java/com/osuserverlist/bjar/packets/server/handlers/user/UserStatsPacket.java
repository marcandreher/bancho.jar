package com.osuserverlist.bjar.packets.server.handlers.user;

import java.io.IOException;

import org.slf4j.Logger;

import com.osuserverlist.bjar.models.essentials.ModeStats;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.logger.LoggerFactory;
import com.osuserverlist.bjar.modules.redis.Redis;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.server.BanchoPacketWriter;
import com.osuserverlist.bjar.packets.server.ServerPacketHandler;
import com.osuserverlist.bjar.packets.server.ServerPackets;
import com.osuserverlist.bjar.server.Server;

public class UserStatsPacket implements ServerPacketHandler {

    private final static Logger logger = LoggerFactory.getLogger(UserStatsPacket.class);
    private Player player;
    private String userId;

    public UserStatsPacket(int userId) {
        Player player = Server.getInstance().playerManager.getById(userId);
        this.userId = String.valueOf(userId);
        this.player = player;
    }

    public UserStatsPacket(Player player) {
        this.player = player;
        this.userId = String.valueOf(player.getId());
    }

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) throws IOException {

        if (player == null) {
            logger.warn("Player not found. ID=" + userId);
            return false;
        }

        ModeStats playerStats = player.getModeStats()[player.getRealGameMode()];
        String beatmapMd5 = player.getBeatmapMd5() != null ? player.getBeatmapMd5() : "";
        float accuracy = playerStats.getAccuracy();
        if (accuracy > 1.0f) {
            accuracy /= 100.0f;
        }

        int pp = 0;
        long rankedScore = 0;
        long totalScore = 0;
        if(playerStats.getPp() > Short.MAX_VALUE) {
            rankedScore = (int) playerStats.getPp();
            totalScore = (int) playerStats.getPp();
        } else {
            pp = (int) playerStats.getPp();
            rankedScore = playerStats.getRankedScore();
            totalScore = playerStats.getTotalScore();
        }

        writer.startPacket(ServerPackets.USER_STATS.getValue());
        writer.writeInt(player.getId());
        writer.writeByte((byte) (player.getAction() & 0xFF));
        writer.writeString(player.getActionText());
        writer.writeString(beatmapMd5);
        writer.writeInt((short) player.getMods());
        writer.writeByte((byte) (player.getGameMode()));
        writer.writeInt(player.getBeatmapId());
        writer.writeLong(rankedScore);
        writer.writeFloat(accuracy);
        writer.writeInt(playerStats.getPlayCount());
        writer.writeLong(totalScore);
        Long redisRank = Redis.getClient().zrevrank("bjar:leaderboard:" + player.getRealGameMode(), String.valueOf(player.getId()));
        writer.writeInt((redisRank != null ? Math.toIntExact(redisRank) : -1) + 1);
        writer.writeShort((short) Math.ceil(pp));
        writer.endPacket();
        return true;
    }
}