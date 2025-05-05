package com.banchojar.packets.server;

import com.banchojar.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketSender {
    private BanchoPacketWriter packetWriter;
    private static final Logger logger = LoggerFactory.getLogger(PacketSender.class);

    public PacketSender() {
        this.packetWriter = new BanchoPacketWriter();
    }

    public PacketSender(BanchoPacketWriter packetWriter) {
        this.packetWriter = packetWriter;
    }

    public BanchoPacketWriter getPacketWriter() {
        return packetWriter;
    }

    // Sends the login reply packet
    public void sendLoginReply(int userId) {
        packetWriter.startPacket(ServerPackets.LOGIN_REPLY.getValue());  // Start new packet with LOGIN_REPLY packet ID
        packetWriter.writeInt(userId);  // Send user ID
        packetWriter.endPacket();  // Finalize the packet
    }

    // Sends the user permissions packet
    public void sendPermissions(int permissions) {
        packetWriter.startPacket(ServerPackets.PRIVILEGES.getValue());  // Start new packet with PRIVILEGES packet ID
        packetWriter.writeInt(permissions);  // Send permissions value
        packetWriter.endPacket();  // Finalize the packet
    }

    // Sends the user stats packet
    public void sendUserStats(Player player) {
        packetWriter.startPacket(ServerPackets.USER_STATS.getValue()); // Start new packet

        // Write player ID
        packetWriter.writeInt(player.getId());

        // Write player status
        packetWriter.writeByte((byte) (player.getAction() & 0xFF)); // uint8
        packetWriter.writeString(player.getActionText()); // string
        packetWriter.writeString(player.getBeatmapMd5()); // string
        packetWriter.writeInt(player.getMods()); // int32
        packetWriter.writeByte((byte) (player.getGameMode() & 0xFF)); // uint8
        packetWriter.writeInt(player.getBeatmapId()); // int32

        // Write player stats
        packetWriter.writeLong(player.getRankedScore()); // int64
        packetWriter.writeFloat(player.getAccuracy() / 100.0f); // float32 (0.0 - 1.0)
        packetWriter.writeInt(player.getPlayCount()); // int32
        packetWriter.writeLong(player.getTotalScore()); // int64
        packetWriter.writeInt(player.getGlobalRank()); // int32

        // Clamp and write PP as int16
        int pp = (int) Math.ceil(player.getPp());
        if (pp > Short.MAX_VALUE) pp = Short.MAX_VALUE;
        if (pp < Short.MIN_VALUE) pp = Short.MIN_VALUE;
        packetWriter.writeShort((short) pp); // int16

        // Log the stats being sent
        logger.info("Sending stats for player {}: ID={}, RankedScore={}, Accuracy={}, PlayCount={}, TotalScore={}, GlobalRank={}, PP={}",
            player.getUsername(), player.getId(), player.getRankedScore(), player.getAccuracy(), player.getPlayCount(),
            player.getTotalScore(), player.getGlobalRank(), pp);

        packetWriter.endPacket(); // Finalize packet
    }
    

    public void sendUserPresence(Player player) {
        packetWriter.startPacket(ServerPackets.USER_PRESENCE.getValue());  // Start new packet with USER_PRESENCE packet ID
        
        packetWriter.writeInt(player.getId());
        packetWriter.writeString(player.getUsername());
        packetWriter.writeByte(player.getTimezone() + 24);
        packetWriter.writeByte(player.getCountry());
        byte permissionsAndMode = (byte) ((player.getPrivileges() | (player.getMode() << 5)) & 0xFF);
        packetWriter.writeByte(permissionsAndMode);
        packetWriter.writeFloat(player.getLongitude());
        packetWriter.writeFloat(player.getLatitude());
        packetWriter.writeInt(player.getRank());
        
        packetWriter.endPacket();  // Finalize the packet with the user presence details
    }

    public void sendChannelInfo(String channel, String description, int userCount) {
        packetWriter.startPacket(ServerPackets.CHANNEL_INFO.getValue());
    
        packetWriter.writeString(channel);          // Channel name
        packetWriter.writeString(description);   // Channel description
        packetWriter.writeInt(userCount);        // Number of connected users
    
        packetWriter.endPacket();
    }

    public void sendChannelAutojoinAvailable(String channel) {
        packetWriter.startPacket(ServerPackets.CHANNEL_AUTO_JOIN.getValue());  // Start new packet with CHANNEL_AUTO_JOIN packet ID
        packetWriter.writeString(channel);  // Channel name
        packetWriter.endPacket();  // Finalize the packet
    }

    public void sendChannelJoinSuccess(String channelName) {
        packetWriter.startPacket(ServerPackets.CHANNEL_JOIN_SUCCESS.getValue());
        packetWriter.writeString(channelName);
        packetWriter.endPacket();
    }

    public void sendChannelInfoEnd() {
        packetWriter.startPacket(ServerPackets.CHANNEL_INFO_END.getValue());  // Start new packet with CHANNEL_INFO_END packet ID
        packetWriter.endPacket();  // Finalize the packet
    }

    public void sendPong() {
        packetWriter.startPacket(ServerPackets.PONG.getValue());  // Start new packet with PONG packet ID
        packetWriter.endPacket();  // Finalize the packet
    }

    public void sendNotification(String message) {
        packetWriter.startPacket(ServerPackets.NOTIFICATION.getValue());  // Start new packet with NOTIFICATION packet ID
        packetWriter.writeString(message);
        packetWriter.endPacket();
    }
    
    public byte[] toBytes() {
        return packetWriter.getPackets();  // Convert all packets to byte array
    }
}
