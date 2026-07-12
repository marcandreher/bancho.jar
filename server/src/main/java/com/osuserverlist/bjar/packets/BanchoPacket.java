package com.osuserverlist.bjar.packets;

import com.osuserverlist.bjar.packets.client.engine.ClientPackets;

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