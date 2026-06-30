package com.osuserverlist.bjar.commands.nomination;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.osu.Privileges;
import com.osuserverlist.bjar.modules.commands.BanchoCommand;
import com.osuserverlist.bjar.modules.commands.BanchoCommandHandler;
import com.osuserverlist.bjar.modules.commands.BanchoCommandProcessor.PlayerCommandInfo;
import com.osuserverlist.bjar.modules.commands.CommandCategory;
import com.osuserverlist.bjar.modules.database.Database;
import com.osuserverlist.bjar.modules.database.MySQL;

@BanchoCommand(
    name = "!rank",
    category = CommandCategory.NOMINATION,
    requiredPrivileges = Privileges.NOMINATOR,
    description = "Ranks or unranks the currently selected beatmap or beatmap set."
)
public class RankMapCommand extends BanchoCommandHandler {

    @Override
    public void handle(Player sender, PlayerCommandInfo[] commandInfos, String[] args) {
        if (args.length == 0) {
            sendBotMessage(commandInfos, "Usage: !rank <set/map> <rank/unrank/love>");
            return;
        }

        String type = args[0].toLowerCase();
        String rankType = args.length > 1 ? args[1].toLowerCase() : "rank";
        boolean isSet = type.equals("set");

        if (!type.equals("set") && !type.equals("map")) {
            sendBotMessage(commandInfos, "Invalid type. Use 'set' or 'map'.");
            return;
        }

        RankType rankValue = RankType.fromName(rankType);

        if (rankValue == null) {
            sendBotMessage(commandInfos, "Invalid rank type. Use 'rank', 'unrank' or 'love'.");
            return;
        }

        try (MySQL mysql = Database.getConnection()) {
            if (isSet) {
                if (sender.getLastNpBeatmapSetId() == 0) {
                    sendBotMessage(commandInfos, "No beatmap set selected. Please select a beatmap set first.");
                    return;
                }
                mysql.exec("UPDATE `maps` SET `status`=?,`frozen`=? WHERE `set_id` = ?", rankValue.getValue(), 1,
                        sender.getLastNpBeatmapSetId());
            } else {
                if (sender.getLastNpBeatmapId() == 0) {
                    sendBotMessage(commandInfos, "No beatmap selected. Please select a beatmap first.");
                    return;
                }
                mysql.exec("UPDATE `maps` SET `status`=?,`frozen`=? WHERE `id` = ?", rankValue.getValue(), 1,
                        sender.getLastNpBeatmapId());
            }
        }

        sendBotMessage(commandInfos, "Beatmap " + (isSet ? "set" : "map") + " has been " + rankType + "ed.");
    }

    enum RankType {
        RANK(1),
        UNRANK(-2),
        LOVE(4);

        private final int value;

        RankType(int value) {
            this.value = value;
        }

        public static RankType fromName(String name) {
            for (RankType type : values()) {
                if (type.name().equalsIgnoreCase(name)) {
                    return type;
                }
            }
            return null;
        }

        public int getValue() {
            return value;
        }
    }

}
