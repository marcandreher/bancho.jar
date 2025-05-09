package com.banchojar.packets.server.handlers;

import com.banchojar.Player;
import com.banchojar.packets.BanchoPacket;
import com.banchojar.packets.server.BanchoPacketWriter;
import com.banchojar.packets.server.ServerPacketHandler;
import com.banchojar.packets.server.ServerPackets;

public class ChannelInfoHandler implements ServerPacketHandler {

    final ServerPackets type = ServerPackets.CHANNEL_INFO;  // Define the packet type for CHANNEL_INFO

    private String channelName; 
    private String channelDescription;
    private short userCount;

    public ChannelInfoHandler(String channelName, String channelDescription, short userCount) {
        this.channelName = channelName;  // Initialize the channel name
        this.channelDescription = channelDescription;  // Initialize the channel description
        this.userCount = userCount;  // Initialize the user count
        
    }

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) throws java.io.IOException {
        writer.startPacket(type.getValue());
    
        writer.writeString(channelName);          // Channel name
        writer.writeString(channelDescription);   // Channel description
        writer.writeShort((short)userCount);        // Number of connected users
    
        writer.endPacket();
        return true;
    }
    
}
