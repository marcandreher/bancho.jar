package com.osuserverlist.bjar.packets.server.handlers.channel;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.server.BanchoPacketWriter;
import com.osuserverlist.bjar.packets.server.ServerPacketHandler;
import com.osuserverlist.bjar.packets.server.ServerPackets;

public class ChannelInfoPacket implements ServerPacketHandler {

    final ServerPackets type = ServerPackets.CHANNEL_INFO;

    private String channelName; 
    private String channelDescription;
    private short userCount;

    public ChannelInfoPacket(String channelName, String channelDescription, short userCount) {
        this.channelName = channelName;
        this.channelDescription = channelDescription;
        this.userCount = userCount;
        
    }

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) throws java.io.IOException {
        writer.startPacket(type.getValue());
    
        writer.writeString(channelName);
        writer.writeString(channelDescription);
        writer.writeShort((short)userCount);
    
        writer.endPacket();
        return true;
    }
    
}