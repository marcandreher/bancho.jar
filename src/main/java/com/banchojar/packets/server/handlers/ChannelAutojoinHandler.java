package com.banchojar.packets.server.handlers;

import java.io.IOException;
import java.util.Arrays;

import com.banchojar.Player;
import com.banchojar.Server;
import com.banchojar.packets.BanchoPacket;
import com.banchojar.packets.server.BanchoPacketWriter;
import com.banchojar.packets.server.ServerPacketHandler;
import com.banchojar.packets.server.ServerPackets;

public class ChannelAutojoinHandler implements ServerPacketHandler {

    final ServerPackets type = ServerPackets.CHANNEL_AUTO_JOIN;  // Define the packet type for CHANNEL_AUTOJOIN

    private String channel;  // Channel name

    public ChannelAutojoinHandler(String channel) {
        this.channel = channel;  // Initialize the channel name
    }

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) throws IOException {
        writer.startPacket(ServerPackets.CHANNEL_AUTO_JOIN.getValue());  // Start new packet with CHANNEL_AUTO_JOIN packet ID
        writer.writeString(channel);  // Channel name
        writer.endPacket();  // Finalize the packet

        if (Arrays.asList("#osu", "#lobby").contains(channel)) {
            return true; 
        }

        Server.channels.get(channel).getPlayers().add(sender);
        return true;
    }
    
}
