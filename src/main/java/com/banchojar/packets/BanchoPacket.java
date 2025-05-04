package com.banchojar.packets;

import com.banchojar.packets.client.ClientPackets;

public class BanchoPacket {
    public int id;
    public boolean compressed;
    public ClientPackets type;

    public BanchoPacket(int id, boolean compressed, ClientPackets type) {
        this.id = id;
        this.compressed = compressed;
        this.type = type;
    }
}