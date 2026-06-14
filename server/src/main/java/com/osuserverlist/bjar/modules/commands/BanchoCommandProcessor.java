package com.osuserverlist.bjar.modules.commands;

import java.util.List;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.commands.BanchoCommandRegistry.CommandInfo;
import com.osuserverlist.bjar.packets.server.handlers.chat.SendMessagePacket;
import com.osuserverlist.bjar.server.Server;

public class BanchoCommandProcessor {

    public static void processCommand(Player sender, String commandLine, String target, List<Player> recievers) {
        if (!(commandLine.startsWith("!") || commandLine.startsWith("/"))) {
            return;
        }

        String[] command = commandLine.split(" ");
        String commandName = command[0].toLowerCase();

        Server server = Server.getInstance();
        CommandInfo commandInfo = BanchoCommandRegistry.getCommand(commandName);
        if (commandInfo == null) {
            recievers.forEach(player -> {
                player.sendPacket(new SendMessagePacket(server.botPlayer.getUsername(),
                        "Unknown command: " + commandName, target, server.botPlayer.getId()));
            });
            return;
        }

        if (sender.getServerPrivileges() < commandInfo.requiredPrivileges) {
            recievers.forEach(player -> {
                player.sendPacket(new SendMessagePacket(server.botPlayer.getUsername(),
                        "You don't have permission to use this command.", target, server.botPlayer.getId()));
            });
            return;
        }

        PlayerCommandInfo[] targetArray = recievers.stream().map(player -> {
            PlayerCommandInfo info = new PlayerCommandInfo();
            info.player = player;
            info.target = target;
            return info;
        }).toArray(PlayerCommandInfo[]::new);

        String[] args = new String[command.length - 1];
        System.arraycopy(command, 1, args, 0, args.length);
        commandInfo.handler.handle(sender, targetArray, args);
    }

    public static class PlayerCommandInfo {
        public Player player;
        public String target;
    }
}
