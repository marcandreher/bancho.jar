package com.osuserverlist.bjar.server.scheudler;

import com.osuserverlist.bjar.models.essentials.BanchoChannel;
import com.osuserverlist.bjar.packets.server.handlers.channel.ChannelInfoPacket;
import com.osuserverlist.bjar.server.Server;

public class SendChannelInfoTask implements Runnable {
    @Override
    public void run() {

        for(BanchoChannel channel : Server.getInstance().channelManager.getAll()) {
            if(channel.isDirty()) {
                if(channel.getName() == "#lobby") continue; // don't send channel info for the lobby
                Server.getInstance().playerManager.getAll().forEach(player -> {
                    player.sendPacket(new ChannelInfoPacket(channel.getName(), channel.getDescription(), (short) channel.getPlayerCount()));
                });
                channel.setDirty(false);
            }
        }

    }
}
