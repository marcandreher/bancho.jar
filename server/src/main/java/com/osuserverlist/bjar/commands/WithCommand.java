package com.osuserverlist.bjar.commands;

import java.io.IOException;
import java.sql.SQLException;

import org.slf4j.Logger;

import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.database.BeatmapEntity;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.essentials.Score;
import com.osuserverlist.bjar.models.osu.Mods;
import com.osuserverlist.bjar.modules.calculations.IPerformanceCalculator;
import com.osuserverlist.bjar.modules.calculations.OsuNativePerformanceCalculator;
import com.osuserverlist.bjar.modules.commands.BanchoCommand;
import com.osuserverlist.bjar.modules.commands.BanchoCommandHandler;
import com.osuserverlist.bjar.modules.commands.BanchoCommandProcessor.PlayerCommandInfo;
import com.osuserverlist.bjar.modules.database.Database;
import com.osuserverlist.bjar.modules.database.MySQL;
import com.osuserverlist.bjar.modules.logger.LoggerFactory;
import com.osuserverlist.bjar.modules.osu.OsuMapDownloader;

@BanchoCommand(name = "!with", description = "Shows PPCount of last nped map")
public class WithCommand extends BanchoCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(WithCommand.class);

    @Override
    public void handle(Player sender, PlayerCommandInfo[] commandInfos, String[] args) {
        if(sender.getLastNpBeatmapId() == 0) {
            sendBotMessage(commandInfos, "No beatmap selected. Please select a beatmap first.");
            return;
        }

        Server server = Server.getInstance();

        int mods = 0;
        if(args.length > 0) {
            for(String modStr : args) {
                try {
                    Mods mod = Mods.fromAbbreviation(modStr);
                    mods |= mod.getValue();
                }catch(IllegalArgumentException e) {
                    sendBotMessage(commandInfos, "Invalid mod: " + modStr);
                    return;
                }
            }
        }
        
        try (MySQL mysql = Database.getConnection()) {
            BeatmapEntity beatmap = server.osuAPIHandler.getBeatmapById(mysql, sender.getLastNpBeatmapId());
            
            if(beatmap == null) {
                sendBotMessage(commandInfos, "Beatmap not found in database.");
                return;
            }

            sendBotMessage(commandInfos, String.format("Selected beatmap: %s - %s [%s]", beatmap.getArtist(), beatmap.getTitle(), beatmap.getVersion()));

            IPerformanceCalculator calculator = new OsuNativePerformanceCalculator();
            byte[] mapData = OsuMapDownloader.downloadMap(beatmap.getId());

            StringBuilder responseBuilder = new StringBuilder();
            responseBuilder.append("PP | ");
            for (int acc = 100; acc >= 80; acc -= 5) {
                Score score = new Score();
                score.setMode(sender.getGameMode());
                score.setAccuracy(acc / 100.0);
                score.setMax_combo(beatmap.getMaxCombo());
                score.setMods(mods);
                double pp = calculator.calculate(score, mapData);
                responseBuilder.append(String.format("%d%% - %.2f | ", acc, pp));
            }
            responseBuilder.substring(0, responseBuilder.length() - 3); // Remove the last " | "
            sendBotMessage(commandInfos, responseBuilder.toString());
        }catch(SQLException | IOException e) {
            logger.error("Failed to fetch Data", e);
            sendBotMessage(commandInfos, "An error occurred while fetching data.");
        }
    }
}
