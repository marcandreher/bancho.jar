package com.osuserverlist.bjar.commands;

import java.util.Arrays;

import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.database.UserEntity;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.osu.Privileges;
import com.osuserverlist.bjar.modules.Commands.BanchoCommand;
import com.osuserverlist.bjar.modules.Commands.BanchoCommandHandler;
import com.osuserverlist.bjar.modules.Commands.CommandCategory;
import com.osuserverlist.bjar.modules.Commands.Session;
import com.osuserverlist.bjar.modules.database.Database;
import com.osuserverlist.bjar.modules.database.MySQL;
import com.osuserverlist.bjar.repos.UserRepository;

public class ModerationCommands extends BanchoCommandHandler {
     @BanchoCommand(
            name = "!restrict",
            category = CommandCategory.MODERATION,
            description = "Restrict a player",
            requiredPrivileges = Privileges.MODERATOR
    )
    public void restrict(Player sender, Session session, String[] args) {
        handleRestriction(sender, session, args, true);
    }

    @BanchoCommand(
            name = "!unrestrict",
            category = CommandCategory.MODERATION,
            description = "Unrestrict a player",
            requiredPrivileges = Privileges.MODERATOR
    )
    public void unrestrict(Player sender, Session session, String[] args) {
        handleRestriction(sender, session, args, false);
    }

    @BanchoCommand(
        name = "!kick", 
        category = CommandCategory.MODERATION, 
        description = "Kick a player from the server", 
        requiredPrivileges = Privileges.MODERATOR
    )
    public void kick(Player sender, Session session, String[] args) {
        if(args.length == 0) {
            session.sendAnswer("Usage: !kick <username>");
            return;
        }

        String username = args[0];

        Player targetPlayer = session.server.playerManager.getByFilter(p -> p.getUsername().equalsIgnoreCase(username));

        if (targetPlayer == null) {
            session.sendAnswer("Player not found: " + username);
            return;
        }

        logger.info("Player {} has been kicked by {}", targetPlayer.toString(), sender.toString());

        session.server.playerManager.disconnect(targetPlayer);
        session.sendAnswer("Player " + username + " has been kicked from the server.");
    }

    private void handleRestriction(
            Player sender,
            Session session,
            String[] args,
            boolean restrict
    ) {
        if (args.length < 2) {
            session.sendAnswer(usageMessage(restrict));
            return;
        }

        String username = args[0];
        String reason = getReason(args);

        if (!isValidReason(session, reason)) {
            return;
        }

        try (MySQL mysql = Database.getConnection()) {
            UserRepository userRepo = new UserRepository(mysql);

            Player targetPlayer = session.server.playerManager.getByFilter(
                    player -> player.getUsername().equalsIgnoreCase(username)
            );

            if (targetPlayer != null) {
                updateOnlinePlayer(session.server, userRepo, targetPlayer, restrict);
                logRestrictionAction(targetPlayer.toString(), sender.toString(), reason, restrict);
            } else {
                UserEntity user = userRepo.getUserByName(username);

                if (user == null) {
                    session.sendAnswer("Player not found: " + username);
                    return;
                }

                updateOfflineUser(userRepo, user, restrict);
                logRestrictionAction(user.toString(), sender.toString(), reason, restrict);
            }

            session.sendAnswer(successMessage(username, reason, restrict));

        } catch (Exception e) {
            e.printStackTrace();
            session.sendAnswer(failureMessage(username, restrict));
        }
    }

    private void updateOnlinePlayer(Server server, UserRepository userRepo, Player targetPlayer, boolean restrict) throws Exception {
        int updatedPrivileges = restrict
                ? targetPlayer.getServerPrivileges() & ~Privileges.UNRESTRICTED.getValue()
                : targetPlayer.getServerPrivileges() | Privileges.UNRESTRICTED.getValue();

        userRepo.updateUserPrivileges(targetPlayer.getId(), updatedPrivileges);

        if (restrict) {
            server.playerManager.restrict(targetPlayer);
        } else {
            server.playerManager.unrestrict(targetPlayer);
        }
    }

    private void updateOfflineUser(UserRepository userRepo, UserEntity user, boolean restrict) throws Exception {
        int updatedPrivileges = restrict
                ? user.getPriv() & ~Privileges.UNRESTRICTED.getValue()
                : user.getPriv() | Privileges.UNRESTRICTED.getValue();

        userRepo.updateUserPrivileges(user.getId(), updatedPrivileges);
    }

    private void logRestrictionAction(String target, String sender, String reason, boolean restrict) {
        String action = restrict ? "restricted" : "unrestricted";
        logger.info("Player {} has been {} by {} for reason: {}", target, action, sender, reason);
    }

    private String usageMessage(boolean restrict) {
        return restrict ? "Usage: !restrict <username> <reason>" : "Usage: !unrestrict <username> <reason>";
    }

    private String successMessage(String username, String reason, boolean restrict) {
        return restrict
                ? "Successfully restricted " + username + " for: " + reason
                : "Successfully unrestricted " + username + " for: " + reason;
    }

    private String failureMessage(String username, boolean restrict) {
        return restrict
                ? "Failed to restrict " + username + " due to an internal error."
                : "Failed to unrestrict " + username + " due to an internal error.";
    }

    private String getReason(String[] args) {
        return String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
    }

    private boolean isValidReason(Session session, String reason) {
        if (reason.isEmpty()) {
            session.sendAnswer("Reason cannot be empty.");
            return false;
        }

        if (reason.length() > 100) {
            session.sendAnswer("Reason cannot exceed 100 characters.");
            return false;
        }

        return true;
    }
}
