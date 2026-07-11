package com.osuserverlist.bjar.packets.client.handlers.multi;

import java.io.IOException;

import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.essentials.Match;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.client.BanchoPacketHandler;
import com.osuserverlist.bjar.packets.client.BanchoPacketReader;
import com.osuserverlist.bjar.packets.client.ClientPackets;
import com.osuserverlist.bjar.packets.client.engine.ClientPacket;

@ClientPacket(ClientPackets.MATCH_CHANGE_PASSWORD)
public class MatchChangePasswordPacket implements BanchoPacketHandler {

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        Match match = reader.readMatch();

        Server server = Server.getInstance();
        Match playerMatch = server.matchManager.getByHostId(player.getId());

        if(playerMatch != null) {
            playerMatch.setRoomPassword(match.getRoomPassword());
            playerMatch.enqueUpdate();
        }
        return true;
    }
    
}
