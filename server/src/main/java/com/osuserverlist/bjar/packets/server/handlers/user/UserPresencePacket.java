package com.osuserverlist.bjar.packets.server.handlers.user;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.server.BanchoPacketWriter;
import com.osuserverlist.bjar.packets.server.ServerPacketHandler;
import com.osuserverlist.bjar.packets.server.ServerPackets;
import com.osuserverlist.bjar.server.Server;

public class UserPresencePacket implements ServerPacketHandler {

    final ServerPackets type = ServerPackets.USER_PRESENCE;

    private final int userId;

    public UserPresencePacket(int userId) {
        this.userId = userId;
    }

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) throws java.io.IOException {
        Player player = Server.getInstance().playerManager.getById(userId);

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