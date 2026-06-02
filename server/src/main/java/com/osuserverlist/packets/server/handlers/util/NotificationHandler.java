package com.osuserverlist.packets.server.handlers.util;

import com.osuserverlist.models.essentials.Player;
import com.osuserverlist.packets.BanchoPacket;
import com.osuserverlist.packets.server.BanchoPacketWriter;
import com.osuserverlist.packets.server.ServerPacketHandler;
import com.osuserverlist.packets.server.ServerPackets;

public class NotificationHandler implements ServerPacketHandler {

    final ServerPackets type = ServerPackets.NOTIFICATION;

    private String message; 

    public NotificationHandler(String message) {
        this.message = message;
    }

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) throws java.io.IOException {
        writer.startPacket(type.getValue());
        writer.writeString(message);
        writer.endPacket();
        return true;
    }
    
}