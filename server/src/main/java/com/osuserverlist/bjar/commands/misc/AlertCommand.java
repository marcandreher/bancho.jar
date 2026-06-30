package com.osuserverlist.bjar.commands.misc;

import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.osu.Privileges;
import com.osuserverlist.bjar.modules.commands.BanchoCommand;
import com.osuserverlist.bjar.modules.commands.BanchoCommandHandler;
import com.osuserverlist.bjar.modules.commands.CommandCategory;
import com.osuserverlist.bjar.modules.commands.BanchoCommandProcessor.PlayerCommandInfo;
import com.osuserverlist.bjar.packets.server.handlers.util.NotificationPacket;

@BanchoCommand(
    name = "!alert", 
    category = CommandCategory.MISC, 
    description = "Alert all players with a message", 
    requiredPrivileges = Privileges.ADMINISTRATOR
)
public class AlertCommand extends BanchoCommandHandler {
    
    @Override
    public void handle(Player sender, PlayerCommandInfo[] commandInfos, String[] args) {
        
        if(args.length == 0) {
            sendBotMessage(commandInfos, "Usage: !alert <message>");
            return;
        }

        String message = String.join(" ", args);

        Server.getInstance().playerManager.getAll().forEach(player -> {
            player.sendPacket(new NotificationPacket(message));
        });
    }

}
