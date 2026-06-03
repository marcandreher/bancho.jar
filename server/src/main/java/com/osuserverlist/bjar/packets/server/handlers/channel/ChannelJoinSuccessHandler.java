package com.osuserverlist.bjar.packets.server.handlers.channel;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.server.BanchoPacketWriter;
import com.osuserverlist.bjar.packets.server.ServerPacketHandler;
import com.osuserverlist.bjar.packets.server.ServerPackets;

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