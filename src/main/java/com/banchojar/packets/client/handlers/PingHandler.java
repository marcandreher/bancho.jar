package com.banchojar.packets.client.handlers;

import com.banchojar.Player;
import com.banchojar.packets.BanchoPacket;
import com.banchojar.packets.client.BanchoPacketHandler;
import com.banchojar.packets.client.BanchoPacketReader;

public class PingHandler implements BanchoPacketHandler {

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) {
       // Do nothing
       
        
       return true;
    }
    
}
