package com.osuserverlist.bjar.packets.server.handlers.spectate;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.server.BanchoPacketWriter;
import com.osuserverlist.bjar.packets.server.ServerPacketHandler;
import com.osuserverlist.bjar.packets.server.ServerPackets;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SendSpectateFramesPacket implements ServerPacketHandler {
    final byte[] data;

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) {
        writer.startPacket(ServerPackets.SPECTATE_FRAMES.getValue());
        writer.writeBytes(data);
        writer.endPacket();

        return true;
    }
}
