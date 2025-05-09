package com.banchojar.packets.client.handlers.mp;

import java.io.IOException;

import com.banchojar.Player;
import com.banchojar.packets.BanchoPacket;
import com.banchojar.packets.client.BanchoPacketHandler;
import com.banchojar.packets.client.BanchoPacketReader;

public class JoinLobbyHandler implements BanchoPacketHandler {

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        player.setInLobby(true);
        return true;
    }
    
}
