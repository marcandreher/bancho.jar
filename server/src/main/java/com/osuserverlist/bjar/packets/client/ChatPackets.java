package com.osuserverlist.bjar.packets.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.essentials.BanchoChannel;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.BanchoPacketReader;
import com.osuserverlist.bjar.modules.ClientPacketEngine.ClientPacket;
import com.osuserverlist.bjar.modules.ClientPacketEngine.ClientPackets;
import com.osuserverlist.bjar.modules.commands.BanchoCommandProcessor;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.server.ChatServerPackets.ChannelJoinSuccessPacket;
import com.osuserverlist.bjar.packets.server.ChatServerPackets.SendMessagePacket;

public class ChatPackets {

    private static final Logger logger = LoggerFactory.getLogger(ChatPackets.class);

    @ClientPacket(ClientPackets.CHANNEL_JOIN)
    public boolean joinChannel(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
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

    @ClientPacket(ClientPackets.CHANNEL_PART)
    public boolean leaveChannel(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
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

    @ClientPacket(ClientPackets.SEND_PUBLIC_MESSAGE)
    public boolean sendPublicMessage(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
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

    @ClientPacket(ClientPackets.SEND_PRIVATE_MESSAGE)
    public boolean sendPrivateMessage(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
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

    public BanchoChannel resolveChannel(Player player, String target) {
        Server server = Server.getInstance();

        if ("#spectator".equals(target)) {
            if(player.getSpectators().size() > 0) {
                return server.channelManager.get("#spec_" + player.getId());
            }
            if (player.getSpectating() != null) {
                return server.channelManager.get("#spec_" + player.getSpectating().getId());
            }

            logger.warn("Player is not spectating anyone but sent a message to #spectator");
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
