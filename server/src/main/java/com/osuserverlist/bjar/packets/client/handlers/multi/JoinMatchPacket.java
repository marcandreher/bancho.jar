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
import com.osuserverlist.bjar.packets.server.handlers.multi.MatchJoinFailPacket;

@ClientPacket(ClientPackets.JOIN_MATCH)
public class JoinMatchPacket implements BanchoPacketHandler {

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        int matchId = reader.readInt();
        String password = "";

        Server server = Server.getInstance();
        Match match = server.matchManager.getById((short) matchId);

        if (match == null) {
            logger.warn("Player {} attempted to join non-existent match {}", player.getUsername(), matchId);
            return true;
        }

        if (match.getRoomPassword().length() > 0) {
            password = reader.readString();
        }

        // TODO: handle restrictions

        if (match.getRoomPassword().length() > 0 && !match.getRoomPassword().equals(password)) {
            logger.warn("Player {} attempted to join match {} with incorrect password", player.getUsername(), matchId);
            player.sendPacket(new MatchJoinFailPacket());
            return true;
        }

        server.matchManager.joinMatch(match, player);

        return true;
    }

}
