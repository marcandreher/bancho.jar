package com.osuserverlist.bjar.packets.client.handlers.chat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.essentials.BanchoChannel;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.commands.BanchoCommandProcessor;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.client.BanchoPacketHandler;
import com.osuserverlist.bjar.packets.client.BanchoPacketReader;
import com.osuserverlist.bjar.packets.client.ClientPackets;
import com.osuserverlist.bjar.packets.client.engine.ClientPacket;
import com.osuserverlist.bjar.packets.server.handlers.chat.SendMessagePacket;

@ClientPacket(ClientPackets.SEND_PUBLIC_MESSAGE)
public class SendPublicMessagePacket implements BanchoPacketHandler {
    
    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        reader.readString(); // senderName
        String message = reader.readString();
        String target = reader.readString();
        

        BanchoChannel channel = resolveChannel(player, target);
        if (channel == null) {
            logger.warn("Player {} sent a message to a non-existing channel {}", player, target);
            return true;
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

        logger.info("Message from {}: <{}> to <{}>", player.toString(), message, channel.getName());

        return true;
    }

    public BanchoChannel resolveChannel(Player player, String target) {
        Server server = Server.getInstance();

        if ("#spectator".equals(target)) {
            if (player.getSpectating() == null) {
                logger.warn("Player is not spectating anyone but sent a message to #spectator");
                return null;
            }

            return server.channelManager.get("#spec_" + player.getSpectating().getId());
        }

        if ("#multiplayer".equals(target)) {
            if (player.getMatch() == null) {
                logger.warn("Player is not in a match but sent a message to #multiplayer");
                return null;
            }

            return server.channelManager.get("#multi_" + player.getMatch().getMatchId());
        }

        BanchoChannel channel = server.channelManager.get(target);
        if (channel == null) {
            logger.warn("Channel not found for target: {}", target);
        }

        return channel;
    }
}