package com.banchojar.packets.client.handlers;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.banchojar.packets.BanchoPacket;
import com.banchojar.packets.client.BanchoPacketHandler;
import com.banchojar.packets.client.BanchoPacketReader;
import com.banchojar.packets.server.PacketSender;

public class ChannelJoinHandler implements BanchoPacketHandler {

    public Logger logger = LoggerFactory.getLogger(BanchoPacketHandler.class);
    @Override
    public boolean handle(BanchoPacket packet, PacketSender sender, BanchoPacketReader reader, int playerId)  throws IOException {
        String channelName = reader.readString();
        sender.sendChannelJoinSuccess(channelName);   
        logger.info("Channel join: " + channelName + " for playerId: " + playerId);
        return true;
    }   
}
