package com.osuserverlist.bjar.packets.server.handlers.user;

import java.io.IOException;
import java.util.List;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.server.BanchoPacketWriter;
import com.osuserverlist.bjar.packets.server.ServerPacketHandler;
import com.osuserverlist.bjar.packets.server.ServerPackets;

public class UserFriendListPacket implements ServerPacketHandler {

    final ServerPackets type = ServerPackets.FRIENDS_LIST;
    private final List<Integer> friendIds;

    public UserFriendListPacket(List<Integer> friendIds) {
        this.friendIds = friendIds;
    }
    
    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) throws IOException {
        writer.startPacket(type.getValue());
        writer.writeIntList(friendIds);
        writer.endPacket();
        return true;
    }

}
