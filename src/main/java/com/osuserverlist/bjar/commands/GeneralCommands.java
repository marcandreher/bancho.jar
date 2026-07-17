package com.osuserverlist.bjar.commands;

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
import com.osuserverlist.bjar.modules.main.Commands;
import com.osuserverlist.bjar.modules.main.Commands.BanchoCommand;
import com.osuserverlist.bjar.modules.main.Commands.BanchoCommandHandler;
import com.osuserverlist.bjar.modules.main.Commands.CommandCategory;
import com.osuserverlist.bjar.modules.main.Commands.CommandInfo;
import com.osuserverlist.bjar.modules.main.Commands.Session;
import com.osuserverlist.bjar.modules.osu.OsuMapDownloader;

import me.skiincraft.api.ousu.entity.beatmap.Beatmap;

public class GeneralCommands extends BanchoCommandHandler {

    @BanchoCommand(name = "!with", category = CommandCategory.GENERAL, description = "Shows PPCount of last nped map")
    public void with(Player sender, Session session, String[] args) {
        if (sender.getLastNpBeatmapId() == 0) {
            session.sendAnswer("No beatmap selected. Please select a beatmap with /np first.");
            return;
        }

        int mods = 0;
        for (String modStr : args) {
            try {
                mods |= Mods.fromAbbreviation(modStr).getValue();
            } catch (IllegalArgumentException e) {
                session.sendAnswer("Invalid mod: " + modStr);
                return;
            }
        }

        Beatmap beatmap = session.server.osuAPIHandler.getRawBeatmapById(sender.getLastNpBeatmapId());
        if (beatmap == null) {
            session.sendAnswer("Beatmap not found.");
            return;
        }

        session.sendAnswer(String.format("Selected beatmap: %s",
                BeatmapEntity.toEmbed(beatmap.getBeatmapId(), beatmap.getBeatmapSetId(), beatmap.getArtist(), beatmap.getTitle(), beatmap.getVersion())));

        session.sendAnswer(calculatePpBreakdown(sender, beatmap, mods, session));

    }

    /**
     * Builds a "PP | 100% - x.xx | 95% - x.xx | ..." breakdown from 100% down to
     * 80% accuracy.
     */
    private String calculatePpBreakdown(Player sender, Beatmap beatmap, int mods, Session session) {
        
        byte[] mapData = OsuMapDownloader.downloadMap(beatmap.getBeatmapId());

        StringBuilder breakdown = new StringBuilder("PP | ");
        for (int acc = 100; acc >= 80; acc -= 5) {
            Score score = new Score();
            score.setMode(sender.getGameMode());
            score.setAccuracy(acc / 100.0);
            score.setMax_combo(beatmap.getMaxCombo());
            int hitObjectCount = beatmap.getCircles() + beatmap.getSliders() + beatmap.getSpinners();
            score.setN300(hitObjectCount);
            score.setMods(mods);

            double pp = session.server.performance.calculate(score, mapData);
            breakdown.append(String.format("%d%% - %.2f | ", acc, pp));
        }
        breakdown.setLength(breakdown.length() - 3); // trim trailing " | "

        return breakdown.toString();
    }

    @BanchoCommand(name = "!help", category = CommandCategory.GENERAL, description = "Lists all available commands with their descriptions.", isHidden = true)
    public void help(Player sender, Session session, String[] args) {
        Map<CommandCategory, List<CommandInfo>> commandsByCategory = Commands.getAllCommands()
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
            session.sendAnswer("No commands available.");
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
                help.append("  !mp ").append(commandInfo.name()).append(" - ").append(commandInfo.description())
                        .append('\n');
            }
        }

        session.sendAnswer(help.toString().stripTrailing());
    }

}