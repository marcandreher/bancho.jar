package com.banchojar.packets.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.banchojar.BanchoHandler;
import com.banchojar.Player;
import com.banchojar.packets.BanchoPacket;
import com.banchojar.packets.client.handlers.UnhandledHandler;
import com.banchojar.packets.server.PacketSender;

public class BanchoPacketReader {

    public Logger logger = LoggerFactory.getLogger(BanchoPacketReader.class);
    private ByteArrayInputStream data;
    private int currentPacketId;
    private int currentPacketLength;
    private int bytesReadInCurrentPacket;
    private boolean compressionFlag;
    private List<Integer> packetIds = new ArrayList<>();
    private PacketSender sender;
    private int playerId;

    public BanchoPacketReader(byte[] packetData, PacketSender sender, int playerId) {
        this.data = new ByteArrayInputStream(packetData);
        this.sender = sender;
        this.playerId = playerId;
        this.bytesReadInCurrentPacket = 0;
    }

    /**
     * Checks if there are more packets available to read
     * @return true if there are more packets, false otherwise
     */
    public boolean hasMorePackets() {
        return data.available() >= 7; // Minimum packet size: 2 (id) + 1 (compression) + 4 (length)
    }

    public List<Integer> readIntList() throws IOException {
        // Read the length (2 bytes, short)
        int lenLow = data.read() & 0xFF;
        int lenHigh = data.read() & 0xFF;
        int length = lenLow | (lenHigh << 8);
        bytesReadInCurrentPacket += 2;
        
        // Create list to hold the integers
        List<Integer> intList = new ArrayList<>(length);
        
        // Read each integer (4 bytes each)
        for (int i = 0; i < length; i++) {
            int byte0 = data.read() & 0xFF;
            int byte1 = data.read() & 0xFF;
            int byte2 = data.read() & 0xFF;
            int byte3 = data.read() & 0xFF;
            bytesReadInCurrentPacket += 4;
            
            // Combine bytes to form integer (little-endian)
            int value = byte0 | (byte1 << 8) | (byte2 << 16) | (byte3 << 24);
            intList.add(value);
        }
        
        return intList;
    }

    /**
     * Begins reading the next packet in the stream
     * @return true if the packet was handled successfully, false otherwise
     * @throws IOException if there's an error reading the packet
     */
    public boolean nextPacket() throws IOException {
        if (!hasMorePackets()) {
            logger.warn("No more packets available to read");
            return false;
        }

        // Make sure we've read all data from the previous packet before moving on
        skipRemainingPacketData();

        // Read packet ID (2 bytes, little-endian)
        int idLow = data.read() & 0xFF;
        int idHigh = data.read() & 0xFF;
        currentPacketId = idLow | (idHigh << 8);
        
        // Read compression flag (1 byte)
        compressionFlag = (data.read() & 0xFF) != 0;
        
        // Read content length (4 bytes, little-endian)
        int lengthByte0 = data.read() & 0xFF;
        int lengthByte1 = data.read() & 0xFF;
        int lengthByte2 = data.read() & 0xFF;
        int lengthByte3 = data.read() & 0xFF;
        currentPacketLength = lengthByte0 | (lengthByte1 << 8) | (lengthByte2 << 16) | (lengthByte3 << 24);
        
        // Reset the bytes read counter for this new packet
        bytesReadInCurrentPacket = 0;
        
        packetIds.add(currentPacketId);
       
        // Log packet details
        logger.info("Reading Packet: ID=" + currentPacketId + " NAME=" + 
                    ClientPackets.getNameById(currentPacketId) + 
                    ", Length=" + currentPacketLength + 
                    ", Compressed=" + compressionFlag);

        if (currentPacketLength < 0 || currentPacketLength > 100000) {
            logger.error("Invalid packet length: " + currentPacketLength);
            // Skip this packet entirely to avoid potential infinite loops
            return false;
        }

        BanchoPacketHandler handler = BanchoHandler.packetHandlers.getOrDefault(ClientPackets.getById(currentPacketId), new UnhandledHandler());
        BanchoPacket packet = new BanchoPacket(currentPacketId, compressionFlag, ClientPackets.getById(currentPacketId));
        
        boolean result = handler.handle(packet, sender, this, playerId);
        
        // After handling, if we haven't read all the data for this packet, skip the rest
        skipRemainingPacketData();
        
        return result;
    }

