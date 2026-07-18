package com.osuserverlist.bjar.commands;

import java.util.Arrays;

import com.osuserverlist.bjar.Server;
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

public class ModerationCommands extends BanchoCommandHandler {

    private static final String RESTRICTION_USAGE = "Usage: !restriction <add|remove> <username> <reason>";
    private static final String SILENCE_ADD_USAGE = "Usage: !silence add <username> <duration> <reason> (duration e.g. 30s, 10m, 2h, 1d, 1w)";
    private static final String SILENCE_REMOVE_USAGE = "Usage: !silence remove <username> <reason>";

    @BanchoCommand(
            name = "!restriction",
            category = CommandCategory.MODERATION,
            description = "Restrict or unrestrict a player",
            requiredPrivileges = Privileges.MODERATOR
    )
    public void restriction(Player sender, Session session, String[] args) {
        if (args.length == 0) {
            session.sendAnswer(RESTRICTION_USAGE);
            return;
        }

        String subCommand = args[0].toLowerCase();
        String[] remainingArgs = Arrays.copyOfRange(args, 1, args.length);

        switch (subCommand) {
            case "add":
                handleRestriction(sender, session, remainingArgs, true);
                break;
            case "remove":
                handleRestriction(sender, session, remainingArgs, false);
                break;
            default:
                session.sendAnswer(RESTRICTION_USAGE);
        }
    }

    @BanchoCommand(
            name = "!silence",
            category = CommandCategory.MODERATION,
            description = "Silence or unsilence a player",
            requiredPrivileges = Privileges.MODERATOR
    )
    public void silence(Player sender, Session session, String[] args) {
        if (args.length == 0) {
            session.sendAnswer(SILENCE_ADD_USAGE);
            return;
        }

        String subCommand = args[0].toLowerCase();
        String[] remainingArgs = Arrays.copyOfRange(args, 1, args.length);

        switch (subCommand) {
            case "add":
                handleSilence(sender, session, remainingArgs, true);
                break;
            case "remove":
                handleSilence(sender, session, remainingArgs, false);
                break;
            default:
                session.sendAnswer(subCommand.equals("add") ? SILENCE_ADD_USAGE : SILENCE_REMOVE_USAGE);
        }
    }

    @BanchoCommand(
            name = "!kick",
            category = CommandCategory.MODERATION,
            description = "Kick a player from the server",
            requiredPrivileges = Privileges.MODERATOR
    )
    public void kick(Player sender, Session session, String[] args) {
        if (args.length == 0) {
            session.sendAnswer("Usage: !kick <username>");
            return;
        }

        String username = args[0];
        Player targetPlayer = session.server.playerManager.getByUsername(username);

        if (targetPlayer == null) {
            session.sendAnswer("Player not found: " + username);
            return;
        }

        logger.info("Player {} has been kicked by {}", targetPlayer, sender);

        session.server.playerManager.disconnect(targetPlayer);
        session.sendAnswer("Player " + username + " has been kicked from the server.");
    }

    private void handleRestriction(Player sender, Session session, String[] args, boolean restrict) {
        if (args.length < 2) {
            session.sendAnswer(RESTRICTION_USAGE);
            return;
        }

        String username = args[0];
        String reason = Validation.joinReason(args, 1);

        if (!Validation.isValidReason(session, reason)) {
            return;
        }

        try (MySQL mysql = Database.getConnection()) {
            UserRepository userRepo = new UserRepository(mysql);
            applyRestriction(session, userRepo, sender, username, reason, restrict);
        } catch (Exception e) {
            e.printStackTrace();
            session.sendAnswer(restrict
                    ? "Failed to restrict " + username + " due to an internal error."
                    : "Failed to unrestrict " + username + " due to an internal error.");
        }
    }

    private void applyRestriction(Session session, UserRepository userRepo, Player sender, String username, String reason, boolean restrict) throws Exception {
        Player targetPlayer = session.server.playerManager.getByUsername(username);

        if (targetPlayer != null) {
            setOnlinePlayerRestricted(session.server, userRepo, targetPlayer, restrict);
            logger.info("Player {} has been {} by {} for reason: {}", targetPlayer, restrict ? "restricted" : "unrestricted", sender, reason);
        } else {
            UserEntity user = userRepo.getUserByName(username);

            if (user == null) {
                session.sendAnswer("Player not found: " + username);
                return;
            }

            setOfflineUserRestricted(userRepo, user, restrict);
            logger.info("Player {} has been {} by {} for reason: {}", user, restrict ? "restricted" : "unrestricted", sender, reason);
        }

        session.sendAnswer(restrict
                ? "Successfully restricted " + username + " for: " + reason
                : "Successfully unrestricted " + username + " for: " + reason);
    }

