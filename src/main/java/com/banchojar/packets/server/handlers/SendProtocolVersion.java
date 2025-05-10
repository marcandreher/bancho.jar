package com.banchojar.packets.server.handlers;

import java.io.IOException;

import com.banchojar.Player;
import com.banchojar.packets.BanchoPacket;
import com.banchojar.packets.server.BanchoPacketWriter;
import com.banchojar.packets.server.ServerPacketHandler;
import com.banchojar.packets.server.ServerPackets;

public class SendProtocolVersion implements ServerPacketHandler {
    
    final ServerPackets type = ServerPackets.PROTOCOL_VERSION;

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) throws IOException {
        writer.startPacket(type.getValue());
        writer.writeInt(19); // Protocol version
        writer.endPacket();
        return true;
    }

}
