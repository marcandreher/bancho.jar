package com.osuserverlist.bjar.packets.client.handlers.multi;

import java.io.IOException;

import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.essentials.Match;
import com.osuserverlist.bjar.models.essentials.MatchSlot;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.client.BanchoPacketHandler;
import com.osuserverlist.bjar.packets.client.BanchoPacketReader;
import com.osuserverlist.bjar.packets.client.ClientPackets;
import com.osuserverlist.bjar.packets.client.engine.ClientPacket;
import com.osuserverlist.bjar.packets.server.handlers.multi.MatchTransferHostPacket;

@ClientPacket(ClientPackets.MATCH_TRANSFER_HOST)
public class MatchTransferHostClientPacket implements BanchoPacketHandler {

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        int slotId = reader.readInt();

        Server server = Server.getInstance();
        Match match = player.getMatch();
        if (match == null) {
            logger.warn("Player {} sent MATCH_TRANSFER_HOST but is not in a match", player);
            return false;
        }

        MatchSlot slot = match.getSlots()[slotId];
        if (slot == null || slot.getPlayerId() == 0) {
            logger.warn("Player {} sent MATCH_TRANSFER_HOST but slot {} is empty", player, slotId);
            return false;
        }

        Player newHost = server.playerManager.getById(slot.getPlayerId());
        if (newHost == null) {
            logger.warn("Player {} sent MATCH_TRANSFER_HOST but slot {} has no player", player, slotId);
            return false;
        }

        server.matchManager.updateHost(match, newHost.getId());
        newHost.sendPacket(new MatchTransferHostPacket());
        logger.info("Player {} transferred host to player {} in match {}", player, newHost, match);
        match.enqueUpdate();
        return true;
    }
    
}
