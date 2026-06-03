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

public class ChannelLeavePacket implements BanchoPacketHandler {

    private final static Logger logger = LoggerFactory.getLogger(ChannelLeavePacket.class);

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        String channelName = reader.readString();

        BanchoChannel channel = Server.getInstance().channelManager.get(channelName);
        if (channel == null) {
            logger.warn("Player {} tried to leave a not existing channel", player.toString());
            return true;
        }

        Server.getInstance().channelManager.leaveChannel(channelName, player);
        logger.info("Player {} left channel {}", player.toString(), channelName);
        return true;
    }
    
}
