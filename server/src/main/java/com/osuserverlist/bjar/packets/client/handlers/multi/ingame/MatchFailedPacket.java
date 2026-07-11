package com.osuserverlist.bjar.packets.client.handlers.multi.ingame;

import java.io.IOException;

import com.osuserverlist.bjar.models.essentials.Match;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.client.BanchoPacketHandler;
import com.osuserverlist.bjar.packets.client.BanchoPacketReader;
import com.osuserverlist.bjar.packets.client.ClientPackets;
import com.osuserverlist.bjar.packets.client.engine.ClientPacket;
import com.osuserverlist.bjar.packets.server.handlers.multi.MatchPlayerFailedPacket;

@ClientPacket(ClientPackets.MATCH_FAILED)
public class MatchFailedPacket implements BanchoPacketHandler {

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        Match match = player.getMatch();
        if (match == null) {
            logger.warn("Player {} sent MATCH_FAILED but is not in a match", player);
            return false;
        }


        int slotIndex = match.getSlot(player);
        match.getPlayers().forEach(p -> {
            p.sendPacket(new MatchPlayerFailedPacket(slotIndex));
        });
        
        return true;
    }
    
}
