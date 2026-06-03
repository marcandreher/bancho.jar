package com.osuserverlist.bjar.packets.server;

import java.io.IOException;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;

public interface ServerPacketHandler {
    boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) throws IOException;
}