package com.osuserverlist.packets.server;

import java.io.IOException;

import com.osuserverlist.models.essentials.Player;
import com.osuserverlist.packets.BanchoPacket;

public interface ServerPacketHandler {
    boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) throws IOException;
}