package com.banchojar.packets.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BanchoPacketWriter {

    public Logger logger = LoggerFactory.getLogger(BanchoPacketWriter.class);

    private final List<byte[]> packets = new ArrayList<>();
    private ByteArrayOutputStream packetBuffer;
    private ByteArrayOutputStream output;

    public BanchoPacketWriter() {}

    public void startPacket(int id) {
        packetBuffer = new ByteArrayOutputStream();
        
        // Write packet ID (2 bytes, little-endian)
        packetBuffer.write(id & 0xFF);           // Low byte first
        packetBuffer.write((id >> 8) & 0xFF);    // High byte next
        
        // Write compression flag (1 byte, always 0 for modern clients)
        packetBuffer.write(0);                   
        
        // We'll write the payload length after we know what it is
        output = new ByteArrayOutputStream();    // This will hold our payload
    }

    public void endPacket() {
        byte[] payload = output.toByteArray();
        int length = payload.length;
        
        
        // Create a new ByteArrayOutputStream for the complete packet
        ByteArrayOutputStream finalPacket = new ByteArrayOutputStream();
        
        try {
            // Copy the header we already started (ID and compression flag)
            finalPacket.write(packetBuffer.toByteArray());
            
            // Write the content length (4 bytes, little-endian)
            finalPacket.write(length & 0xFF);           // Byte 0 (least significant)
            finalPacket.write((length >> 8) & 0xFF);    // Byte 1
            finalPacket.write((length >> 16) & 0xFF);   // Byte 2
            finalPacket.write((length >> 24) & 0xFF);   // Byte 3 (most significant)
            
            // Write the actual payload
            finalPacket.write(payload);
            
            // Log packet details
            StringBuilder sb = new StringBuilder();
            byte[] packetBytes = finalPacket.toByteArray();
            int packetId = (packetBytes[0] & 0xFF) | ((packetBytes[1] & 0xFF) << 8);
            
            sb.append("Packet: ID=").append(packetId).append( " NAME=" + ServerPackets.getNameById(packetId));
            sb.append(", Length=").append(length);
            sb.append(", Full packet in hex: ");
            for (byte b : packetBytes) {
                sb.append(String.format("%02X ", b));
            }
            logger.info(sb.toString());
            
            // Add the complete packet to the list
            packets.add(finalPacket.toByteArray());
            
        } catch (IOException e) {
            logger.error("Error assembling packet: ", e);
        }
    }

    public byte[] getPackets() {
        ByteArrayOutputStream all = new ByteArrayOutputStream();
        for (byte[] packet : packets) {
            try {
                all.write(packet);
            } catch (IOException e) {
                logger.error("Error combining packets: ", e);
            }
        }
        return all.toByteArray();
    }

    public void writeByte(int value) {
        output.write(value & 0xFF);
    }

    public void writeShort(short value) {
        // Little-endian
        output.write(value & 0xFF);            // Low byte first
        output.write((value >> 8) & 0xFF);     // High byte next
    }

    public void writeInt(int value) {
        // Little-endian
        output.write(value & 0xFF);            // Byte 0 (least significant)
        output.write((value >> 8) & 0xFF);     // Byte 1
        output.write((value >> 16) & 0xFF);    // Byte 2
        output.write((value >> 24) & 0xFF);    // Byte 3 (most significant)
    }

    public void writeLong(long value) {
        // Little-endian
        for (int i = 0; i < 8; i++) {
            output.write((int) (value >> (i * 8)) & 0xFF);
        }
    }

    public void writeFloat(float value) {
        int intBits = Float.floatToIntBits(value);
        writeInt(intBits);
    }

    public void writeBoolean(boolean value) {
        writeByte(value ? 1 : 0);
    }

    public void writeString(String str) {
        if (str == null || str.isEmpty()) {
            writeByte(0x00); // Empty/null string indicator
            return;
        }

        writeByte(0x0b); // String present indicator
        
        // Get UTF-8 bytes of the string
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        
        // Write the length as ULEB128
        writeUleb128(bytes.length);
        
        // Write the actual string bytes
        try {
            output.write(bytes);
        } catch (IOException e) {
            logger.error("Error writing string: ", e);
        }
    }

    /**
     * Writes an unsigned LEB128 encoded integer.
     * ULEB128 is a variable-length encoding for unsigned integers.
     * 
     * @param value The value to encode
     */
    private void writeUleb128(int value) {
        do {
            byte b = (byte)(value & 0x7F);
            value >>= 7;
            if (value != 0) {
                b |= 0x80;  // Set the continuation bit
            }
            writeByte(b);
        } while (value != 0);
    }
}