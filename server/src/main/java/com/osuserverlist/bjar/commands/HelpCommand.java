package com.osuserverlist.bjar.commands;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.commands.BanchoCommand;
import com.osuserverlist.bjar.modules.commands.BanchoCommandHandler;
import com.osuserverlist.bjar.modules.commands.BanchoCommandProcessor.PlayerCommandInfo;
import com.osuserverlist.bjar.modules.commands.BanchoCommandRegistry;

@BanchoCommand(name = "!help", description = "Lists all available commands with their descriptions.")
public class HelpCommand extends BanchoCommandHandler {
    
    @Override
    public void handle(Player sender, PlayerCommandInfo[] commandInfos, String[] args) {
        
        BanchoCommandRegistry.getAllCommands().forEach(commandInfo -> {
            if(!(sender.getServerPrivileges() >= commandInfo.requiredPrivileges)) {
                return;
            }

            String helpMessage = String.format("%s: %s", commandInfo.name, commandInfo.description);
            
            sendBotMessage(commandInfos, helpMessage.toString());
        });
    }

}
