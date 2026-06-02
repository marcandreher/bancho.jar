package com.osuserverlist.packets.server.handlers.channel;

import com.osuserverlist.models.essentials.Player;
import com.osuserverlist.packets.BanchoPacket;
import com.osuserverlist.packets.server.BanchoPacketWriter;
import com.osuserverlist.packets.server.ServerPacketHandler;
import com.osuserverlist.packets.server.ServerPackets;

public class ChannelJoinSuccessHandler implements ServerPacketHandler {

    final ServerPackets type = ServerPackets.CHANNEL_JOIN_SUCCESS;

    private String channelName;

    public ChannelJoinSuccessHandler(String channelName) {
        this.channelName = channelName;
    }

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) throws java.io.IOException {
        writer.startPacket(type.getValue());
        writer.writeString(channelName); 
        writer.endPacket();
        return true;
    }
    
}