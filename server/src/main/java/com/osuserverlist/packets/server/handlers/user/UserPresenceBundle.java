package com.osuserverlist.packets.server.handlers.user;

import java.io.IOException;
import java.util.List;

import com.osuserverlist.main.Server;
import com.osuserverlist.models.essentials.Player;
import com.osuserverlist.packets.BanchoPacket;
import com.osuserverlist.packets.server.BanchoPacketWriter;
import com.osuserverlist.packets.server.ServerPacketHandler;
import com.osuserverlist.packets.server.ServerPackets;

public class UserPresenceBundle implements ServerPacketHandler {

    final ServerPackets type = ServerPackets.USER_PRESENCE_BUNDLE;

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) throws IOException {
        List<Integer> userIds = Server.getInstance().playerManager.getAll().stream()
                .filter(p -> p.getId() != sender.getId())
                .map(Player::getId)
                .toList();
        writer.startPacket(type.getValue());
        writer.writeIntList(userIds);
        writer.endPacket();
        return true;
    }
    
}
