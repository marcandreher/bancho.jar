package com.osuserverlist.bjar.packets.server.handlers.multi;

import java.io.IOException;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.server.BanchoPacketWriter;
import com.osuserverlist.bjar.packets.server.ServerPacketHandler;
import com.osuserverlist.bjar.packets.server.ServerPackets;

public class DisposeMatchPacket implements ServerPacketHandler {

    private final short matchId;

    public DisposeMatchPacket(short matchId) {
        this.matchId = matchId;
    }

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) throws IOException {
        writer.startPacket(ServerPackets.DISPOSE_MATCH.getValue());
        writer.writeInt(matchId);
        writer.endPacket();
        return true;
    }
}