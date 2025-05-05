package com.banchojar.packets.server.handlers;

import com.banchojar.Player;
import com.banchojar.packets.BanchoPacket;
import com.banchojar.packets.server.BanchoPacketWriter;
import com.banchojar.packets.server.ServerPacketHandler;
import com.banchojar.packets.server.ServerPackets;

public class NotificationHandler implements ServerPacketHandler {
    final ServerPackets type = ServerPackets.NOTIFICATION;  // Define the packet type for NOTIFICATION

    private String message; 

    public NotificationHandler(String message) {
        this.message = message;  // Initialize the message
    }

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) throws java.io.IOException {
        writer.startPacket(type.getValue());  // Start new packet with NOTIFICATION packet ID
        writer.writeString(message);  // Send message
        writer.endPacket();  // Finalize the packet
        return true;
    }
    
}
