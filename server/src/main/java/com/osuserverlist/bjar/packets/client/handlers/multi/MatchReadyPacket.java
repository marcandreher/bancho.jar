package com.osuserverlist.bjar.packets.client.handlers.multi;

import java.io.IOException;

import com.osuserverlist.bjar.models.essentials.Match;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.osu.match.SlotStatus;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.client.BanchoPacketHandler;
import com.osuserverlist.bjar.packets.client.BanchoPacketReader;
import com.osuserverlist.bjar.packets.client.ClientPackets;
import com.osuserverlist.bjar.packets.client.engine.ClientPacket;

@ClientPacket(ClientPackets.MATCH_READY)
public class MatchReadyPacket implements BanchoPacketHandler {

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        if(player.getMatch() == null) {
            return true;
        }

        Integer slot = player.getMatch().getSlot(player);
        if(slot == null) {
            return true;
        }

        Match playerMatch = player.getMatch();
        if(playerMatch == null) {
            return true;
        }

        playerMatch.getSlots()[slot].setStatus((byte) SlotStatus.READY.value);
        playerMatch.enqueUpdate();
        
        return true;   
    }
    
}
