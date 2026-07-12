package com.osuserverlist.bjar.packets.server.handlers.multi;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.server.BanchoPacketWriter;
import com.osuserverlist.bjar.packets.server.ServerPacketHandler;
import com.osuserverlist.bjar.packets.server.ServerPackets;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MatchPlayerFailedPacket implements ServerPacketHandler {
    private final int slotId;

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) {
        writer.startPacket(ServerPackets.MATCH_PLAYER_FAILED.getValue());
        writer.writeInt(slotId);
        writer.endPacket();
        return true;
    }
}
