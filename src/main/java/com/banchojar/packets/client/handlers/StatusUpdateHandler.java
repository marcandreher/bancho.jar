package com.banchojar.packets.client.handlers;

import java.io.IOException;

import com.banchojar.Player;
import com.banchojar.Server;
import com.banchojar.packets.BanchoPacket;
import com.banchojar.packets.client.BanchoPacketHandler;
import com.banchojar.packets.client.BanchoPacketReader;
import com.banchojar.packets.server.handlers.UserStatsHandler;

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

        for (Player onlinePlayer : Server.players.values()) {
            onlinePlayer.addPacketToStack(new UserStatsHandler(player));
        }

        return true;
    }

}
