package com.osuserverlist.bjar.packets.client;

import java.io.IOException;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;

public interface BanchoPacketHandler {
    boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException;
}