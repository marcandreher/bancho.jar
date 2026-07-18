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
import com.osuserverlist.bjar.modules.util.Validation;
import com.osuserverlist.bjar.repos.UserRepository;

public class AdministationCommands extends BanchoCommandHandler {

    private static final String PRIV_USAGE = "Usage: !priv <add|remove> <username> <privilege>";
    private static final String SUPPORTER_ADD_USAGE = "Usage: !supporter add <username> <duration> (duration e.g. 30d, 1w, 3M)";
    private static final String SUPPORTER_REMOVE_USAGE = "Usage: !supporter remove <username>";

    @BanchoCommand(
            name = "!priv",
            category = CommandCategory.ADMINISTRATION,
            description = "Add or remove privileges from a player",
            requiredPrivileges = Privileges.ADMINISTRATOR
    )
    public void priv(Player sender, Session session, String[] args) {
        if (args.length == 0) {
            session.sendAnswer(PRIV_USAGE);
            return;
        }

        String subCommand = args[0].toLowerCase();
        String[] remainingArgs = Arrays.copyOfRange(args, 1, args.length);

        switch (subCommand) {
            case "add":
                handlePrivilegeChange(session, remainingArgs, true);
                break;
            case "remove":
                handlePrivilegeChange(session, remainingArgs, false);
                break;
            default:
                session.sendAnswer(PRIV_USAGE);
        }
    }

    @BanchoCommand(
            name = "!supporter",
            category = CommandCategory.ADMINISTRATION,
            description = "Add or remove supporter from a player",
            requiredPrivileges = Privileges.ADMINISTRATOR
    )
    public void supporter(Player sender, Session session, String[] args) {
        if (args.length == 0) {
            session.sendAnswer(SUPPORTER_ADD_USAGE);
            return;
        }

        String subCommand = args[0].toLowerCase();
        String[] remainingArgs = Arrays.copyOfRange(args, 1, args.length);

        switch (subCommand) {
            case "add":
                handleSupporterAdd(session, remainingArgs);
                break;
            case "remove":
                handleSupporterRemove(session, remainingArgs);
                break;
            default:
                session.sendAnswer(SUPPORTER_ADD_USAGE);
        }
    }

    private void handlePrivilegeChange(Session session, String[] args, boolean grant) {
        if (args.length < 2) {
            session.sendAnswer(PRIV_USAGE);
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
            applyPrivilegeChange(session, userRepo, username, privilege, grant);
        } catch (Exception e) {
            e.printStackTrace();
            session.sendAnswer(grant
                    ? "Failed to add privilege due to an internal error."
                    : "Failed to remove privilege due to an internal error.");
        }
    }

    private void applyPrivilegeChange(Session session, UserRepository userRepo, String username, Privileges privilege, boolean grant) throws Exception {
        Player targetPlayer = session.server.playerManager.getByUsername(username);
        int userId;

        if (targetPlayer != null) {
            if (grant) {
                session.server.playerManager.addPriv(targetPlayer, privilege);
            } else {
                session.server.playerManager.removePriv(targetPlayer, privilege);
            }
            userRepo.updateUserPrivileges(targetPlayer.getId(), targetPlayer.getServerPrivileges());
            userId = targetPlayer.getId();
        } else {
            UserEntity user = userRepo.getUserByName(username);

            if (user == null) {
                session.sendAnswer("Player not found: " + username);
                return;
            }

            int updatedPrivileges = grant
                    ? user.getPriv() | privilege.getValue()
                    : user.getPriv() & ~privilege.getValue();

            userRepo.updateUserPrivileges(user.getId(), updatedPrivileges);
            userId = user.getId();
        }

        logger.info("Privilege change: {} {} privilege {} for user <{}>({})",
                grant ? "Added" : "Removed", privilege.name(), grant ? "to" : "from", username, userId);

        session.sendAnswer(grant
                ? "Successfully added " + privilege.name() + " to " + username
                : "Successfully removed " + privilege.name() + " from " + username);
    }

    private void handleSupporterAdd(Session session, String[] args) {
        if (args.length < 2) {
            session.sendAnswer(SUPPORTER_ADD_USAGE);
            return;
        }

        String username = args[0];
        int durationSeconds = (int) Validation.parseDuration(args[1]);

        if (durationSeconds <= 0) {
            session.sendAnswer("Invalid duration: " + args[1] + " (expected format like 30d, 1w, 3M)");
            return;
        }

        int supporterExpiry = (int) (System.currentTimeMillis() / 1000L) + durationSeconds;

        try (MySQL mysql = Database.getConnection()) {
            UserRepository userRepo = new UserRepository(mysql);
            applySupporterChange(session, userRepo, username, true, durationSeconds, supporterExpiry);
        } catch (Exception e) {
            e.printStackTrace();
            session.sendAnswer("Failed to grant supporter to " + username + " due to an internal error.");
        }
    }

    private void handleSupporterRemove(Session session, String[] args) {
        if (args.length < 1) {
            session.sendAnswer(SUPPORTER_REMOVE_USAGE);
            return;
        }

        String username = args[0];

        try (MySQL mysql = Database.getConnection()) {
            UserRepository userRepo = new UserRepository(mysql);
            applySupporterChange(session, userRepo, username, false, 0, 0);
        } catch (Exception e) {
            e.printStackTrace();
            session.sendAnswer("Failed to remove supporter from " + username + " due to an internal error.");
        }
    }

    private void applySupporterChange(Session session, UserRepository userRepo, String username, boolean grant, int durationSeconds, int supporterExpiry) throws Exception {
        Player targetPlayer = session.server.playerManager.getByUsername(username);
        int userId;

        if (targetPlayer != null) {
            if (grant) {
                session.server.playerManager.addPriv(targetPlayer, Privileges.SUPPORTER);
            } else {
                session.server.playerManager.removePriv(targetPlayer, Privileges.SUPPORTER);
            }
            userRepo.updateUserPrivileges(targetPlayer.getId(), targetPlayer.getServerPrivileges());
            userRepo.updateUserDonor(targetPlayer.getId(), supporterExpiry);
            userId = targetPlayer.getId();
        } else {
            UserEntity user = userRepo.getUserByName(username);

            if (user == null) {
                session.sendAnswer("Player not found: " + username);
                return;
            }

            int updatedPrivileges = grant
                    ? user.getPriv() | Privileges.SUPPORTER.getValue()
                    : user.getPriv() & ~Privileges.SUPPORTER.getValue();

            userRepo.updateUserPrivileges(user.getId(), updatedPrivileges);
            userRepo.updateUserDonor(user.getId(), supporterExpiry);
            userId = user.getId();
        }

        if (grant) {
            logger.info("Supporter granted to user <{}>({}) for {}", username, userId, Validation.formatDuration(durationSeconds));
            session.sendAnswer("Successfully granted " + username + " supporter for " + Validation.formatDuration(durationSeconds) + ".");
        } else {
            logger.info("Supporter removed from user <{}>({})", username, userId);
            session.sendAnswer("Successfully removed supporter from " + username + ".");
        }
    }

    private String invalidPrivilegeMessage(String privName) {
        String validPrivileges = Arrays.stream(Privileges.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));

        return "Invalid privilege '" + privName + "'. Valid privileges: " + validPrivileges;
    }
}