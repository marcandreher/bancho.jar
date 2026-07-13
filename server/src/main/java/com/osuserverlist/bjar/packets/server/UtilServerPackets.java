package com.osuserverlist.bjar.packets.server;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.BanchoPacketWriter;
import com.osuserverlist.bjar.modules.ServerPacketEngine.PacketHandler;
import com.osuserverlist.bjar.modules.ServerPacketEngine.ServerPacket;
import com.osuserverlist.bjar.modules.ServerPacketEngine.ServerPacketHandler;
import com.osuserverlist.bjar.modules.ServerPacketEngine.ServerPackets;

import lombok.Value;

public class UtilServerPackets {
    
    @Value
    public static class NotificationPacket implements ServerPacket {
        public String message;
    }

    @PacketHandler(NotificationPacket.class)
    public static final class NotificationHandler implements ServerPacketHandler<NotificationPacket> {
        @Override
        public void write(NotificationPacket packet, BanchoPacketWriter writer, Player player) {
            writer.startPacket(ServerPackets.NOTIFICATION);
            writer.writeString(packet.getMessage());
            writer.endPacket();
        }
    }

}
