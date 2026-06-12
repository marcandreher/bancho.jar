package com.osuserverlist.bjar.packets.client.handlers.user;

import java.io.IOException;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.client.BanchoPacketHandler;
import com.osuserverlist.bjar.packets.client.BanchoPacketReader;
import com.osuserverlist.bjar.packets.client.ClientPackets;
import com.osuserverlist.bjar.packets.client.engine.ClientPacket;
import com.osuserverlist.bjar.packets.server.handlers.user.UserStatsPacket;

@ClientPacket(ClientPackets.REQUEST_STATUS_UPDATE)
public class UserSelfRequestStatusPacket implements BanchoPacketHandler {

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        player.sendPacket(new UserStatsPacket(player.getId()));
        return true;
    }
    
}
