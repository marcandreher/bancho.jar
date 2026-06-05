package com.osuserverlist.bjar.packets.server.handlers.user;

import java.io.IOException;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.server.BanchoPacketWriter;
import com.osuserverlist.bjar.packets.server.ServerPacketHandler;
import com.osuserverlist.bjar.packets.server.ServerPackets;

public class UserQuitPacket implements ServerPacketHandler {

    private static final ServerPackets type = ServerPackets.USER_LOGOUT;

    private final int id;

    public UserQuitPacket(int id) {
        this.id = id;
    }

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) throws IOException {
        writer.startPacket(type.getValue());
        writer.writeInt(id);
        writer.endPacket();
        return true;
    }

}
