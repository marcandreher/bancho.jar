package com.osuserverlist.bjar.packets.client.handlers.chat;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.commands.BanchoCommandProcessor;
import com.osuserverlist.bjar.modules.logger.LoggerFactory;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.client.BanchoPacketHandler;
import com.osuserverlist.bjar.packets.client.BanchoPacketReader;
import com.osuserverlist.bjar.packets.client.ClientPackets;
import com.osuserverlist.bjar.packets.client.engine.ClientPacket;
import com.osuserverlist.bjar.packets.server.handlers.chat.SendMessagePacket;
import com.osuserverlist.bjar.server.Server;

@ClientPacket(ClientPackets.SEND_PRIVATE_MESSAGE)
public class SendPrivateMessagePacket implements BanchoPacketHandler {
    private final Logger logger = LoggerFactory.getLogger(SendPrivateMessagePacket.class);

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

        targetPlayer.sendPacket(new SendMessagePacket(player.getUsername(), message, target, player.getId()));

        BanchoCommandProcessor.processNp(player, message);
        BanchoCommandProcessor.processCommand(player, message, target, List.of(player));

        return true;
    }

}
