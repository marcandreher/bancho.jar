package com.osuserverlist.bjar.packets.client.handlers.multi;

import java.io.IOException;

import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.database.BeatmapEntity;
import com.osuserverlist.bjar.models.essentials.Match;
import com.osuserverlist.bjar.models.essentials.MatchSlot;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.osu.match.MatchTeams;
import com.osuserverlist.bjar.models.osu.match.SlotStatus;
import com.osuserverlist.bjar.modules.database.Database;
import com.osuserverlist.bjar.modules.database.MySQL;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.client.BanchoPacketHandler;
import com.osuserverlist.bjar.packets.client.BanchoPacketReader;
import com.osuserverlist.bjar.packets.client.ClientPackets;
import com.osuserverlist.bjar.packets.client.engine.ClientPacket;
import com.osuserverlist.bjar.packets.server.handlers.chat.SendMessagePacket;

@ClientPacket(ClientPackets.MATCH_CHANGE_SETTINGS)
public class MatchChangeSettingsPacket implements BanchoPacketHandler {

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        Server server = Server.getInstance();

        Match match = reader.readMatch();
        Match playerMatch = server.matchManager.getByHostId(player.getId());

        if (playerMatch == null) {
            return true;
        }

        /*
         * Map changed
         */
        if (playerMatch.getBeatmapId() != match.getBeatmapId()) {

            BeatmapEntity beatmap = null;

            if (match.getBeatmapId() > 0) {
                try (MySQL mysql = Database.getConnection()) {
                    beatmap = server.osuAPIHandler.getBeatmapById(mysql, match.getBeatmapId());
                } catch (Exception e) {
                    logger.error("Error while fetching beatmap {}", match.getBeatmapId(), e);
                }
            }

            for (MatchSlot slot : playerMatch.getSlots()) {
                if (slot.getStatus() == SlotStatus.READY.byteValue) {
                    slot.setStatus(SlotStatus.NOT_READY.byteValue);
                }

                Player slotPlayer = server.playerManager.getById(slot.getPlayerId());

                if (slotPlayer != null && beatmap != null) {
                    slotPlayer.sendPacket(new SendMessagePacket(
                            server.botPlayer.getUsername(),
                            "Selected: " + beatmap.toEmbed(),
                            "#multiplayer",
                            server.botPlayer.getId()));
                }
            }

            playerMatch.setBeatmapId(match.getBeatmapId());
            playerMatch.setBeatmapChecksum(match.getBeatmapChecksum());
            playerMatch.setBeatmapName(match.getBeatmapName());
        }

        /*
         * Team type changed.
         *
         */
        if (playerMatch.getTeamType() != match.getTeamType()) {

            byte defaultTeam;

            switch (match.getTeamType()) {
                case HEAT_TO_HEAD:
                case TAG_COOP:
                    defaultTeam = MatchTeams.NEUTRAL.byteValue;
                    break;

                case TEAM_VS:
                case TAG_TEAM_VS:
                default:
                    defaultTeam = MatchTeams.RED.byteValue;
                    break;
            }

            for (MatchSlot slot : playerMatch.getSlots()) {
                if (slot.getPlayerId() != 0) {
                    slot.setTeam(defaultTeam);
                }
            }

            playerMatch.setTeamType(match.getTeamType());
        }

        /*
         * Remaining settings
         */
        playerMatch.setMode(match.getMode());
        playerMatch.setMods(match.getMods());
        playerMatch.setMatchType(match.getMatchType());
        playerMatch.setRoomPassword(match.getRoomPassword());
        playerMatch.setRoomName(match.getRoomName());
        playerMatch.setSpecialMode(match.getSpecialMode());
        playerMatch.setScoringType(match.getScoringType());

        playerMatch.enqueUpdate();

        return true;
    }
}