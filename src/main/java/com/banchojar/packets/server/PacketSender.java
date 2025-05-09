package com.banchojar.packets.server;

import com.banchojar.Player;

import java.io.IOException;

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

        packetHandler.handle(null, packetWriter, new Player(-1, false));
    }

    
    public byte[] toBytes() {
        return packetWriter.getPackets();  // Convert all packets to byte array
    }
}
