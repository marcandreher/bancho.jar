package com.osuserverlist.bjar.packets.client.handlers.multi.ingame;

import java.io.IOException;

import com.osuserverlist.bjar.models.essentials.Match;
import com.osuserverlist.bjar.models.essentials.MatchSlot;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.osu.match.SlotStatus;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.client.BanchoPacketHandler;
import com.osuserverlist.bjar.packets.client.BanchoPacketReader;
import com.osuserverlist.bjar.packets.client.ClientPackets;
import com.osuserverlist.bjar.packets.client.engine.ClientPacket;
import com.osuserverlist.bjar.packets.server.handlers.multi.MatchCompletePacket;

@ClientPacket(ClientPackets.MATCH_COMPLETE)
public class MatchCompleteClientPacket implements BanchoPacketHandler {

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        Match match = player.getMatch();
        if (match == null) {
            logger.warn("Player {} sent MATCH_COMPLETE but is not in a match", player);
            return true;
        }

        MatchSlot slot = match.getSlots()[match.getSlot(player)];
        slot.setFinished(true);

        boolean allFinished = true;
        for (MatchSlot s : match.getSlots()) {
            if(s.getStatus() == SlotStatus.PLAYING.byteValue && !s.isFinished()) {
                allFinished = false;
                break;
            }
        }

        if(allFinished) {
            match.getPlayers().forEach(p -> {
                p.sendPacket(new MatchCompletePacket());
                match.getSlots()[match.getSlot(p)].setStatus(SlotStatus.NOT_READY.byteValue);
            });
            match.enqueUpdate();
            
        }

        return true;
    }
    
}
