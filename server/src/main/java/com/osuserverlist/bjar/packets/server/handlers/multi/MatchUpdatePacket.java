package com.osuserverlist.bjar.packets.server.handlers.multi;

import com.osuserverlist.bjar.models.essentials.Match;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.server.BanchoPacketWriter;
import com.osuserverlist.bjar.packets.server.ServerPacketHandler;
import com.osuserverlist.bjar.packets.server.ServerPackets;

public class MatchUpdatePacket implements ServerPacketHandler {

    private final Match match;

    public MatchUpdatePacket(Match match) {
        this.match = match;
    }

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) {
        writer.startPacket(ServerPackets.UPDATE_MATCH.getValue());
        writer.writeMatch(match);
        writer.endPacket();
        return true;
    }
}