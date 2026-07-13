package com.osuserverlist.bjar.modules.commands;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.commands.BanchoCommandRegistry.CommandInfo;
import com.osuserverlist.bjar.packets.server.ChatServerPackets.SendMessagePacket;

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
                        "Unknown command: " + commandName + " use !help for a list of commands", target, server.botPlayer.getId()));
            });
            return;
        }

        if (sender.getServerPrivileges() < commandInfo.requiredPrivileges && commandInfo.requiredPrivileges != 0) {
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

    public static void processNp(Player sender, String message) {
        Pattern pattern = Pattern.compile("beatmapsets/(\\d+)#/(\\d+)");
        Matcher matcher = pattern.matcher(message);

        if(matcher.find()) {
            String beatmapId = matcher.group(2);
            String beatmapSetId = matcher.group(1);
            sender.setLastNpBeatmapId(Long.parseLong(beatmapId));
            sender.setLastNpBeatmapSetId(Long.parseLong(beatmapSetId));   
        }
    }

    public static class PlayerCommandInfo {
        public Player player;
        public String target;
    }
}
