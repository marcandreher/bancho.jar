package com.osuserverlist.bjar.commands.nomination;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.database.BeatmapEntity;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.osu.Privileges;
import com.osuserverlist.bjar.modules.commands.BanchoCommand;
import com.osuserverlist.bjar.modules.commands.BanchoCommandHandler;
import com.osuserverlist.bjar.modules.commands.BanchoCommandProcessor.PlayerCommandInfo;
import com.osuserverlist.bjar.modules.commands.CommandCategory;
import com.osuserverlist.bjar.modules.database.Database;
import com.osuserverlist.bjar.modules.database.MySQL;
import com.osuserverlist.bjar.modules.logger.LoggerFactory;

@BanchoCommand(
    name = "!requests",
    category = CommandCategory.NOMINATION,
    requiredPrivileges = Privileges.NOMINATOR,
    description = "View and manage pending nomination requests."
)
public class RequestsCommand extends BanchoCommandHandler {

    private final Logger logger = LoggerFactory.getLogger(RequestsCommand.class);

    @Override
    public void handle(Player sender, PlayerCommandInfo[] commandInfos, String[] args) {

        try (MySQL mysql = Database.getConnection()) {

            // Fetch all pending requests
            ResultSet rs = mysql.query(
                "SELECT `map_id` FROM `map_requests` WHERE `active` = 1 ORDER BY `id` ASC"
            ).executeQuery();

            List<Integer> mapIds = new ArrayList<>();

            while (rs.next()) {
                mapIds.add(rs.getInt("map_id"));
            }

            // No arguments -> list requests
            if (args.length == 0) {

                if (mapIds.isEmpty()) {
                    sendBotMessage(commandInfos, "No pending nomination requests.");
                    return;
                }

                for (int i = 0; i < mapIds.size(); i++) {
                    BeatmapEntity beatmap = Server.getInstance()
                        .osuAPIHandler
                        .getBeatmapById(mysql, mapIds.get(i));

                    sendBotMessage(commandInfos,
                        "[" + i + "] " + beatmap.toEmbed());
                }

                sendBotMessage(commandInfos,
                    "Use !requests approve <index> or !requests deny <index>.");
                return;
            }

            if (args.length != 2) {
                sendBotMessage(commandInfos,
                    "Usage: !requests <approve|deny> <index>");
                return;
            }

            String action = args[0].toLowerCase();

            int index;
            try {
                index = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sendBotMessage(commandInfos, "Invalid request index.");
                return;
            }

            if (index < 0 || index >= mapIds.size()) {
                sendBotMessage(commandInfos, "Request index out of range.");
                return;
            }

            int mapId = mapIds.get(index);

            switch (action) {

                case "approve":
                    mysql.query(
                        "UPDATE `map_requests` SET `active` = 0, `admin_id` = ? WHERE `map_id` = ?", sender.getId(), mapId
                    )
                    .executeUpdate();

                    sendBotMessage(commandInfos, "Approved request #" + index + ".");
                    break;

                case "deny":
                    mysql.query(
                        "UPDATE `map_requests` SET `active` = 0, `admin_id` = ? WHERE `map_id` = ?", sender.getId(), mapId
                    )
                    .executeUpdate();

                    sendBotMessage(commandInfos, "Denied request #" + index + ".");
                    break;

                default:
                    sendBotMessage(commandInfos,
                        "Unknown action. Use approve or deny.");
                    break;
            }

        } catch (Exception e) {
            sendBotMessage(commandInfos,
                "An error occurred while processing the requests.");
            logger.error("Error processing requests for player {}", sender.getUsername(), e);
        }
    }
}