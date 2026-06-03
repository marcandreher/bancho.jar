package com.osuserverlist.bjar.packets.client.handlers.chat;

import java.io.IOException;

import org.slf4j.Logger;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.logger.LoggerFactory;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.client.BanchoPacketHandler;
import com.osuserverlist.bjar.packets.client.BanchoPacketReader;
import com.osuserverlist.bjar.packets.server.handlers.chat.SendMessageHandler;
import com.osuserverlist.bjar.server.Server;

public class SendPrivateMessageHandler implements BanchoPacketHandler {
    private final Logger logger = LoggerFactory.getLogger(SendPublicMessageHandler.class);

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        reader.readString(); // senderName
        String message = reader.readString();
        String target = reader.readString();

        logger.info("Private Message from <{}>: <{}> to <{}>", player.getUsername(), message, target);

        Player targetPlayer = Server.getInstance().playerManager.getByFilter(p -> p.getUsername().equalsIgnoreCase(target));

        if (targetPlayer == null) {
            logger.warn("Target player not found for private message: " + target);
            return true;
        }

        targetPlayer.sendPacket(new SendMessageHandler(player.getUsername(), message, target, player.getId()));

        return true;
    }

}
