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
import com.osuserverlist.bjar.packets.server.handlers.multi.MatchStartClientPacket;

@ClientPacket(ClientPackets.MATCH_START)
public class MatchStartPacket implements BanchoPacketHandler {

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        Match match = player.getMatch();
        if(match == null) {
            logger.warn("Player {} tried to start a match but is not in a match", player.toString());
            return true;
        }

        if(match.getHostId() != player.getId()) {
            logger.warn("Player {} tried to start a match but is not the host", player.toString());
            return true;
        }

        for(int i = 0; i < match.getSlots().length; i++) {
            MatchSlot slot = match.getSlots()[i];
            slot.setSkipped(false);
            slot.setLoaded(false);
            if(slot.getPlayerId() == 0) continue;
            if(slot.getStatus() == SlotStatus.NO_MAP_LOADED.value) continue;
            slot.setStatus(SlotStatus.PLAYING.byteValue);
        }

        match.getPlayers().forEach(p -> {
            p.sendPacket(new MatchStartClientPacket(match));
        });

        return true;
    }
    
}
