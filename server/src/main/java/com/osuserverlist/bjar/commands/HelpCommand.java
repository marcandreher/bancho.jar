package com.osuserverlist.bjar.commands;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.commands.BanchoCommand;
import com.osuserverlist.bjar.modules.commands.BanchoCommandHandler;
import com.osuserverlist.bjar.modules.commands.BanchoCommandProcessor.PlayerCommandInfo;
import com.osuserverlist.bjar.modules.commands.BanchoCommandRegistry.CommandInfo;
import com.osuserverlist.bjar.modules.commands.BanchoCommandRegistry;
import com.osuserverlist.bjar.modules.commands.CommandCategory;

@BanchoCommand(name = "!help", category = CommandCategory.GENERAL, description = "Lists all available commands with their descriptions.")
public class HelpCommand extends BanchoCommandHandler {

    @Override
    public void handle(Player sender, PlayerCommandInfo[] commandInfos, String[] args) {
        Map<CommandCategory, List<CommandInfo>> commandsByCategory = BanchoCommandRegistry.getAllCommands()
                .stream()
                .filter(commandInfo -> sender.getServerPrivileges() >= commandInfo.requiredPrivileges)
                .sorted(Comparator.comparing((CommandInfo c) -> c.name)) // name first, so each group is alphabetical
                .collect(Collectors.groupingBy(
                        commandInfo -> commandInfo.category,
                        TreeMap::new, // sorts categories by their natural (enum/name) order
                        Collectors.toList()));

        if (commandsByCategory.isEmpty()) {
            sendBotMessage(commandInfos, "No commands available.");
            return;
        }

        commandsByCategory.forEach((category, commands) -> {
            sendBotMessage(commandInfos, String.format("== %s ==", category));

            for (CommandInfo commandInfo : commands) {
                String helpMessage = String.format("  %s - %s", commandInfo.name, commandInfo.description);
                sendBotMessage(commandInfos, helpMessage);
            }
        });
    }

}