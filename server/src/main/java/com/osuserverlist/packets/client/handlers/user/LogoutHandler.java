package com.osuserverlist.packets.client.handlers.user;

import java.io.IOException;

import org.slf4j.Logger;

import com.osuserverlist.main.Server;
import com.osuserverlist.models.essentials.Player;
import com.osuserverlist.modules.logger.LoggerFactory;
import com.osuserverlist.packets.BanchoPacket;
import com.osuserverlist.packets.client.BanchoPacketHandler;
import com.osuserverlist.packets.client.BanchoPacketReader;

public class LogoutHandler implements BanchoPacketHandler {

    private static final Logger logger = LoggerFactory.getLogger(LogoutHandler.class);

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        logger.info("Player {} has logged out.", player.toString());

        Server.getInstance().playerManager.disconnect(player);
        return true;
    }

}
