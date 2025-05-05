package com.banchojar.packets.server.handlers;

import com.banchojar.Player;
import com.banchojar.Server;
import com.banchojar.packets.BanchoPacket;
import com.banchojar.packets.server.BanchoPacketWriter;
import com.banchojar.packets.server.ServerPacketHandler;
import com.banchojar.packets.server.ServerPackets;

public class UserPresenceHandler implements ServerPacketHandler {

    final ServerPackets type = ServerPackets.USER_PRESENCE;  // Define the packet type for USER_PRESENCE

    private int userId; 

    public UserPresenceHandler(int userId) {
        this.userId = userId;  // Initialize the user ID
    }

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) throws java.io.IOException {
        
        Player player = Server.players.values().stream()
            .filter(p -> p.getId() == userId) // Find the player by ID
            .findFirst()
            .orElse(null); // Retrieve the player object using the user ID

        writer.startPacket(type.getValue());  // Start new packet with USER_PRESENCE packet ID
        
        writer.writeInt(player.getId());
        writer.writeString(player.getUsername());
        writer.writeByte(player.getTimezone() + 24);
        writer.writeByte(player.getCountry());
        byte permissionsAndMode = (byte) ((player.getPrivileges() | (player.getMode() << 5)) & 0xFF);
        writer.writeByte(permissionsAndMode);
        writer.writeFloat(player.getLongitude());
        writer.writeFloat(player.getLatitude());
        writer.writeInt(player.getRank());
        
        writer.endPacket();  // Finalize the packet with the user presence details
        return true;
    }
    
}
