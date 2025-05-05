package com.banchojar.packets.server.handlers;

import com.banchojar.Player;
import com.banchojar.packets.BanchoPacket;
import com.banchojar.packets.server.BanchoPacketWriter;
import com.banchojar.packets.server.ServerPacketHandler;
import com.banchojar.packets.server.ServerPackets;

public class ChannelInfoEndHandler implements ServerPacketHandler {
    final ServerPackets type = ServerPackets.CHANNEL_INFO_END;  // Define the packet type for CHANNEL_INFO_END

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) throws java.io.IOException {
        writer.startPacket(type.getValue());  // Start new packet with CHANNEL_INFO_END packet ID
        writer.endPacket();  // Finalize the packet
        return true;
    }
    
}
