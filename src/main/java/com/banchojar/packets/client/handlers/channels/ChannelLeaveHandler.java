package com.banchojar.packets.client.handlers.channels;

import java.io.IOException;
import java.util.Arrays;

import com.banchojar.Player;
import com.banchojar.Server;
import com.banchojar.packets.BanchoPacket;
import com.banchojar.packets.client.BanchoPacketHandler;
import com.banchojar.packets.client.BanchoPacketReader;
import com.banchojar.packets.server.BanchoChannel;
import com.banchojar.packets.server.handlers.ChannelInfoHandler;

public class ChannelLeaveHandler implements BanchoPacketHandler {

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        String channelName = reader.readString();

        if (Arrays.asList("#osu", "#lobby").contains(channelName)) {
            return true; 
        }

        BanchoChannel channel = Server.channels.get(channelName);

        channel.getPlayers().remove(player);
        player.addPacketToStack(new ChannelInfoHandler(channel.getName(), channel.getDescription(), (short)channel.getPlayerCount()));

        return true;
    }
    
}
