package com.osuserverlist.bjar.packets.server.handlers.multi;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.server.BanchoPacketWriter;
import com.osuserverlist.bjar.packets.server.ServerPacketHandler;
import com.osuserverlist.bjar.packets.server.ServerPackets;

public class MatchJoinFailPacket implements ServerPacketHandler {

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) {
        writer.startPacket(ServerPackets.MATCH_JOIN_FAIL.getValue());
        writer.endPacket();
        return true;
    }
    
}
