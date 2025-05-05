package com.banchojar.packets.client.handlers;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.banchojar.Player;
import com.banchojar.Server;
import com.banchojar.packets.BanchoPacket;
import com.banchojar.packets.client.BanchoPacketHandler;
import com.banchojar.packets.client.BanchoPacketReader;
import com.banchojar.packets.server.PacketSender;

public class LogoutHandler implements BanchoPacketHandler {

     public Logger logger = LoggerFactory.getLogger(BanchoPacketHandler.class);

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        logger.info("Player (" + player.getId() + ") has logged out.");
        
        Server.players.entrySet().removeIf(entry -> entry.getValue() == player);

        return true;
    }
    
}
