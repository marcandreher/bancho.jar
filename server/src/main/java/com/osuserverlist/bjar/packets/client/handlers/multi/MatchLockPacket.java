package com.osuserverlist.bjar.packets.client.handlers.multi;

import java.io.IOException;

import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.essentials.Match;
import com.osuserverlist.bjar.models.essentials.MatchSlot;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.osu.match.SlotStatus;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.client.BanchoPacketHandler;
import com.osuserverlist.bjar.packets.client.BanchoPacketReader;
import com.osuserverlist.bjar.packets.client.ClientPackets;
import com.osuserverlist.bjar.packets.client.engine.ClientPacket;
import com.osuserverlist.bjar.packets.server.handlers.multi.MatchUpdatePacket;

@ClientPacket(ClientPackets.MATCH_LOCK)
public class MatchLockPacket implements BanchoPacketHandler {

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        Match match = player.getMatch();
        if (match == null) {
            return true;
        }

        if (match.getHostId() != player.getId()) {
            return true;
        }

        int slotId = reader.readInt();
        if (slotId < 0 || slotId >= Match.MAX_SLOTS) {
            return true;
        }

        MatchSlot slot = match.getSlots()[slotId];
        Server server = Server.getInstance();

        if (slot.getStatus() == (byte) SlotStatus.LOCKED.value) {
            slot.setStatus((byte) SlotStatus.OPEN.value);
            match.enqueUpdate();
            return true;
        }

        if (slot.getPlayerId() == player.getId()) {
            return true;
        }

        if (slot.getPlayerId() != 0) {
            int playerId = slot.getPlayerId();
            slot.reset();

            // Kick player from match
            Player p = server.playerManager.getById(playerId);
            p.sendPacket(new MatchUpdatePacket(match));
            p.setMatch(null);
        }

        slot.setStatus((byte) SlotStatus.LOCKED.value);
        match.enqueUpdate();
        return true;
    }



}
