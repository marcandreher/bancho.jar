package com.banchojar.packets.client;

import java.io.IOException;

import com.banchojar.Player;
import com.banchojar.packets.BanchoPacket;
import com.banchojar.packets.server.PacketSender;

public interface BanchoPacketHandler {
    boolean handle(BanchoPacket packet, PacketSender sender, BanchoPacketReader reader, int playerId) throws IOException;
}
