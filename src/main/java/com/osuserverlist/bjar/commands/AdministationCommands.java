package com.osuserverlist.bjar.commands;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.osuserverlist.bjar.models.database.UserEntity;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.osu.Privileges;
import com.osuserverlist.bjar.modules.datastore.Database;
import com.osuserverlist.bjar.modules.datastore.MySQL;
import com.osuserverlist.bjar.modules.main.Commands.BanchoCommand;
import com.osuserverlist.bjar.modules.main.Commands.BanchoCommandHandler;
import com.osuserverlist.bjar.modules.main.Commands.CommandCategory;
import com.osuserverlist.bjar.modules.main.Commands.Session;
import com.osuserverlist.bjar.repos.UserRepository;

public class AdministationCommands extends BanchoCommandHandler {

    @BanchoCommand(
            name = "!privadd",
            category = CommandCategory.ADMINISTRATION,
            description = "Add privileges to a player",
            requiredPrivileges = Privileges.ADMINISTRATOR
    )
    public void addPrivilege(Player sender, Session session, String[] args) {
        handlePrivilegeChange(session, args, true);
    }

    @BanchoCommand(
            name = "!privrm",
            category = CommandCategory.ADMINISTRATION,
            description = "Remove privileges from a player",
            requiredPrivileges = Privileges.ADMINISTRATOR
    )
    public void removePrivilege(Player sender, Session session, String[] args) {
        handlePrivilegeChange(session, args, false);
    }

    private void handlePrivilegeChange(Session session, String[] args, boolean grant) {
        if (args.length < 2) {
            session.sendAnswer(usageMessage(grant));
            return;
        }

        String username = args[0];
        String privName = args[1].toUpperCase();
        Privileges privilege = Privileges.fromName(privName);

        if (privilege == null) {
            session.sendAnswer(invalidPrivilegeMessage(privName));
            return;
        }

        try (MySQL mysql = Database.getConnection()) {
            UserRepository userRepo = new UserRepository(mysql);

            Player targetPlayer = session.server.playerManager
                    .getByFilter(player -> player.getUsername().equalsIgnoreCase(username));
            int userId = targetPlayer != null ? targetPlayer.getId() : 0;
            if (targetPlayer != null) {
                updateOnlinePlayer(userRepo, targetPlayer, privilege, grant, session);
            } else {
                UserEntity user = userRepo.getUserByName(username);

                if (user == null) {
                    session.sendAnswer("Player not found: " + username);
                    return;
                }

                userId = user.getId();
                updateOfflineUser(userRepo, user, privilege, grant);
            }

            logger.info("Privilege change: {} {} privilege {} for user <{}>({})", grant ? "Added" : "Removed", privilege.name(), grant ? "to" : "from", username, userId);

            session.sendAnswer(successMessage(username, privilege, grant));

        } catch (Exception e) {
            e.printStackTrace();
            session.sendAnswer(failureMessage(grant));
        }


    }

    private void updateOnlinePlayer(UserRepository userRepo, Player targetPlayer, Privileges privilege, boolean grant, Session session) throws Exception {
        if (grant) {
            session.server.playerManager.addPriv(targetPlayer, privilege);
            userRepo.updateUserPrivileges(targetPlayer.getId(), targetPlayer.getServerPrivileges());
        } else {
            session.server.playerManager.removePriv(targetPlayer, privilege);
            userRepo.updateUserPrivileges(targetPlayer.getId(), targetPlayer.getServerPrivileges());
        }
    }

    private void updateOfflineUser(UserRepository userRepo, UserEntity user, Privileges privilege, boolean grant) throws Exception {
        int updatedPrivileges = grant
                ? user.getPriv() | privilege.getValue()
                : user.getPriv() & ~privilege.getValue();

        userRepo.updateUserPrivileges(user.getId(), updatedPrivileges);
    }

    private String usageMessage(boolean grant) {
        return grant ? "Usage: !privadd <username> <privilege>" : "Usage: !privrm <username> <privilege>";
    }

    private String invalidPrivilegeMessage(String privName) {
        String validPrivileges = Arrays.stream(Privileges.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));

        return "Invalid privilege '" + privName + "'. Valid privileges: " + validPrivileges;
    }

    private String successMessage(String username, Privileges privilege, boolean grant) {
        return grant
                ? "Successfully added " + privilege.name() + " to " + username
                : "Successfully removed " + privilege.name() + " from " + username;
    }

    private String failureMessage(boolean grant) {
        return grant
                ? "Failed to add privilege due to an internal error."
                : "Failed to remove privilege due to an internal error.";
    }
}