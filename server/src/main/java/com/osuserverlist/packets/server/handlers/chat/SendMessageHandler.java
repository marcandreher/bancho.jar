package com.osuserverlist.packets.server.handlers.chat;

import java.io.IOException;

import com.osuserverlist.models.essentials.Player;
import com.osuserverlist.packets.BanchoPacket;
import com.osuserverlist.packets.server.BanchoPacketWriter;
import com.osuserverlist.packets.server.ServerPacketHandler;
import com.osuserverlist.packets.server.ServerPackets;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SendMessageHandler implements ServerPacketHandler {

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