package com.osuserverlist.packets.server.handlers.connect;

import java.io.IOException;

import com.osuserverlist.models.essentials.Player;
import com.osuserverlist.packets.BanchoPacket;
import com.osuserverlist.packets.server.BanchoPacketWriter;
import com.osuserverlist.packets.server.ServerPacketHandler;
import com.osuserverlist.packets.server.ServerPackets;

public class PermissionsHandler implements ServerPacketHandler {

    final ServerPackets type = ServerPackets.PRIVILEGES;
    private int permissions; 

    public PermissionsHandler(int permissions) {
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