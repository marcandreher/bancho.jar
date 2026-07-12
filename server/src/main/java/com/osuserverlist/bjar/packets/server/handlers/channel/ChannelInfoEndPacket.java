package com.osuserverlist.bjar.packets.server.handlers.channel;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.server.BanchoPacketWriter;
import com.osuserverlist.bjar.packets.server.ServerPacketHandler;
import com.osuserverlist.bjar.packets.server.ServerPackets;

public class ChannelInfoEndPacket implements ServerPacketHandler {
    final ServerPackets type = ServerPackets.CHANNEL_INFO_END;

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) {
        writer.startPacket(type.getValue());
        writer.endPacket();
        return true;
    }
    
}