package com.osuserverlist.bjar.packets.client.handlers.multi;

import java.io.IOException;
import java.util.Arrays;

import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.essentials.Match;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.client.BanchoPacketHandler;
import com.osuserverlist.bjar.packets.client.BanchoPacketReader;
import com.osuserverlist.bjar.packets.client.ClientPackets;
import com.osuserverlist.bjar.packets.client.engine.ClientPacket;

@ClientPacket(ClientPackets.PART_MATCH)
public class PartMatchPacket implements BanchoPacketHandler {

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        Server server = Server.getInstance();

        Match match = server.matchManager.getAll().stream()
                .filter(m -> Arrays.stream(m.getSlots())
                .anyMatch(slot -> slot.getPlayerId() == player.getId()))
                .findFirst()
                .orElse(null);

        if (match == null) {
            player.setMatch(null);
            return true;
        }

        server.matchManager.leaveMatch(match, player);
        return true;
    }

    

    

    
}