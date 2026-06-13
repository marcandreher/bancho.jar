package com.osuserverlist.bjar.commands;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.osu.Privileges;
import com.osuserverlist.bjar.modules.commands.BanchoCommand;
import com.osuserverlist.bjar.modules.commands.BanchoCommandHandler;

@BanchoCommand(name = "!rank", requiredPrivileges = Privileges.NOMINATOR)
public class RankMapCommand extends BanchoCommandHandler {
    @Override
    public void handle(Player player, String target, String[] args) {
        sendBotMessage(player, "This command is not implemented", target);
    }
}
