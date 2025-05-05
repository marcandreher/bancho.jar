package com.banchojar.packets.client.handlers;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.banchojar.Player;
import com.banchojar.packets.BanchoPacket;
import com.banchojar.packets.client.BanchoPacketHandler;
import com.banchojar.packets.client.BanchoPacketReader;
import com.banchojar.packets.server.PacketSender;
import com.banchojar.packets.server.handlers.ChannelJoinSuccessHandler;

public class ChannelJoinHandler implements BanchoPacketHandler {

    public Logger logger = LoggerFactory.getLogger(BanchoPacketHandler.class);
    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player)  throws IOException {
        String channelName = reader.readString();
        player.addPacketToStack(new ChannelJoinSuccessHandler(channelName));
        logger.info("Channel join: " + channelName + " for playerId: " + player.getId());
        return true;
    }   
}
