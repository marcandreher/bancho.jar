package com.osuserverlist.bjar.packets.client.engine;

import java.io.IOException;

import org.slf4j.Logger;

import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.logger.LoggerFactory;
import com.osuserverlist.bjar.packets.BanchoPacket;

public interface BanchoPacketHandler {
    public static Logger logger = LoggerFactory.getLogger(BanchoPacketHandler.class);

    default Server server() {
        return Server.getInstance();
    }
    boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException;
}