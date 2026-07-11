package com.osuserverlist.bjar.packets.client.handlers.multi.ingame;

import java.io.IOException;

import com.osuserverlist.bjar.models.essentials.Match;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.client.BanchoPacketHandler;
import com.osuserverlist.bjar.packets.client.BanchoPacketReader;
import com.osuserverlist.bjar.packets.client.ClientPackets;
import com.osuserverlist.bjar.packets.client.engine.ClientPacket;
import com.osuserverlist.bjar.packets.server.handlers.multi.MatchScoreUpdatePacket;

@ClientPacket(ClientPackets.MATCH_SCORE_UPDATE)
public class MatchScoreUpdateClientPacket implements BanchoPacketHandler {

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        Match match = player.getMatch();
        if (match == null) {
            logger.warn("Player {} sent MATCH_SCORE_UPDATE but is not in a match", player);
            return false;
        }

        int slotId = match.getSlot(player);
        if (slotId < 0) {
            logger.warn("Player {} has no slot in match {}", player, match.getMatchId());
            return false;
        }

        // Copy the original payload exactly as the client sent it.
        byte[] playData = reader.getCurrentPacketBody().clone();

        // ScoreFrame.id is the 5th byte (offset 4) of the payload.
        playData[4] = (byte) slotId;

        match.getPlayers().forEach(p ->
            p.sendPacket(new MatchScoreUpdatePacket(playData))
        );

        return true;
    }
}