    private void setOnlinePlayerRestricted(Server server, UserRepository userRepo, Player targetPlayer, boolean restrict) throws Exception {
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

    private void setOfflineUserRestricted(UserRepository userRepo, UserEntity user, boolean restrict) throws Exception {
        int updatedPrivileges = restrict
                ? user.getPriv() & ~Privileges.UNRESTRICTED.getValue()
                : user.getPriv() | Privileges.UNRESTRICTED.getValue();

        userRepo.updateUserPrivileges(user.getId(), updatedPrivileges);
    }

    private void handleSilence(Player sender, Session session, String[] args, boolean silence) {
        String usage = silence ? SILENCE_ADD_USAGE : SILENCE_REMOVE_USAGE;
        int minArgs = silence ? 3 : 2;

        if (args.length < minArgs) {
            session.sendAnswer(usage);
            return;
        }

        String username = args[0];
        int durationSeconds = 0;
        String reason;

        if (silence) {
            try {
                durationSeconds = Validation.parseDuration(args[1]);
            } catch (IllegalArgumentException e) {
                session.sendAnswer("Invalid duration: " + args[1] + " (expected format like 30s, 10m, 2h, 1d)");
                return;
            }
            reason = Validation.joinReason(args, 2);
        } else {
            reason = Validation.joinReason(args, 1);
        }

        if (!Validation.isValidReason(session, reason)) {
            return;
        }

        int silenceEnd = silence ? (int) ((System.currentTimeMillis() / 1000L) + durationSeconds) : 0;

        try (MySQL mysql = Database.getConnection()) {
            UserRepository userRepo = new UserRepository(mysql);
            applySilence(session, userRepo, sender, username, reason, silence, durationSeconds, silenceEnd);
        } catch (Exception e) {
            e.printStackTrace();
            session.sendAnswer(silence
                    ? "Failed to silence " + username + " due to an internal error."
                    : "Failed to unsilence " + username + " due to an internal error.");
        }
    }

    private void applySilence(Session session, UserRepository userRepo, Player sender, String username, String reason, boolean silence, int durationSeconds, int silenceEnd) throws Exception {
        Player targetPlayer = session.server.playerManager.getByUsername(username);

        if (targetPlayer != null) {
            setOnlinePlayerSilenced(session.server, userRepo, targetPlayer, silence, silenceEnd);
            logSilenceAction(targetPlayer.toString(), sender.toString(), reason, silence, durationSeconds);
        } else {
            UserEntity user = userRepo.getUserByName(username);

            if (user == null) {
                session.sendAnswer("Player not found: " + username);
                return;
            }

            setOfflineUserSilenced(userRepo, user, silenceEnd);
            logSilenceAction(user.toString(), sender.toString(), reason, silence, durationSeconds);
        }

        session.sendAnswer(silence
                ? "Successfully silenced " + username + " for " + Validation.formatDuration(durationSeconds) + ": " + reason
                : "Successfully unsilenced " + username + " for: " + reason);
    }

    private void setOnlinePlayerSilenced(Server server, UserRepository userRepo, Player targetPlayer, boolean silence, int silenceEnd) throws Exception {
        userRepo.updateUserSilence(targetPlayer.getId(), silenceEnd);

        if (silence) {
            server.playerManager.silence(targetPlayer, silenceEnd);
        } else {
            server.playerManager.unsilence(targetPlayer);
        }
    }

    private void setOfflineUserSilenced(UserRepository userRepo, UserEntity user, int silenceEnd) throws Exception {
        userRepo.updateUserSilence(user.getId(), silenceEnd);
    }

    private void logSilenceAction(String target, String sender, String reason, boolean silence, int durationSeconds) {
        if (silence) {
            logger.info("Player {} has been silenced by {} for {}s. Reason: {}", target, sender, durationSeconds, reason);
        } else {
            logger.info("Player {} has been unsilenced by {} for reason: {}", target, sender, reason);
        }
    }
}