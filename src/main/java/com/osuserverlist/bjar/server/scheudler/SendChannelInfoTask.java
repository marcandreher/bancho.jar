package com.osuserverlist.bjar.server.scheudler;

import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.packets.server.ChatServerPackets.ChannelInfoPacket;

public class SendChannelInfoTask implements Runnable {

    @Override
    public void run() {
        Server server = Server.getInstance();

        server.channelManager.getAll().forEach(channel -> {
            if(!channel.isDirty()) return;

            server.playerManager.getAll().forEach(player -> {
                if(!channel.isVisible()) {
                    if(!channel.getPlayers().contains(player)) {
                        return; // don't send channel info for invisible channels to players that aren't in the channel
                    }
                }
                if(channel.getReadPriv() > player.getServerPrivileges()) return; // don't send channel info to players that can't see the channel
                player.sendPacket(new ChannelInfoPacket(channel.getAlias(), channel.getDescription(), channel.getPlayerCount()));
            });

            channel.setDirty(false);
        });
    }

}
