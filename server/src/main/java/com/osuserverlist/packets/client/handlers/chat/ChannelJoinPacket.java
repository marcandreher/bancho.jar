package com.osuserverlist.packets.client.handlers.chat;

import java.io.IOException;

import org.slf4j.Logger;

import com.osuserverlist.main.Server;
import com.osuserverlist.models.essentials.BanchoChannel;
import com.osuserverlist.models.essentials.Player;
import com.osuserverlist.modules.logger.LoggerFactory;
import com.osuserverlist.packets.BanchoPacket;
import com.osuserverlist.packets.client.BanchoPacketHandler;
import com.osuserverlist.packets.client.BanchoPacketReader;
import com.osuserverlist.packets.server.handlers.channel.ChannelJoinSuccessHandler;

public class ChannelJoinPacket implements BanchoPacketHandler {
    
    private final static Logger logger = LoggerFactory.getLogger(ChannelJoinPacket.class);

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        String channelName = reader.readString();

        BanchoChannel channel = Server.getInstance().channelManager.get(channelName);
        if (channel == null) {
            logger.warn("Player {} tried to join a not existing channel", player.toString());
            return true;
        }
        player.sendPacket(new ChannelJoinSuccessHandler(channelName));

        Server.getInstance().channelManager.joinChannel(channelName, player);
        logger.info("Player {} joined channel {}", player.toString(), channelName);
        return true;
    }

}
