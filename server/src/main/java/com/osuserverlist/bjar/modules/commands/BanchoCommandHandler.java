package com.osuserverlist.bjar.modules.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.commands.BanchoCommandProcessor.PlayerCommandInfo;
import com.osuserverlist.bjar.packets.server.ChatServerPackets.SendMessagePacket;

public class BanchoCommandHandler {

    public static Logger logger = LoggerFactory.getLogger(BanchoCommandHandler.class);

    public static Server server;

    public void handle(Player sender, PlayerCommandInfo[] commandInfos, String[] args) {
    }

    public void sendBotMessage(PlayerCommandInfo[] commandInfos, String message) {
        Server server = Server.getInstance();
        for (PlayerCommandInfo info : commandInfos) {
            info.player.sendPacket(new SendMessagePacket(server.botPlayer.getUsername(), message, info.target, server.botPlayer.getId()));
        }
    }

}
