package com.osuserverlist.packets.client.handlers;

import org.slf4j.Logger;

import com.osuserverlist.models.essentials.Player;
import com.osuserverlist.modules.logger.LoggerFactory;
import com.osuserverlist.packets.BanchoPacket;
import com.osuserverlist.packets.client.BanchoPacketHandler;
import com.osuserverlist.packets.client.BanchoPacketReader;

public class UnhandledHandler implements BanchoPacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(UnhandledHandler.class);

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) {
        logger.warn("Unhandled packet: " + reader.getCurrentPacketId() + " (" + packet.type.name() + ")");
        return true;
    }

}