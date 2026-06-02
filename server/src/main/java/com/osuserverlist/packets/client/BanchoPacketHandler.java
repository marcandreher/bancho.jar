package com.osuserverlist.packets.client;

import java.io.IOException;

import com.osuserverlist.models.essentials.Player;
import com.osuserverlist.packets.BanchoPacket;

public interface BanchoPacketHandler {
    boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException;
}