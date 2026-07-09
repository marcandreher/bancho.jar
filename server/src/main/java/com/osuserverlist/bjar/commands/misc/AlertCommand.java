package com.osuserverlist.bjar.commands.misc;

import org.slf4j.Logger;

import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.osu.Privileges;
import com.osuserverlist.bjar.modules.commands.BanchoCommand;
import com.osuserverlist.bjar.modules.commands.BanchoCommandHandler;
import com.osuserverlist.bjar.modules.commands.CommandCategory;
import com.osuserverlist.bjar.modules.commands.BanchoCommandProcessor.PlayerCommandInfo;
import com.osuserverlist.bjar.modules.logger.LoggerFactory;
import com.osuserverlist.bjar.packets.server.handlers.util.NotificationPacket;

@BanchoCommand(
    name = "!alert", 
    category = CommandCategory.MISC, 
    description = "Alert all players with a message", 
    requiredPrivileges = Privileges.ADMINISTRATOR
)
public class AlertCommand extends BanchoCommandHandler {
    
    private final static Logger logger = LoggerFactory.getLogger(AlertCommand.class);

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

        logger.info("Alert sent by {}: {}", sender.toString(), message);

        sendBotMessage(commandInfos, "Alert sent to all players: " + message);
    }

}
