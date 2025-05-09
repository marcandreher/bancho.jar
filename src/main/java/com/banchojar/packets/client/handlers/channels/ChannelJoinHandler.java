package com.banchojar.packets.client.handlers.channels;

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.banchojar.Player;
import com.banchojar.Server;
import com.banchojar.packets.BanchoPacket;
import com.banchojar.packets.client.BanchoPacketHandler;
import com.banchojar.packets.client.BanchoPacketReader;
import com.banchojar.packets.server.BanchoChannel;
import com.banchojar.packets.server.handlers.ChannelInfoHandler;
import com.banchojar.packets.server.handlers.ChannelJoinSuccessHandler;

public class ChannelJoinHandler implements BanchoPacketHandler {

    public Logger logger = LoggerFactory.getLogger(BanchoPacketHandler.class);
    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player)  throws IOException {
        String channelName = reader.readString();
        player.addPacketToStack(new ChannelJoinSuccessHandler(channelName));

          if (Arrays.asList("#osu", "#lobby").contains(channelName)) {
            return true; 
        }
        


        BanchoChannel channel = Server.channels.get(channelName);
        channel.getPlayers().add(player);
        player.addPacketToStack(new ChannelInfoHandler(channel.getName(), channel.getDescription(), (short)channel.getPlayerCount() ));

        return true;
    }   
}
