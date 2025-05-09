package com.banchojar.packets.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BanchoPacketWriter {

    static Logger logger = LoggerFactory.getLogger("packets");

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
            finalPacket.write(length & 0xFF);           // Least significant byte first
            finalPacket.write((length >> 8) & 0xFF);
            finalPacket.write((length >> 16) & 0xFF);
            finalPacket.write((length >> 24) & 0xFF);   // Most significant byte last
            
            // Write the actual payload
            finalPacket.write(payload);
            
            // Log packet details
            StringBuilder sb = new StringBuilder();
            byte[] packetBytes = finalPacket.toByteArray();
            int packetId = (packetBytes[0] & 0xFF) | ((packetBytes[1] & 0xFF) << 8);
            
            sb.append("Writing Packet: ID=(").append(packetId).append(") NAME=<").append(ServerPackets.getNameById(packetId));
            sb.append(">, Length=(").append(length);
            sb.append("), HEX: <");
            for (byte b : packetBytes) {
                sb.append(String.format("%02X ", b));
            }
            sb.delete(sb.length()-1, sb.length());
            sb.append(">");
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
        // A single byte is the same in both endianness
        output.write(value & 0xFF);
    }

    public void writeShort(short value) throws IOException {
        // Changed to little-endian to be consistent with packet format
        writeShortLE(value);
    }

    public void writeInt(int value) {
        // Changed to little-endian to be consistent with packet format
        writeIntLE(value);
    }

    public void writeShortLE(short value) throws IOException {
        output.write(new byte[]{
			(byte) (value & 0xFF),
			(byte) ((value >> 8) & 0xFF)
		});
    }

    public void writeIntLE(int value) {
        // Little-endian: Low byte first, then high bytes
        output.write(value & 0xFF);         // Low byte first
        output.write((value >> 8) & 0xFF);
        output.write((value >> 16) & 0xFF);
        output.write((value >> 24) & 0xFF); // Most significant byte last
    }

    public void writeByteLE(int value) {
        // A single byte is the same in both endianness
        output.write(value & 0xFF);
    }

    public void writeLongLE(long value) throws IOException {
        // Write 8 bytes in little-endian order
        output.write(new byte[]{
			(byte) (value & 0xFF),
			(byte) ((value >> 8) & 0xFF),
			(byte) ((value >> 16) & 0xFF),
			(byte) ((value >> 24) & 0xFF),
			(byte) ((value >> 32) & 0xFF),
			(byte) ((value >> 40) & 0xFF),
			(byte) ((value >> 48) & 0xFF),
			(byte) ((value >> 56) & 0xFF),
		});
    }
    

    public void writeLong(long value) throws IOException {
        // Changed to little-endian to be consistent with packet format
        writeLongLE(value);
    }

    public void writeFloat(float value) throws IOException {
       byte[] bytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(value).array();
       output.write(bytes);
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