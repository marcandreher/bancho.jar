package com.osuserverlist.bjar.packets.client.handlers.multi;

import java.io.IOException;

import com.osuserverlist.bjar.models.essentials.Match;
import com.osuserverlist.bjar.models.essentials.MatchSlot;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.osu.match.MatchTeams;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.client.BanchoPacketHandler;
import com.osuserverlist.bjar.packets.client.BanchoPacketReader;
import com.osuserverlist.bjar.packets.client.ClientPackets;
import com.osuserverlist.bjar.packets.client.engine.ClientPacket;

@ClientPacket(ClientPackets.MATCH_CHANGE_TEAM)
public class MatchChangeTeamPacket implements BanchoPacketHandler {

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        Match match = player.getMatch();
        if (match == null) {
            logger.warn("Player {} tried to change team but is not in a match", player.getUsername());
            return true;
        }

        MatchSlot slot = match.getSlots()[match.getSlot(player)];
        if(slot == null) {
            logger.warn("Player {} tried to change team but is not in a valid slot", player.getUsername());
            return true;
        }

        MatchTeams curTeam = MatchTeams.values()[slot.getTeam()];
        MatchTeams newTeam;

        if(curTeam == MatchTeams.BLUE) {
            newTeam = MatchTeams.RED;
        } else if(curTeam == MatchTeams.RED) {
            newTeam = MatchTeams.BLUE;
        } else {
            return true;
        }

        slot.setTeam(newTeam.byteValue);

        match.enqueUpdate();

        return true;
    }
    
}
