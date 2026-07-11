package com.osuserverlist.bjar.commands.general;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.essentials.Match;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.osu.Privileges;
import com.osuserverlist.bjar.modules.commands.BanchoCommand;
import com.osuserverlist.bjar.modules.commands.BanchoCommandHandler;
import com.osuserverlist.bjar.modules.commands.BanchoCommandProcessor.PlayerCommandInfo;
import com.osuserverlist.bjar.modules.commands.CommandCategory;
import com.osuserverlist.bjar.packets.server.handlers.multi.MatchTransferHostPacket;

@BanchoCommand(
        name = "!mp",
        category = CommandCategory.GENERAL,
        description = "Multiplayer commands.",
        isHidden = true
)
public class MultiplayerCommands extends BanchoCommandHandler {

    @MultiplayerCommand(
            name = "start",
            description = "Starts the match if you are the host."
    )
    public void start(Player sender, PlayerCommandInfo[] commandInfos, String[] args, Match match) {
        // TODO: Handle multiplayer commands
    }

    @MultiplayerCommand(
            name = "randpw",
            description = "Changes the match password."
    )
    public void randpw(Player sender, PlayerCommandInfo[] commandInfos, String[] args, Match match) {
        String randPw = UUID.randomUUID().toString();
        match.setRoomPassword(randPw);
        match.enqueUpdate();
        sendBotMessage(commandInfos, "Match password changed to: " + randPw);
    }

    @MultiplayerCommand(
            name = "host",
            description = "Transfers host to another player."
    )
    public void host(Player sender, PlayerCommandInfo[] commandInfos, String[] args, Match match) {
        if (args.length == 0) {
            sendBotMessage(commandInfos, "Usage: !mp host <player>");
            return;
        }

        String targetPlayerName = args[0];
        Player targetPlayer = match.getPlayers().stream()
                .filter(p -> p.getUsername().equalsIgnoreCase(targetPlayerName))
                .findFirst()
                .orElse(null);

        if (targetPlayer == null) {
            sendBotMessage(commandInfos, "Player not found in the match.");
            return;
        }

        match.setHostId(targetPlayer.getId());
        targetPlayer.sendPacket(new MatchTransferHostPacket());
        match.enqueUpdate();
        sendBotMessage(commandInfos, "Host transferred to: " + targetPlayerName);
    }

    @MultiplayerCommand(
            name = "forcejoin",
            description = "Forces a player to join the match.",
            requiredPrivileges = Privileges.ADMINISTRATOR
    )
    public void forcejoin(Player sender, PlayerCommandInfo[] commandInfos, String[] args, Match match) {
        if (args.length == 0) {
            sendBotMessage(commandInfos, "Usage: !mp forcejoin <player>");
            return;
        }

        String targetPlayerName = args[0];
        Server server = Server.getInstance();
        Player targetPlayer = server.playerManager.getByFilter(p -> p.getUsername().equalsIgnoreCase(targetPlayerName));

        if (targetPlayer == null) {
            sendBotMessage(commandInfos, "Player not found.");
            return;
        }

        server.matchManager.joinMatch(match, targetPlayer);
        sendBotMessage(commandInfos, "Player " + targetPlayerName + " has been forced to join the match.");
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    private @interface MultiplayerCommand {
        String name();
        String description();
        Privileges requiredPrivileges() default Privileges.NONE;
    }

    private record MultiplayerCommandEntry(Method method, String description, Privileges requiredPrivileges) {}

    private static final Map<String, MultiplayerCommandEntry> COMMANDS = Arrays.stream(MultiplayerCommands.class.getDeclaredMethods())
            .filter(method -> method.isAnnotationPresent(MultiplayerCommand.class))
            .collect(Collectors.toMap(
                    method -> method.getAnnotation(MultiplayerCommand.class).name().toLowerCase(),
                    method -> new MultiplayerCommandEntry(method, method.getAnnotation(MultiplayerCommand.class).description(), method.getAnnotation(MultiplayerCommand.class).requiredPrivileges())));

    @Override
    public void handle(Player sender, PlayerCommandInfo[] commandInfos, String[] args) {
        if (args.length == 0) {
            sendBotMessage(commandInfos, "No command specified.");
            return;
        }

        Match match = sender.getMatch();
        if (match == null) {
            sendBotMessage(commandInfos, "You are not in a match.");
            return;
        }

        MultiplayerCommandEntry commandEntry = COMMANDS.get(args[0].toLowerCase());

        if (commandEntry == null) {
            sendBotMessage(commandInfos, "Unknown command: !mp " + args[0]);
            return;
        }

        if(sender.getServerPrivileges() < commandEntry.requiredPrivileges().value) {
            sendBotMessage(commandInfos, "You do not have permission to use this command.");
            return;
        }

        String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
        try {
            commandEntry.method().invoke(this, sender, commandInfos, newArgs, match);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to execute multiplayer command: " + args[0], e);
        }
    }

}