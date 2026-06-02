package com.osuserverlist.packets.server.handlers.user;

import java.io.IOException;

import com.osuserverlist.models.essentials.Player;
import com.osuserverlist.packets.BanchoPacket;
import com.osuserverlist.packets.server.BanchoPacketWriter;
import com.osuserverlist.packets.server.ServerPacketHandler;
import com.osuserverlist.packets.server.ServerPackets;

public class UserPresenceSingle implements ServerPacketHandler {
    final ServerPackets type = ServerPackets.USER_PRESENCE_SINGLE;

    private final int userId;

    public UserPresenceSingle(int userId) {
        this.userId = userId;
    }

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) throws IOException {
        writer.startPacket(type.getValue());
        writer.writeInt(userId);
        writer.endPacket();
        return true;
    }

}
