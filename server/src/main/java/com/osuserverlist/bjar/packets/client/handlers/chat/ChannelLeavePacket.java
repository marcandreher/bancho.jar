package com.osuserverlist.bjar.packets.client.handlers.chat;

import java.io.IOException;
import java.util.List;

import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.essentials.BanchoChannel;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.client.BanchoPacketHandler;
import com.osuserverlist.bjar.packets.client.BanchoPacketReader;
import com.osuserverlist.bjar.packets.client.ClientPackets;
import com.osuserverlist.bjar.packets.client.engine.ClientPacket;

@ClientPacket(ClientPackets.CHANNEL_PART)
public class ChannelLeavePacket implements BanchoPacketHandler {

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        String channelName = reader.readString();

        if (List.of("#multiplayer", "#spectator").contains(channelName)) {
            return true;
        }

        Server server = Server.getInstance();
        BanchoChannel channel = server.channelManager.get(channelName);
        if (channel == null) {
            logger.warn("Player {} tried to leave a not existing channel {}", player.toString(), channelName);
            return true;
        }

        server.channelManager.leaveChannel(channel.getName(), player);
        logger.info("Player {} left channel {}", player.toString(), channel.getName());
        return true;
    }

}
