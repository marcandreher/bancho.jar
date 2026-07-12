package com.osuserverlist.bjar.packets.server.handlers.connect;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.server.BanchoPacketWriter;
import com.osuserverlist.bjar.packets.server.ServerPacketHandler;
import com.osuserverlist.bjar.packets.server.ServerPackets;

public class LoginReplyPacket implements ServerPacketHandler {
    
    final ServerPackets type = ServerPackets.LOGIN_REPLY;

    private int userId; 

    public LoginReplyPacket(int userId) {
        this.userId = userId;
    }

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) {
        writer.startPacket(type.getValue());
        writer.writeInt(userId);
        writer.endPacket();
        return true;
    }
}
