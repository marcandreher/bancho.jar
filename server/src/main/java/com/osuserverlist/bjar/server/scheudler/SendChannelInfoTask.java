package com.osuserverlist.bjar.server.scheudler;

import com.osuserverlist.bjar.packets.server.handlers.channel.ChannelInfoPacket;
import com.osuserverlist.bjar.server.Server;

public class SendChannelInfoTask implements Runnable {

    @Override
    public void run() {
        Server server = Server.getInstance();

        server.channelManager.getAll().forEach(channel -> {
            if(!channel.isDirty()) return;
            if(channel.getName() == "#lobby") return; // don't send channel info for


            server.playerManager.getAll().forEach(player -> {
                if(channel.getReadPriv() > player.getServerPrivileges()) return; // don't send channel info to players that can't see the channel
                player.sendPacket(new ChannelInfoPacket(channel.getName(), channel.getDescription(), channel.getPlayerCount()));
            });

            channel.setDirty(false);
        });
    }

}
