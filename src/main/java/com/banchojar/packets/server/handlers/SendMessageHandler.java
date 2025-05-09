package com.banchojar.packets.server.handlers;

import com.banchojar.Player;
import com.banchojar.packets.BanchoPacket;
import com.banchojar.packets.server.BanchoPacketWriter;
import com.banchojar.packets.server.ServerPacketHandler;
import com.banchojar.packets.server.ServerPackets;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SendMessageHandler implements ServerPacketHandler {

    private String senderName;
    private String message;
    private String target;
    private int senderId;

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) throws java.io.IOException {
        writer.startPacket(ServerPackets.SEND_MESSAGE.getValue());
        writer.writeString(senderName);
        writer.writeString(message);
        writer.writeString(target);
        writer.writeInt(senderId);
        writer.endPacket();
        return true;
    }
    
}
