package com.osuserverlist.bjar.commands;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.osuserverlist.bjar.commands.MultiplayerCommands.MultiplayerCommandInfo;
import com.osuserverlist.bjar.models.database.BeatmapEntity;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.essentials.Score;
import com.osuserverlist.bjar.models.osu.Mods;
import com.osuserverlist.bjar.modules.calculations.IPerformanceCalculator;
import com.osuserverlist.bjar.modules.calculations.OsuNativePerformanceCalculator;
import com.osuserverlist.bjar.modules.commands.BanchoCommand;
import com.osuserverlist.bjar.modules.commands.BanchoCommandHandler;
import com.osuserverlist.bjar.modules.commands.BanchoCommandProcessor.PlayerCommandInfo;
import com.osuserverlist.bjar.modules.commands.BanchoCommandRegistry;
import com.osuserverlist.bjar.modules.commands.BanchoCommandRegistry.CommandInfo;
import com.osuserverlist.bjar.modules.commands.CommandCategory;
import com.osuserverlist.bjar.modules.database.Database;
import com.osuserverlist.bjar.modules.database.MySQL;
import com.osuserverlist.bjar.modules.osu.OsuMapDownloader;

public class GeneralCommands extends BanchoCommandHandler {

    @BanchoCommand(
            name = "!with",
            category = CommandCategory.GENERAL,
            description = "Shows PPCount of last nped map"
    )
    public void with(Player sender, PlayerCommandInfo[] commandInfos, String[] args) {
        if (sender.getLastNpBeatmapId() == 0) {
            sendBotMessage(commandInfos, "No beatmap selected. Please select a beatmap first.");
            return;
        }

        int mods = 0;
        for (String modStr : args) {
            try {
                mods |= Mods.fromAbbreviation(modStr).getValue();
            } catch (IllegalArgumentException e) {
                sendBotMessage(commandInfos, "Invalid mod: " + modStr);
                return;
            }
        }

        try (MySQL mysql = Database.getConnection()) {
            BeatmapEntity beatmap = server.osuAPIHandler.getBeatmapById(mysql, sender.getLastNpBeatmapId());
            if (beatmap == null) {
                sendBotMessage(commandInfos, "Beatmap not found in database.");
                return;
            }

            sendBotMessage(commandInfos, String.format("Selected beatmap: %s - %s [%s]",
                    beatmap.getArtist(), beatmap.getTitle(), beatmap.getVersion()));

            sendBotMessage(commandInfos, calculatePpBreakdown(sender, beatmap, mods));
        } catch (SQLException | IOException e) {
            logger.error("Failed to fetch Data", e);
            sendBotMessage(commandInfos, "An error occurred while fetching data.");
        }
    }

    /** Builds a "PP | 100% - x.xx | 95% - x.xx | ..." breakdown from 100% down to 80% accuracy. */
    private String calculatePpBreakdown(Player sender, BeatmapEntity beatmap, int mods) throws IOException {
        IPerformanceCalculator calculator = new OsuNativePerformanceCalculator();
        byte[] mapData = OsuMapDownloader.downloadMap(beatmap.getId());

        StringBuilder breakdown = new StringBuilder("PP | ");
        for (int acc = 100; acc >= 80; acc -= 5) {
            Score score = new Score();
            score.setMode(sender.getGameMode());
            score.setAccuracy(acc / 100.0);
            score.setMax_combo(beatmap.getMaxCombo());
            score.setMods(mods);

            double pp = calculator.calculate(score, mapData);
            breakdown.append(String.format("%d%% - %.2f | ", acc, pp));
        }
        breakdown.setLength(breakdown.length() - 3); // trim trailing " | "

        return breakdown.toString();
    }

    @BanchoCommand(
            name = "!help",
            category = CommandCategory.GENERAL,
            description = "Lists all available commands with their descriptions.",
            isHidden = true
    )
    public void help(Player sender, PlayerCommandInfo[] commandInfos, String[] args) {
        Map<CommandCategory, List<CommandInfo>> commandsByCategory = BanchoCommandRegistry.getAllCommands()
                .stream()
                .filter(commandInfo -> sender.getServerPrivileges() >= commandInfo.requiredPrivileges)
                .filter(commandInfo -> !commandInfo.isHidden)
                .sorted(Comparator.comparing((CommandInfo c) -> c.name))
                .collect(Collectors.groupingBy(
                        commandInfo -> commandInfo.category,
                        TreeMap::new, // sorts categories by their natural (enum/name) order
                        Collectors.toList()));

        List<MultiplayerCommandInfo> multiplayerCommands = sender.getMatch() != null
                ? MultiplayerCommands.getCommands().stream()
                        .filter(cmd -> sender.getServerPrivileges() >= cmd.requiredPrivileges().value)
                        .toList()
                : List.of();

        if (commandsByCategory.isEmpty() && multiplayerCommands.isEmpty()) {
            sendBotMessage(commandInfos, "No commands available.");
            return;
        }

        StringBuilder help = new StringBuilder();
        commandsByCategory.forEach((category, commands) -> {
            help.append("== ").append(category).append(" ==\n");
            for (CommandInfo commandInfo : commands) {
                help.append("  ").append(commandInfo.name).append(" - ").append(commandInfo.description).append('\n');
            }
        });

        if (!multiplayerCommands.isEmpty()) {
            help.append("== Multiplayer (!mp) ==\n");
            for (MultiplayerCommandInfo commandInfo : multiplayerCommands) {
                help.append("  !mp ").append(commandInfo.name()).append(" - ").append(commandInfo.description()).append('\n');
            }
        }

        sendBotMessage(commandInfos, help.toString().stripTrailing());
    }

}