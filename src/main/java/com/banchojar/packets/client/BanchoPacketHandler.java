package com.banchojar.packets.client;

import java.io.IOException;

import com.banchojar.Player;
import com.banchojar.packets.BanchoPacket;

public interface BanchoPacketHandler {
    boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException;
}
