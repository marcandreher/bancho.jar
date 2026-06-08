package com.osuserverlist.bjar.packets.server.handlers.channel;

import java.io.IOException;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.server.BanchoPacketWriter;
import com.osuserverlist.bjar.packets.server.ServerPacketHandler;
import com.osuserverlist.bjar.packets.server.ServerPackets;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ChannelRevokedPacket implements ServerPacketHandler {
    final ServerPackets type = ServerPackets.CHANNEL_KICK;

    private final String channelName;

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) throws IOException {
        writer.startPacket(type.getValue());
        writer.writeString(channelName); 
        writer.endPacket();
        return true;
    }
}
