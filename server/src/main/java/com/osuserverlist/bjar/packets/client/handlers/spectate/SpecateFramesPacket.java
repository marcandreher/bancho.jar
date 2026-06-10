package com.osuserverlist.bjar.packets.client.handlers.spectate;

import java.io.IOException;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.osu.replay.ReplayFrameBundle;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.client.BanchoPacketHandler;
import com.osuserverlist.bjar.packets.client.BanchoPacketReader;
import com.osuserverlist.bjar.packets.server.handlers.spectate.SendSpectateFramesPacket;

public class SpecateFramesPacket implements BanchoPacketHandler {

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        ReplayFrameBundle frames = reader.readReplayFrameBundle();

        byte[] rawData = frames.getRawData();

        for (Player spectator : player.getSpectators()) {
            spectator.sendPacket(new SendSpectateFramesPacket(rawData));
        }

        return true;
    }

}
