package com.osuserverlist.bjar.packets.client.handlers.multi;

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

@ClientPacket(ClientPackets.MATCH_CHANGE_SLOT)
public class MatchChangeSlotPacket implements BanchoPacketHandler {

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {

        Match match = player.getMatch();
        if (match == null) {
            return true;
        }

        int slotId = reader.readInt();

        if (slotId < 0 || slotId >= Match.MAX_SLOTS) {
            return true;
        }

        MatchSlot targetSlot = match.getSlots()[slotId];

        if (targetSlot.getStatus() != (byte) SlotStatus.OPEN.value) {
            return true;
        }

        MatchSlot currentSlot = null;

        for (MatchSlot slot : match.getSlots()) {
            if (slot.getPlayerId() == player.getId()) {
                currentSlot = slot;
                break;
            }
        }

        if (currentSlot == null) {
            return true;
        }

        // copy current slot into target slot
        targetSlot.setPlayerId(currentSlot.getPlayerId());
        targetSlot.setStatus(currentSlot.getStatus());
        targetSlot.setTeam(currentSlot.getTeam());
        targetSlot.setMods(currentSlot.getMods());
        targetSlot.setLoaded(currentSlot.isLoaded());
        targetSlot.setSkipped(currentSlot.isSkipped());

        // reset old slot
        currentSlot.setPlayerId(0);
        currentSlot.setStatus((byte) SlotStatus.OPEN.value);
        currentSlot.setTeam((byte) 0);
        currentSlot.setMods(0);
        currentSlot.setLoaded(false);
        currentSlot.setSkipped(false);

        match.enqueUpdate();

        return true;
    }

}