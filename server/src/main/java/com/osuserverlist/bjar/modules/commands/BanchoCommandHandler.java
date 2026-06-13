package com.osuserverlist.bjar.modules.commands;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.server.handlers.chat.SendMessagePacket;
import com.osuserverlist.bjar.server.Server;

public abstract class BanchoCommandHandler {
    
    public abstract void handle(Player player, String target, String[] args);

    public void sendBotMessage(Player player, String message, String target) {
        Server server = Server.getInstance();
        player.sendPacket(new SendMessagePacket(server.botPlayer.getUsername(), message, target, server.botPlayer.getId()));
    }

}
