package com.osuserverlist.bjar.packets.client.handlers.spectate;

import java.io.IOException;

import com.osuserverlist.bjar.models.essentials.BanchoChannel;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.client.BanchoPacketHandler;
import com.osuserverlist.bjar.packets.client.BanchoPacketReader;
import com.osuserverlist.bjar.packets.client.ClientPackets;
import com.osuserverlist.bjar.packets.client.engine.ClientPacket;
import com.osuserverlist.bjar.packets.server.handlers.channel.ChannelJoinSuccessPacket;
import com.osuserverlist.bjar.packets.server.handlers.channel.ChannelRevokedPacket;
import com.osuserverlist.bjar.packets.server.handlers.channel.ChannelInfoPacket;
import com.osuserverlist.bjar.packets.server.handlers.spectate.FellowSpectatorJoinedPacket;
import com.osuserverlist.bjar.packets.server.handlers.spectate.SpectatorJoinedPacket;

import com.osuserverlist.bjar.server.Server;

@ClientPacket(ClientPackets.START_SPECTATING)
public class StartSpectatePacket implements BanchoPacketHandler {

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        int targetUserId = reader.readInt();

        Server server = Server.getInstance();

        Player newHost = server.playerManager.getById(targetUserId);
        if (newHost == null) {
            return true;
        }

        // If already spectating someone else, stop spectating them first.
        Player currentHost = player.getSpectating();
        if (currentHost != null) {
            if (currentHost == newHost) {
                return true;
            }

            currentHost.getSpectators().remove(player);

            String oldChannelName = "#spec_" + currentHost.getId();
            BanchoChannel oldChannel = server.channelManager.get(oldChannelName);

            if (oldChannel != null) {
                server.channelManager.forceLeaveChannel(oldChannelName, player);
                player.sendPacket(new ChannelRevokedPacket(oldChannelName));

                if (currentHost.getSpectators().isEmpty()) {
                    server.channelManager.forceLeaveChannel(oldChannelName, currentHost);
                    server.channelManager.removeChannel(oldChannelName);
                }
            }

            player.setSpectating(null);
        }

        String channelName = "#spec_" + newHost.getId();
        BanchoChannel channel = server.channelManager.get(channelName);

        if (channel == null) {
            channel = new BanchoChannel(
                    channelName,
                    channelName,
                    "Spectator channel for " + newHost.getUsername(),
                    false,
                    false,
                    0,
                    0,
                    false);

            server.channelManager.add(channel);

            server.channelManager.forceJoinChannel(channelName, newHost);
            newHost.sendPacket(new ChannelJoinSuccessPacket(channelName));
            newHost.sendPacket(
                    new ChannelInfoPacket(
                            channelName,
                            channel.getDescription(),
                            (short) channel.getPlayerCount()));

        }

        // Join spectator channel
        server.channelManager.forceJoinChannel(channelName, player);

        player.sendPacket(new ChannelJoinSuccessPacket(channelName));
        player.sendPacket(
                new ChannelInfoPacket(
                        channelName,
                        channel.getDescription(),
                        (short) channel.getPlayerCount()));

        if (!player.isStealth()) {
            // Existing spectators -> joining spectator
            for (Player spectator : newHost.getSpectators()) {
                player.sendPacket(
                        new FellowSpectatorJoinedPacket(
                                spectator.getId()));
            }

            // Joining spectator -> existing spectators
            FellowSpectatorJoinedPacket joinedPacket = new FellowSpectatorJoinedPacket(player.getId());

            for (Player spectator : newHost.getSpectators()) {
                spectator.sendPacket(joinedPacket);
            }

            // Notify host
            newHost.sendPacket(
                    new SpectatorJoinedPacket(player.getId()));
        }

        newHost.getSpectators().add(player);
        player.setSpectating(newHost);

        return true;
    }
}