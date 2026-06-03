package com.osuserverlist.bjar.packets.server;

import java.io.IOException;
import java.util.UUID;

import com.osuserverlist.bjar.models.essentials.Player;

public class PacketSender {
    private BanchoPacketWriter packetWriter;

    public PacketSender() {
        this.packetWriter = new BanchoPacketWriter();
    }

    public PacketSender(BanchoPacketWriter packetWriter) {
        this.packetWriter = packetWriter;
    }

    public BanchoPacketWriter getPacketWriter() {
        return packetWriter;
    }

    public void handlePacketHandler(ServerPacketHandler packetHandler) throws IOException {

        packetHandler.handle(null, packetWriter, new Player(-1, false, UUID.randomUUID().toString()));
    }

    
    public byte[] toBytes() {
        return packetWriter.getPackets();  // Convert all packets to byte array
    }
}