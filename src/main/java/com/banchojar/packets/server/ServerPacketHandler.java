package com.banchojar.packets.server;

import java.io.IOException;

import com.banchojar.Player;
import com.banchojar.packets.BanchoPacket;

public interface ServerPacketHandler {
    boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) throws IOException;
}
