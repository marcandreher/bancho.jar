package com.osuserverlist.bjar.packets.client.handlers.chat;

import java.io.IOException;

import org.slf4j.Logger;

import com.osuserverlist.bjar.models.essentials.BanchoChannel;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.logger.LoggerFactory;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.client.BanchoPacketHandler;
import com.osuserverlist.bjar.packets.client.BanchoPacketReader;
import com.osuserverlist.bjar.packets.client.ClientPackets;
import com.osuserverlist.bjar.packets.client.engine.ClientPacket;
import com.osuserverlist.bjar.packets.server.handlers.chat.SendMessagePacket;
import com.osuserverlist.bjar.server.Server;

@ClientPacket(ClientPackets.SEND_PUBLIC_MESSAGE)
public class SendPublicMessagePacket implements BanchoPacketHandler {
    private final Logger logger = LoggerFactory.getLogger(SendPublicMessagePacket.class);

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        reader.readString(); // senderName
        String message = reader.readString();
        String target = reader.readString();
        logger.info("Message from <{}>: <{}> to <{}>", player.getUsername(), message, target);

        BanchoChannel channel = Server.getInstance().channelManager.get(target);
        if (channel == null) {
            logger.warn("Channel not found for target: " + target);
            return true;
        }

        for (Player member : channel.getPlayers()) {
            if(member.isBot()) continue;
            if(member.getId() == player.getId()) continue; // Don't send the message back to the sender
            member.sendPacket(new SendMessagePacket(player.getUsername(), message, target, player.getId()));
        }

        return true;
    }
}