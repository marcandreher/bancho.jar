package com.banchojar.packets.client.handlers;

import com.banchojar.Player;
import com.banchojar.packets.BanchoPacket;
import com.banchojar.packets.client.BanchoPacketHandler;
import com.banchojar.packets.client.BanchoPacketReader;
import com.banchojar.packets.server.PacketSender;

public class PingHandler implements BanchoPacketHandler {

    @Override
    public boolean handle(BanchoPacket packet, PacketSender sender, BanchoPacketReader reader, int playerId) {
       // Do nothing
    
       return true;
    }
    
}
