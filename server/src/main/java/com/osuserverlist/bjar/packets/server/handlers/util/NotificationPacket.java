package com.osuserverlist.bjar.packets.server.handlers.util;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.server.BanchoPacketWriter;
import com.osuserverlist.bjar.packets.server.ServerPacketHandler;
import com.osuserverlist.bjar.packets.server.ServerPackets;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NotificationPacket implements ServerPacketHandler {

    final ServerPackets type = ServerPackets.NOTIFICATION;
    private final String message; 

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) {
        writer.startPacket(type.getValue());
        writer.writeString(message);
        writer.endPacket();
        return true;
    }
    
}