package com.banchojar.packets.server;

import java.io.IOException;

import com.banchojar.Player;
import com.banchojar.packets.BanchoPacket;
import com.banchojar.packets.client.BanchoPacketReader;

public interface ServerPacketHandler {
    boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) throws IOException;
}
