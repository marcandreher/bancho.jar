package com.osuserverlist.bjar.packets.client.handlers.user;

import java.io.IOException;
import java.util.List;

import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.client.BanchoPacketHandler;
import com.osuserverlist.bjar.packets.client.BanchoPacketReader;
import com.osuserverlist.bjar.packets.client.ClientPackets;
import com.osuserverlist.bjar.packets.client.engine.ClientPacket;
import com.osuserverlist.bjar.packets.server.handlers.user.UserStatsPacket;

@ClientPacket(ClientPackets.USER_STATS_REQUEST)
public class StatsRequestPacket implements BanchoPacketHandler {

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        List<Integer> userIds = reader.readIntList();

        userIds.forEach(id -> {
            if(player.getId() == id) return;

            Player requestedPlayer = Server.getInstance().playerManager.getById(id);

            if (requestedPlayer != null) {
                player.sendPacket(new UserStatsPacket(requestedPlayer.getId()));
            }
        });

        return true;
    }
}