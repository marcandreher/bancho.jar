package com.osuserverlist.bjar.packets.client.handlers.user;

import java.io.IOException;
import java.util.List;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.client.BanchoPacketHandler;
import com.osuserverlist.bjar.packets.client.BanchoPacketReader;
import com.osuserverlist.bjar.packets.server.handlers.user.UserPresenceHandler;
import com.osuserverlist.bjar.server.Server;

public class PresenceRequestHandler implements BanchoPacketHandler {

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        List<Integer> userIds = reader.readIntList();

        for (int userId : userIds) {
            Player requestedPlayer = Server.getInstance().playerManager.getById(userId);

            // If player is found, send their presence and stats
            if (requestedPlayer != null) {
                player.sendPacket(new UserPresenceHandler(requestedPlayer.getId()));
            }
        }
        return true;
    }

}
