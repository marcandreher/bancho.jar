package com.osuserverlist.bjar.packets.client.handlers.multi.ingame;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

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
import com.osuserverlist.bjar.packets.server.handlers.multi.MatchAllPlayersLoadedPacket;

@ClientPacket(ClientPackets.MATCH_LOAD_COMPLETE)
public class MatchLoadCompletePacket implements BanchoPacketHandler {

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        Match match = player.getMatch();
        if (match == null) {
            logger.warn("Player {} sent MATCH_LOAD_COMPLETE but is not in a match", player);
            return false;
        }

        Integer slotIndex = match.getSlot(player);
        if (slotIndex == null) {
            logger.warn("Player {} sent MATCH_LOAD_COMPLETE but is not in a match slot", player);
            return false;
        }

        match.getSlots()[slotIndex].setLoaded(true);

        boolean isLoaded = match.isLoaded();

        if (match.getLoadTimeoutTask() == null) {
            match.setLoadTimeoutTask(
                Server.getInstance().scheduler.schedule(() -> {
                    sendMatchAllPlayersLoadedPacket(match);
                    match.setLoadTimeoutTask(null);
                }, 30, TimeUnit.SECONDS)
            );
        }

        if (isLoaded) {
            if (match.getLoadTimeoutTask() != null) {
                match.getLoadTimeoutTask().cancel(false);
                match.setLoadTimeoutTask(null);
            }

            sendMatchAllPlayersLoadedPacket(match);
        }

        return true;
    }

    public void sendMatchAllPlayersLoadedPacket(Match match) {
        match.getPlayers().forEach(p -> {
            MatchSlot slot = match.getSlots()[match.getSlot(p)];
            if (slot.getStatus() != SlotStatus.NO_MAP_LOADED.byteValue) {
                p.sendPacket(new MatchAllPlayersLoadedPacket());
            }
        });

        for(MatchSlot matchSlot : match.getSlots()) {
            logger.debug("Slot: {}", matchSlot.toString());
        }
    }
}
