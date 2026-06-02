package com.osuserverlist.packets.server.handlers.connect;

import com.osuserverlist.models.essentials.Player;
import com.osuserverlist.packets.BanchoPacket;
import com.osuserverlist.packets.server.BanchoPacketWriter;
import com.osuserverlist.packets.server.ServerPacketHandler;
import com.osuserverlist.packets.server.ServerPackets;

public class LoginReplyHandler implements ServerPacketHandler {
    
    final ServerPackets type = ServerPackets.LOGIN_REPLY;

    private int userId; 

    public LoginReplyHandler(int userId) {
        this.userId = userId;
    }

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) throws java.io.IOException {
        writer.startPacket(type.getValue());
        writer.writeInt(userId);
        writer.endPacket();
        return true;
    }
}
