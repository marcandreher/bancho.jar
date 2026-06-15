package com.osuserverlist.bjar.packets.client.handlers.spectate;

import java.io.IOException;

import com.osuserverlist.bjar.models.essentials.BanchoChannel;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.client.BanchoPacketHandler;
import com.osuserverlist.bjar.packets.client.BanchoPacketReader;
import com.osuserverlist.bjar.packets.client.ClientPackets;
import com.osuserverlist.bjar.packets.client.engine.ClientPacket;
import com.osuserverlist.bjar.packets.server.handlers.channel.ChannelRevokedPacket;
import com.osuserverlist.bjar.packets.server.handlers.spectate.FellowSpectatorLeftPacket;
import com.osuserverlist.bjar.packets.server.handlers.spectate.SpectatorLeftPacket;
import com.osuserverlist.bjar.server.Server;
import com.osuserverlist.bjar.packets.server.handlers.channel.ChannelInfoPacket;

@ClientPacket(ClientPackets.STOP_SPECTATING)
public class StopSpectatePacket implements BanchoPacketHandler {

    @Override
    public boolean handle(
            BanchoPacket packet,
            BanchoPacketReader reader,
            Player player) throws IOException {

        Player host = player.getSpectating();
        Server server = Server.getInstance();

        if (host == null) {
            return true;
        }

        host.getSpectators().remove(player);
        player.setSpectating(null);

        host.sendPacket(new SpectatorLeftPacket(player.getId()));

        String channelName = "#spec_" + host.getId();
        BanchoChannel channel = server.channelManager.get(channelName);

        if (channel != null) {
            server.channelManager.forceLeaveChannel(channelName, player);
            player.sendPacket(new ChannelRevokedPacket(channel.getAlias()));

            ChannelInfoPacket infoPacket = new ChannelInfoPacket(
                    channel.getAlias(),
                    channel.getDescription(),
                    (short) channel.getPlayerCount());

            FellowSpectatorLeftPacket leftPacket = new FellowSpectatorLeftPacket(player.getId());

            for (Player spectator : host.getSpectators()) {
                spectator.sendPacket(leftPacket);
                spectator.sendPacket(infoPacket);
            }

            host.sendPacket(infoPacket);

            if (host.getSpectators().isEmpty()) {
                server.channelManager.forceLeaveChannel(channelName, host);
                host.sendPacket(new ChannelRevokedPacket(channel.getAlias()));
                server.channelManager.removeChannel(channelName);
                player.sendPacket(new ChannelRevokedPacket(channel.getAlias()));
            }

        }
        return true;
    }

}
