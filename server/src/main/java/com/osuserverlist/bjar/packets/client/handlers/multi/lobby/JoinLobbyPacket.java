package com.osuserverlist.bjar.packets.client.handlers.multi.lobby;

import java.io.IOException;

import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.essentials.Match;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.client.BanchoPacketHandler;
import com.osuserverlist.bjar.packets.client.BanchoPacketReader;
import com.osuserverlist.bjar.packets.client.ClientPackets;
import com.osuserverlist.bjar.packets.client.engine.ClientPacket;
import com.osuserverlist.bjar.packets.server.handlers.multi.NewMatchPacket;

@ClientPacket(ClientPackets.JOIN_LOBBY)
public class JoinLobbyPacket implements BanchoPacketHandler {

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        player.setInLobby(true);

        Server server = Server.getInstance();

        for (Match match : server.matchManager.getAll()) {
            player.sendPacket(new NewMatchPacket(match));
        }

        return true;
    }

}
