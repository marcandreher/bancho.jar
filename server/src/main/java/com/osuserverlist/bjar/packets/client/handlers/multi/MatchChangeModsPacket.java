package com.osuserverlist.bjar.packets.client.handlers.multi;

import java.io.IOException;

import com.osuserverlist.bjar.models.essentials.Match;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.osu.Mods;
import com.osuserverlist.bjar.models.osu.match.MatchSpecialMode;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.client.BanchoPacketHandler;
import com.osuserverlist.bjar.packets.client.BanchoPacketReader;
import com.osuserverlist.bjar.packets.client.ClientPackets;
import com.osuserverlist.bjar.packets.client.engine.ClientPacket;

@ClientPacket(ClientPackets.MATCH_CHANGE_MODS)
public class MatchChangeModsPacket implements BanchoPacketHandler {

    private static final int SPEED_CHANGING_MODS = Mods.DoubleTime.getValue()
            | Mods.Nightcore.getValue()
            | Mods.HalfTime.getValue();

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        Match match = player.getMatch();
        if (match == null) {
            return true;
        }

        int mods = reader.readInt();

        if (match.getSpecialMode() == MatchSpecialMode.FREE_MOD) {
            if (match.getHostId() == player.getId()) {
                match.setMods(mods & SPEED_CHANGING_MODS);
            }

            Integer slot = match.getSlot(player);
            if (slot != null) {
                match.getSlots()[slot].setMods(mods & ~SPEED_CHANGING_MODS);
            }
        } else {
            if (match.getHostId() != player.getId()) {
                return true;
            }

            match.setMods(mods);
        }

        match.enqueUpdate();
        return true;
    }


}
