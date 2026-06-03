package com.osuserverlist.bjar.packets.server.handlers.connect;

import java.io.IOException;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.server.BanchoPacketWriter;
import com.osuserverlist.bjar.packets.server.ServerPacketHandler;
import com.osuserverlist.bjar.packets.server.ServerPackets;

public class PermissionsPacket implements ServerPacketHandler {

    final ServerPackets type = ServerPackets.PRIVILEGES;
    private int permissions; 

    public PermissionsPacket(int permissions) {
        this.permissions = permissions;
    }

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) throws IOException {
        writer.startPacket(type.getValue());
        writer.writeInt(permissions);
        writer.endPacket();
        return true;
    }
    
}