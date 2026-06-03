package com.osuserverlist.packets.server.handlers.channel;

import java.io.IOException;

import com.osuserverlist.models.essentials.Player;
import com.osuserverlist.packets.BanchoPacket;
import com.osuserverlist.packets.server.BanchoPacketWriter;
import com.osuserverlist.packets.server.ServerPacketHandler;
import com.osuserverlist.packets.server.ServerPackets;

public class ChannelAutojoinHandler implements ServerPacketHandler {

    final ServerPackets type = ServerPackets.CHANNEL_AUTO_JOIN;

    private String channel;

    public ChannelAutojoinHandler(String channel) {
        this.channel = channel;
    }

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) throws IOException {
        writer.startPacket(ServerPackets.CHANNEL_AUTO_JOIN.getValue());  // Start new packet with CHANNEL_AUTO_JOIN packet ID
        writer.writeString(channel); 
        writer.endPacket();
        return true;
    }
    
}