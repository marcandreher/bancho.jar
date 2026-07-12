package com.osuserverlist.bjar.packets.server;

import org.slf4j.Logger;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.logger.LoggerFactory;
import com.osuserverlist.bjar.packets.BanchoPacket;

public interface ServerPacketHandler {

    static final Logger logger = LoggerFactory.getLogger(ServerPacketHandler.class);

    boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender);
}