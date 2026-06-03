package com.osuserverlist.bjar.packets.server.handlers.channel;

import java.io.IOException;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.server.BanchoPacketWriter;
import com.osuserverlist.bjar.packets.server.ServerPacketHandler;
import com.osuserverlist.bjar.packets.server.ServerPackets;

public class ChannelAutojoinPacket implements ServerPacketHandler {

    final ServerPackets type = ServerPackets.CHANNEL_AUTO_JOIN;

    private String channel;

    public ChannelAutojoinPacket(String channel) {
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