package com.osuserverlist.bjar.commands.misc;

import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.osu.Privileges;
import com.osuserverlist.bjar.modules.commands.BanchoCommand;
import com.osuserverlist.bjar.modules.commands.BanchoCommandHandler;
import com.osuserverlist.bjar.modules.commands.BanchoCommandProcessor.PlayerCommandInfo;
import com.osuserverlist.bjar.modules.commands.CommandCategory;

@BanchoCommand(
    name = "!kick", 
    category = CommandCategory.MISC, 
    description = "Kick a player from the server", 
    requiredPrivileges = Privileges.ADMINISTRATOR
)
public class KickCommand extends BanchoCommandHandler {
    
    @Override
    public void handle(Player sender, PlayerCommandInfo[] commandInfos, String[] args) {
        
        if(args.length == 0) {
            sendBotMessage(commandInfos, "Usage: !kick <username>");
            return;
        }

        String username = args[0];
        Server server = Server.getInstance();

        Player targetPlayer = server.playerManager.getByFilter(p -> p.getUsername().equalsIgnoreCase(username));

        if (targetPlayer == null) {
            sendBotMessage(commandInfos, "Player not found: " + username);
            return;
        }

        server.playerManager.disconnect(targetPlayer);
        sendBotMessage(commandInfos, "Player " + username + " has been kicked from the server.");
        
    }

}
