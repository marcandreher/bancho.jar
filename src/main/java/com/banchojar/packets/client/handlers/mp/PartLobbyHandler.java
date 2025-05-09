package com.banchojar.packets.client.handlers.mp;

import com.banchojar.Player;
import com.banchojar.packets.BanchoPacket;
import com.banchojar.packets.client.BanchoPacketHandler;
import com.banchojar.packets.client.BanchoPacketReader;

public class PartLobbyHandler implements BanchoPacketHandler {

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) {
        player.setInLobby(false);
        return true;
    }
    
}
