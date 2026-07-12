package com.osuserverlist.bjar.packets.server.handlers.connect;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.server.BanchoPacketWriter;
import com.osuserverlist.bjar.packets.server.ServerPacketHandler;
import com.osuserverlist.bjar.packets.server.ServerPackets;

public class ProtocolVersionPacket implements ServerPacketHandler {
        
    final ServerPackets type = ServerPackets.PROTOCOL_VERSION;

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) {
        writer.startPacket(type.getValue());
        writer.writeInt(19); // Protocol version
        writer.endPacket();
        return true;
    }

}
