package com.osuserverlist.packets.client.handlers.user;

import java.io.IOException;
import java.util.List;

import com.osuserverlist.main.Server;
import com.osuserverlist.models.essentials.Player;
import com.osuserverlist.packets.BanchoPacket;
import com.osuserverlist.packets.client.BanchoPacketHandler;
import com.osuserverlist.packets.client.BanchoPacketReader;
import com.osuserverlist.packets.server.handlers.user.UserStatsHandler;

public class StatsRequestHandler implements BanchoPacketHandler {

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        List<Integer> userIds = reader.readIntList();

        for (int userId : userIds) {

            if (player.getId() == userId) {
                continue;
            }

            Player requestedPlayer = Server.getInstance().playerManager.getById(userId);

            if (requestedPlayer != null) {
                player.sendPacket(new UserStatsHandler(requestedPlayer.getId()));
            }
        }

        return true;
    }
}