package com.osuserverlist.bjar.commands.general;

import java.sql.ResultSet;

import org.slf4j.Logger;

import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.database.BeatmapEntity;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.osu.RankedStatus;
import com.osuserverlist.bjar.modules.commands.BanchoCommand;
import com.osuserverlist.bjar.modules.commands.BanchoCommandHandler;
import com.osuserverlist.bjar.modules.commands.BanchoCommandProcessor.PlayerCommandInfo;
import com.osuserverlist.bjar.modules.commands.CommandCategory;
import com.osuserverlist.bjar.modules.database.Database;
import com.osuserverlist.bjar.modules.database.MySQL;
import com.osuserverlist.bjar.modules.logger.LoggerFactory;

@BanchoCommand(
    name = "!request", 
    category = CommandCategory.GENERAL, 
    description = "Requests a beatmap to be ranked."
)
public class RequestCommand extends BanchoCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(RequestCommand.class);
    
    @Override
    public void handle(Player sender, PlayerCommandInfo[] commandInfos, String[] args) {
        
        if(sender.getLastNpBeatmapId() == 0) {
            sendBotMessage(commandInfos, "Please /np a beatmap first to use this command.");
            return;
        }

        try (MySQL mysql = Database.getConnection()) {
            BeatmapEntity beatmap = Server.getInstance().osuAPIHandler.getBeatmapById(mysql, sender.getLastNpBeatmapId());
            RankedStatus rankedStatus = RankedStatus.getById(beatmap.getStatus());

            if(rankedStatus == RankedStatus.Ranked) {
                sendBotMessage(commandInfos, "This beatmap is already ranked.");
                return;
            }

            ResultSet alreadyRequestedResult = mysql.query("SELECT COUNT(*) FROM `map_requests` WHERE `map_id` = ? AND `active` = 1", beatmap.getId()).executeQuery();
            if(alreadyRequestedResult.next() && alreadyRequestedResult.getInt(1) > 0) {
                sendBotMessage(commandInfos, "This beatmap has already been requested.");
                return;
            }


            mysql.exec("INSERT INTO `map_requests`(`map_id`, `player_id`, `active`) VALUES (?,?,1)", beatmap.getId(), sender.getId());
            sendBotMessage(commandInfos, "Your request for the beatmap '" + beatmap.getTitle() + "' has been submitted successfully.");
            logger.info("Player {} requested beatmap {} ({})", sender.getUsername(), beatmap.getId(), beatmap.getTitle());
        }catch (Exception e) {
            sendBotMessage(commandInfos, "An error occurred while fetching the beatmap information.");
            logger.error("Error fetching beatmap information for player {}", sender.getUsername(), e);
        }

    }

}
