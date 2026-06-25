package com.osuserverlist.bjar.packets.client.handlers.chat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.essentials.BanchoChannel;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.commands.BanchoCommandProcessor;
import com.osuserverlist.bjar.modules.logger.LoggerFactory;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.client.BanchoPacketHandler;
import com.osuserverlist.bjar.packets.client.BanchoPacketReader;
import com.osuserverlist.bjar.packets.client.ClientPackets;
import com.osuserverlist.bjar.packets.client.engine.ClientPacket;
import com.osuserverlist.bjar.packets.server.handlers.chat.SendMessagePacket;

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
        if (channel == null && !target.equals("#spectator")) {
            logger.warn("Channel not found for target: " + target);
            return true;
        }

        if(target.equals("#spectator")) {
            channel = Server.getInstance().channelManager.get("#spec_" + player.getSpectating().getId());
            if(channel == null) {
                logger.warn("Player is not spectating anyone but sent a message to #spectator");
                return true;
            }
        }

        List<Player> players = new ArrayList<>(channel.getPlayers());

        players.forEach(user -> {
            if(user.isBot())
                return;
            if(user.getId() == player.getId())
                return; // Don't send the message back to the sender
            user.sendPacket(new SendMessagePacket(player.getUsername(), message, target, player.getId()));
        });

        BanchoCommandProcessor.processNp(player, message);
        BanchoCommandProcessor.processCommand(player, message, target, players);

        return true;
    }
}