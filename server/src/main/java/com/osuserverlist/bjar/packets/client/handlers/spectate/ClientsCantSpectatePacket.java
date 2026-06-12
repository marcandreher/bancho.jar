package com.osuserverlist.bjar.packets.client.handlers.spectate;

import java.io.IOException;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.client.BanchoPacketHandler;
import com.osuserverlist.bjar.packets.client.BanchoPacketReader;
import com.osuserverlist.bjar.packets.client.ClientPackets;
import com.osuserverlist.bjar.packets.client.engine.ClientPacket;
import com.osuserverlist.bjar.packets.server.ServerPacketHandler;
import com.osuserverlist.bjar.packets.server.handlers.spectate.CantSpectatePacket;

@ClientPacket(ClientPackets.CANT_SPECTATE)
public class ClientsCantSpectatePacket implements BanchoPacketHandler {
    
    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        if(player.getSpectating() == null){
            return true;
        }

        if(player.getSpectating().isStealth()) {
            return true;
        }

        ServerPacketHandler cantSpectatePacket = new CantSpectatePacket(player.getId());
        Player host = player.getSpectating();
        host.sendPacket(cantSpectatePacket);

        for(Player p : host.getSpectators()) {
            p.sendPacket(cantSpectatePacket);
        }

        return true;
    }

}
