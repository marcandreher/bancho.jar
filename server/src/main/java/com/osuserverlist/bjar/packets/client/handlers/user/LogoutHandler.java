package com.osuserverlist.bjar.packets.client.handlers.user;

import java.io.IOException;

import org.slf4j.Logger;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.logger.LoggerFactory;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.client.BanchoPacketHandler;
import com.osuserverlist.bjar.packets.client.BanchoPacketReader;
import com.osuserverlist.bjar.server.Server;

public class LogoutHandler implements BanchoPacketHandler {

    private static final Logger logger = LoggerFactory.getLogger(LogoutHandler.class);

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        logger.info("Player {} has logged out.", player.toString());

        Server.getInstance().playerManager.disconnect(player);
        return true;
    }

}