    /**
     * Skip any remaining unread data in the current packet
     */
    private void skipRemainingPacketData() {
        if (currentPacketLength > 0 && bytesReadInCurrentPacket < currentPacketLength) {
            int bytesToSkip = currentPacketLength - bytesReadInCurrentPacket;
            logger.info("Skipping " + bytesToSkip + " unread bytes from packet ID " + currentPacketId);
            data.skip(bytesToSkip);
            bytesReadInCurrentPacket = currentPacketLength; // Mark as fully read
        }
    }

    /**
     * Reads a byte from the current packet
     * @return the byte value
     */
    public byte readByte() {
        bytesReadInCurrentPacket++;
        return (byte) (data.read() & 0xFF);
    }

    /**
     * Reads a short (16-bit) value from the current packet
     * @return the short value
     */
    public short readShort() {
        // Little-endian
        int low = data.read() & 0xFF;
        int high = data.read() & 0xFF;
        bytesReadInCurrentPacket += 2;
        return (short) (low | (high << 8));
    }

    /**
     * Reads an int (32-bit) value from the current packet
     * @return the int value
     */
    public int readInt() {
        // Little-endian
        int value = 0;
        for (int i = 0; i < 4; i++) {
            value |= (data.read() & 0xFF) << (8 * i);
        }
        bytesReadInCurrentPacket += 4;
        return value;
    }

    /**
     * Reads a long (64-bit) value from the current packet
     * @return the long value
     */
    public long readLong() {
        // Little-endian
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value |= ((long)(data.read() & 0xFF)) << (8 * i);
        }
        bytesReadInCurrentPacket += 8;
        return value;
    }

    /**
     * Reads a float value from the current packet
     * @return the float value
     */
    public float readFloat() {
        int intBits = readInt();
        return Float.intBitsToFloat(intBits);
    }

    /**
     * Reads a boolean value from the current packet
     * @return the boolean value
     */
    public boolean readBoolean() {
        return readByte() != 0;
    }

    /**
     * Reads a string from the current packet
     * @return the string value, or null if the string indicator is 0
     * @throws IOException if there's an error reading the string
     */
    public String readString() throws IOException {
        int indicator = readByte() & 0xFF;
        
        if (indicator == 0x00) {
            // Null or empty string
            return "";
        }
        
        if (indicator != 0x0b) {
            logger.warn("Unexpected string indicator: " + indicator);
            return "";
        }
        
        // Read the string length (ULEB128)
        int length = readUleb128();
        
        // Read the string bytes
        byte[] stringBytes = new byte[length];
        int bytesRead = data.read(stringBytes, 0, length);
        bytesReadInCurrentPacket += bytesRead;
        
        return new String(stringBytes, StandardCharsets.UTF_8);
    }

    /**
     * Reads an unsigned LEB128 encoded integer
     * @return the decoded integer value
     */
    private int readUleb128() {
        int value = 0;
        int shift = 0;
        byte b;
        
        do {
            b = readByte();
            value |= ((b & 0x7F) << shift);
            shift += 7;
        } while ((b & 0x80) != 0);
        
        return value;
    }
    
    /**
     * Skips a number of bytes in the current packet
     * @param count number of bytes to skip
     */
    public void skip(int count) {
        data.skip(count);
        bytesReadInCurrentPacket += count;
    }
    
    /**
     * Returns the ID of the current packet
     * @return the current packet ID
     */
    public int getCurrentPacketId() {
        return currentPacketId;
    }
    
    /**
     * Returns the length of the current packet's payload
     * @return the current packet length
     */
    public int getCurrentPacketLength() {
        return currentPacketLength;
    }
    
    /**
     * Returns a list of all packet IDs encountered so far
     * @return list of packet IDs
     */
    public List<Integer> getPacketIds() {
        return packetIds;
    }
    
    /**
     * Returns the number of bytes remaining in the current packet
     * @return number of bytes remaining
     */
    public int available() {
        return data.available();
    }
}