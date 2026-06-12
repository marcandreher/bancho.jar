package com.osuserverlist.bjar.packets.client.handlers.user;

import java.io.IOException;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.osu.GameMode;
import com.osuserverlist.bjar.models.osu.Mods;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.client.BanchoPacketHandler;
import com.osuserverlist.bjar.packets.client.BanchoPacketReader;
import com.osuserverlist.bjar.packets.client.ClientPackets;
import com.osuserverlist.bjar.packets.client.engine.ClientPacket;
import com.osuserverlist.bjar.packets.server.handlers.user.UserStatsPacket;
import com.osuserverlist.bjar.server.Server;

@ClientPacket(ClientPackets.CHANGE_ACTION)
public class StatusUpdatePacket implements BanchoPacketHandler {

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        byte status = reader.readByte();
        String statusText = reader.readString();
        String beatmapMd5 = reader.readString();
        int mods = reader.readInt();
        byte gameMode = reader.readByte();
        int beatmapId = reader.readInt();

        GameMode realGameMode = GameMode.fromValue(gameMode, mods);
        boolean newRelax =
            (mods & Mods.Relax.getValue()) != 0 ||
            (mods & Mods.Relax2.getValue()) != 0;

        player.setRelaxEnabled(newRelax);
        player.setRealGameMode(realGameMode.getValue());

        player.setAction(status);
        player.setActionText(statusText);
        player.setBeatmapMd5(beatmapMd5);
        player.setMods(mods);
        player.setGameMode(gameMode);
        player.setBeatmapId(beatmapId);

        for (Player onlinePlayer : Server.getInstance().playerManager.getAll()) {
            onlinePlayer.sendPacket(new UserStatsPacket(player));
        }

        return true;
    }

}