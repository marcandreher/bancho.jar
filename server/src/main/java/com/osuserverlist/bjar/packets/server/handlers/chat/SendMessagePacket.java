package com.osuserverlist.bjar.packets.server.handlers.chat;

import java.io.IOException;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.server.BanchoPacketWriter;
import com.osuserverlist.bjar.packets.server.ServerPacketHandler;
import com.osuserverlist.bjar.packets.server.ServerPackets;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SendMessagePacket implements ServerPacketHandler {

    private String senderName;
    private String message;
    private String target;
    private int senderId;

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) throws IOException {
        writer.startPacket(ServerPackets.SEND_MESSAGE.getValue());
        writer.writeString(senderName);
        writer.writeString(message);
        writer.writeString(target);
        writer.writeInt(senderId);
        writer.endPacket();
        return true;
    }
    
}