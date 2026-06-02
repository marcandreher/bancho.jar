package com.osuserverlist.packets.client.handlers.user;

import java.io.IOException;

import com.osuserverlist.models.essentials.Player;
import com.osuserverlist.packets.BanchoPacket;
import com.osuserverlist.packets.client.BanchoPacketHandler;
import com.osuserverlist.packets.client.BanchoPacketReader;
import com.osuserverlist.packets.server.handlers.user.UserStatsHandler;

public class UserSelfRequestStatusHandler implements BanchoPacketHandler {

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        player.sendPacket(new UserStatsHandler(player.getId()));
        return true;
    }
    
}
