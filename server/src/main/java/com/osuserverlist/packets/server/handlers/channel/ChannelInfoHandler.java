package com.osuserverlist.packets.server.handlers.channel;

import com.osuserverlist.models.essentials.Player;
import com.osuserverlist.packets.BanchoPacket;
import com.osuserverlist.packets.server.BanchoPacketWriter;
import com.osuserverlist.packets.server.ServerPacketHandler;
import com.osuserverlist.packets.server.ServerPackets;

public class ChannelInfoHandler implements ServerPacketHandler {

    final ServerPackets type = ServerPackets.CHANNEL_INFO;

    private String channelName; 
    private String channelDescription;
    private short userCount;

    public ChannelInfoHandler(String channelName, String channelDescription, short userCount) {
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