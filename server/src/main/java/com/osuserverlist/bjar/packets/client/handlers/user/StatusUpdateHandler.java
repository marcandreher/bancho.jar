package com.osuserverlist.bjar.packets.client.handlers.user;

import java.io.IOException;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.client.BanchoPacketHandler;
import com.osuserverlist.bjar.packets.client.BanchoPacketReader;
import com.osuserverlist.bjar.packets.server.handlers.user.UserStatsHandler;
import com.osuserverlist.bjar.server.Server;

public class StatusUpdateHandler implements BanchoPacketHandler {

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        byte status = reader.readByte();
        String statusText = reader.readString();
        String beatmapMd5 = reader.readString();
        int mods = reader.readInt();
        byte gameMode = reader.readByte();
        int beatmapId = reader.readInt();

        player.setAction(status);
        player.setActionText(statusText);
        player.setBeatmapMd5(beatmapMd5);
        player.setMods(mods);
        player.setGameMode(gameMode);
        player.setBeatmapId(beatmapId);

        for (Player onlinePlayer : Server.getInstance().playerManager.getAll()) {
            onlinePlayer.sendPacket(new UserStatsHandler(player));
        }

        return true;
    }
}