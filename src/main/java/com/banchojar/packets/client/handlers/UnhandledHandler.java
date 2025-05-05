package com.banchojar.packets.client.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.banchojar.Player;
import com.banchojar.packets.BanchoPacket;
import com.banchojar.packets.client.BanchoPacketHandler;
import com.banchojar.packets.client.BanchoPacketReader;
import com.banchojar.packets.server.PacketSender;

public class UnhandledHandler implements BanchoPacketHandler {
    private Logger logger = LoggerFactory.getLogger(UnhandledHandler.class);
    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) {
       logger.warn("Unhandled packet: " + reader.getCurrentPacketId() + " (" + packet.type.name() + ")");
       return true;
    }

    
    
}
