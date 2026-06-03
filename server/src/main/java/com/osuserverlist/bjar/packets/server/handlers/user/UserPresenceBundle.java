package com.osuserverlist.bjar.packets.server.handlers.user;

import java.io.IOException;
import java.util.List;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.server.BanchoPacketWriter;
import com.osuserverlist.bjar.packets.server.ServerPacketHandler;
import com.osuserverlist.bjar.packets.server.ServerPackets;
import com.osuserverlist.bjar.server.Server;

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
