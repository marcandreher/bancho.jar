package com.banchojar.packets.server.handlers;

import com.banchojar.Player;
import com.banchojar.Server;
import com.banchojar.packets.BanchoPacket;
import com.banchojar.packets.server.BanchoPacketWriter;
import com.banchojar.packets.server.ServerPacketHandler;
import com.banchojar.packets.server.ServerPackets;

public class UserPresenceHandler implements ServerPacketHandler {

    final ServerPackets type = ServerPackets.USER_PRESENCE;

    private final int userId;

    public UserPresenceHandler(int userId) {
        this.userId = userId;
    }

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) throws java.io.IOException {
        Player player = Server.players.values().stream()
                .filter(p -> p.getId() == userId)
                .findFirst()
                .orElse(null);
    
        if (player == null) return false;
    
        writer.startPacket(type.getValue());
    
        // Write each field, ensuring correct sizes
        writer.writeInt(player.getId()); // User ID (4 bytes)
        writer.writeString(player.getUsername()); // Username (null-terminated string)
        writer.writeByte((byte) (player.getTimezone() + 24)); // Timezone (1 byte)
        writer.writeByte((byte) player.getCountry()); // Country ID (1 byte)
        writer.writeByte((byte) (player.getPrivileges() | (player.getGameMode()) << 5)); // Permissions | Mode << 5 (1 byte)
        writer.writeFloat(player.getLongitude()); // Longitude (4 bytes)
        writer.writeFloat(player.getLatitude()); // Latitude (4 bytes)
        writer.writeInt(player.getRank()); // Rank (4 bytes)
    
        writer.endPacket();
        return true;
    }
    
}
