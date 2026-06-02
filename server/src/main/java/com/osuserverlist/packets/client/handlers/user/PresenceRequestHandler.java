package com.osuserverlist.packets.client.handlers.user;

import java.io.IOException;
import java.util.List;

import com.osuserverlist.main.Server;
import com.osuserverlist.models.essentials.Player;
import com.osuserverlist.packets.BanchoPacket;
import com.osuserverlist.packets.client.BanchoPacketHandler;
import com.osuserverlist.packets.client.BanchoPacketReader;
import com.osuserverlist.packets.server.handlers.user.UserPresenceHandler;

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
