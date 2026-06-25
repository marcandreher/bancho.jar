package com.osuserverlist.bjar.packets.client.handlers.chat;

import java.io.IOException;

import org.slf4j.Logger;

import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.essentials.BanchoChannel;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.logger.LoggerFactory;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.client.BanchoPacketHandler;
import com.osuserverlist.bjar.packets.client.BanchoPacketReader;
import com.osuserverlist.bjar.packets.client.ClientPackets;
import com.osuserverlist.bjar.packets.client.engine.ClientPacket;
import com.osuserverlist.bjar.packets.server.handlers.channel.ChannelJoinSuccessPacket;

@ClientPacket(ClientPackets.CHANNEL_JOIN)
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
        
        if(channel.getReadPriv() > player.getServerPrivileges()) {
            logger.warn("Player {} tried to join channel {} without sufficient privileges", player.toString(), channelName);
            return true;
        }

        player.sendPacket(new ChannelJoinSuccessPacket(channelName));

        Server.getInstance().channelManager.joinChannel(channelName, player);
        logger.info("Player {} joined channel {}", player.toString(), channelName);
        return true;
    }

}
