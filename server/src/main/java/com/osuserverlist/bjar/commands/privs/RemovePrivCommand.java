package com.osuserverlist.bjar.commands.privs;

import com.osuserverlist.bjar.models.database.UserEntity;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.osu.Privileges;
import com.osuserverlist.bjar.modules.commands.BanchoCommand;
import com.osuserverlist.bjar.modules.commands.BanchoCommandHandler;
import com.osuserverlist.bjar.modules.commands.BanchoCommandProcessor.PlayerCommandInfo;
import com.osuserverlist.bjar.modules.database.Database;
import com.osuserverlist.bjar.modules.database.MySQL;
import com.osuserverlist.bjar.repos.UserRepository;
import com.osuserverlist.bjar.server.Server;

import java.util.Arrays;
import java.util.stream.Collectors;

@BanchoCommand(
    name = "!rmpriv",
    description = "Remove privileges from a player",
    requiredPrivileges = Privileges.ADMINISTRATOR
)
public class RemovePrivCommand extends BanchoCommandHandler {

    @Override
    public void handle(Player sender, PlayerCommandInfo[] commandInfos, String[] args) {
        if (args.length < 2) {
            sendBotMessage(commandInfos, "Usage: !rmpriv <username> <privilege>");
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
                Server.getInstance().playerManager.removePriv(targetPlayer, privilege);

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
                        user.getPriv() & ~privilege.getValue()
                );
            }

            sendBotMessage(
                    commandInfos,
                    "Successfully removed " + privilege.name() + " from " + username
            );

        } catch (Exception e) {
            e.printStackTrace();

            sendBotMessage(
                    commandInfos,
                    "Failed to remove privilege due to an internal error."
            );
        }
    }
}