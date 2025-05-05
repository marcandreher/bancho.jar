package com.banchojar.packets.server.handlers;

import com.banchojar.Player;
import com.banchojar.packets.BanchoPacket;
import com.banchojar.packets.server.BanchoPacketWriter;
import com.banchojar.packets.server.ServerPacketHandler;
import com.banchojar.packets.server.ServerPackets;

public class LoginReplyHandler implements ServerPacketHandler {

    final ServerPackets type = ServerPackets.LOGIN_REPLY;  // Define the packet type for LOGIN_REPLY

    private int userId; 

    public LoginReplyHandler(int userId) {
        this.userId = userId;  // Initialize the user ID
    }

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) throws java.io.IOException {
        writer.startPacket(type.getValue());  // Start new packet with LOGIN_REPLY packet ID
        writer.writeInt(userId);  // Send user ID
        writer.endPacket();  // Finalize the packet
        return true;
    }
    
}
