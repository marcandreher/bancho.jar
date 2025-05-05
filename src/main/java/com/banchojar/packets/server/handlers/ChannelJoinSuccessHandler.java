package com.banchojar.packets.server.handlers;

import com.banchojar.Player;
import com.banchojar.packets.BanchoPacket;
import com.banchojar.packets.server.BanchoPacketWriter;
import com.banchojar.packets.server.ServerPacketHandler;
import com.banchojar.packets.server.ServerPackets;

public class ChannelJoinSuccessHandler implements ServerPacketHandler {

    final ServerPackets type = ServerPackets.CHANNEL_JOIN_SUCCESS;  // Define the packet type for CHANNEL_JOIN_SUCCESS

    private String channelName;

    public ChannelJoinSuccessHandler(String channelName) {
        this.channelName = channelName;  // Initialize the channel name
    }

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) throws java.io.IOException {
        writer.startPacket(type.getValue());  // Start new packet with CHANNEL_JOIN_SUCCESS packet ID
        writer.writeString(channelName);  // Send channel ID
        writer.endPacket();  // Finalize the packet
        return true;
    }
    
}
