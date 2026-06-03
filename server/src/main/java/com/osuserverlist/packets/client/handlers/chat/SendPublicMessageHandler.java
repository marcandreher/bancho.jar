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
import com.osuserverlist.packets.server.handlers.chat.SendMessageHandler;

public class SendPublicMessageHandler implements BanchoPacketHandler {
    private final Logger logger = LoggerFactory.getLogger(SendPublicMessageHandler.class);

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
            member.sendPacket(new SendMessageHandler(player.getUsername(), message, target, player.getId()));
        }

        return true;
    }
}