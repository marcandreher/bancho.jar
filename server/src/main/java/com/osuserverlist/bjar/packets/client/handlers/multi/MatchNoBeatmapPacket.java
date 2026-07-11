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

@ClientPacket(ClientPackets.MATCH_NO_BEATMAP)
public class MatchNoBeatmapPacket implements BanchoPacketHandler {

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        Match match = player.getMatch();
        if (match == null) {
            logger.warn("Player {} sent MATCH_NO_BEATMAP but is not in a match", player);
            return false;
        }

        Integer slot = match.getSlot(player);
        if (slot == null) {
            logger.warn("Player {} sent MATCH_NO_BEATMAP but is not in a match slot", player);
            return false;
        }

        match.getSlots()[slot].setStatus(SlotStatus.NO_MAP_LOADED.byteValue);
        match.enqueUpdate();
        return true;
    }
    
}
