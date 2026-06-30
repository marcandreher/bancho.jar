package com.osuserverlist.bjar.commands.administration;

import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.database.UserEntity;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.osu.Privileges;
import com.osuserverlist.bjar.modules.commands.BanchoCommand;
import com.osuserverlist.bjar.modules.commands.BanchoCommandHandler;
import com.osuserverlist.bjar.modules.commands.CommandCategory;
import com.osuserverlist.bjar.modules.commands.BanchoCommandProcessor.PlayerCommandInfo;
import com.osuserverlist.bjar.modules.database.Database;
import com.osuserverlist.bjar.modules.database.MySQL;
import com.osuserverlist.bjar.repos.UserRepository;

import java.util.Arrays;
import java.util.stream.Collectors;

@BanchoCommand(
    name = "!privadd",
    category = CommandCategory.ADMINISTRATION,
    description = "Add privileges to a player",
    requiredPrivileges = Privileges.ADMINISTRATOR
)
public class AddPrivCommand extends BanchoCommandHandler {

    @Override
    public void handle(Player sender, PlayerCommandInfo[] commandInfos, String[] args) {
        if (args.length < 2) {
            sendBotMessage(commandInfos, "Usage: !privadd <username> <privilege>");
            return;
        }

        String username = args[0];
        String privName = args[1].toUpperCase();

        Privileges privilege = Privileges.fromName(privName);

        if (privilege == null) {
            String validPrivileges = Arrays.stream(Privileges.values())
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));

            sendBotMessage(
                    commandInfos,
                    "Invalid privilege '" + privName + "'. Valid privileges: " + validPrivileges
            );
            return;
        }

        try (MySQL mysql = Database.getConnection()) {
            UserRepository userRepo = new UserRepository(mysql);

            Player targetPlayer = Server.getInstance()
                    .playerManager
                    .getByFilter(p -> p.getUsername().equalsIgnoreCase(username));

            if (targetPlayer != null) {
                Server.getInstance().playerManager.addPriv(targetPlayer, privilege);

                userRepo.updateUserPrivileges(
                        targetPlayer.getId(),
                        targetPlayer.getServerPrivileges()
                );
            } else {
                UserEntity user = userRepo.getUserByName(username);

                if (user == null) {
                    sendBotMessage(commandInfos, "Player not found: " + username);
                    return;
                }

                userRepo.updateUserPrivileges(
                        user.getId(),
                        user.getPriv() | privilege.getValue()
                );
            }

            sendBotMessage(
                    commandInfos,
                    "Successfully added " + privilege.name() + " to " + username
            );

        } catch (Exception e) {
            e.printStackTrace();

            sendBotMessage(
                    commandInfos,
                    "Failed to add privilege due to an internal error."
            );
        }
    }
}