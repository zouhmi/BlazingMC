package com.blazingmc.world.storage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

public class RegionFile {
    private static final int HEADER_SIZE = 8192;
    private static final int SECTOR_SIZE = 4096;
    private static final int MAX_CHUNK_SIZE = SECTOR_SIZE * 255;
    
    private final Path filePath;
    private final int regionX;
    private final int regionZ;
    private byte[] header;
    private byte[] data;
    private int[] offsets;
    private int[] timestamps;
    
    public RegionFile(Path filePath, int regionX, int regionZ) throws IOException {
        this.filePath = filePath;
        this.regionX = regionX;
        this.regionZ = regionZ;
        
        if (Files.exists(filePath)) {
            loadRegion();
        } else {
            createRegion();
        }
    }
    
    private void createRegion() throws IOException {
        header = new byte[HEADER_SIZE];
        data = new byte[SECTOR_SIZE];
        offsets = new int[1024];
        timestamps = new int[1024];
        
        Files.createDirectories(filePath.getParent());
        saveRegion();
    }
    
    private void loadRegion() throws IOException {
        byte[] fileData = Files.readAllBytes(filePath);
        
        if (fileData.length < HEADER_SIZE) {
            createRegion();
            return;
        }
        
        header = new byte[HEADER_SIZE];
        System.arraycopy(fileData, 0, header, 0, HEADER_SIZE);
        
        ByteBuffer headerBuffer = ByteBuffer.wrap(header).order(ByteOrder.BIG_ENDIAN);
        
        offsets = new int[1024];
        timestamps = new int[1024];
        
        for (int i = 0; i < 1024; i++) {
            offsets[i] = headerBuffer.getInt();
        }
        
        for (int i = 0; i < 1024; i++) {
            timestamps[i] = headerBuffer.getInt();
        }
        
        data = new byte[fileData.length - HEADER_SIZE];
        System.arraycopy(fileData, HEADER_SIZE, data, 0, data.length);
    }
    
    public void saveRegion() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE + data.length).order(ByteOrder.BIG_ENDIAN);
        
        for (int offset : offsets) {
            buffer.putInt(offset);
        }
        
        for (int timestamp : timestamps) {
            buffer.putInt(timestamp);
        }
        
        buffer.put(data);
        
        Files.write(filePath, buffer.array());
    }
    
    public byte[] readChunkData(int localX, int localZ) throws IOException {
        if (localX < 0 || localX >= 32 || localZ < 0 || localZ >= 32) {
            throw new IllegalArgumentException("Local coordinates out of range: " + localX + ", " + localZ);
        }
        
        int index = localZ * 32 + localX;
        int offset = offsets[index];
        
        if (offset == 0) {
            return null;
        }
        
        int sectorNumber = offset >> 8;
        int sectorCount = offset & 0xFF;
        
        if (sectorNumber < 2 || sectorNumber * SECTOR_SIZE >= data.length) {
            return null;
        }
        
        int startOffset = (sectorNumber - 2) * SECTOR_SIZE;
        
        if (startOffset + sectorCount * SECTOR_SIZE > data.length) {
            return null;
        }
        
        ByteBuffer chunkHeader = ByteBuffer.wrap(data, startOffset, 5).order(ByteOrder.BIG_ENDIAN);
        int length = chunkHeader.getInt();
        byte compressionType = chunkHeader.get();
        
        if (length <= 0 || length > MAX_CHUNK_SIZE) {
            return null;
        }
        
        byte[] compressedData = new byte[length];
        System.arraycopy(data, startOffset + 5, compressedData, 0, length);
        
        return decompressChunkData(compressedData, compressionType);
    }
    
    public void writeChunkData(int localX, int localZ, byte[] chunkData) throws IOException {
        if (localX < 0 || localX >= 32 || localZ < 0 || localZ >= 32) {
            throw new IllegalArgumentException("Local coordinates out of range: " + localX + ", " + localZ);
        }
        
        byte[] compressedData = compressChunkData(chunkData);
        int totalLength = compressedData.length + 5;
        int sectorsNeeded = (totalLength + SECTOR_SIZE - 1) / SECTOR_SIZE;
        
        if (sectorsNeeded > 255) {
            throw new IOException("Chunk data too large: " + totalLength + " bytes");
        }
        
        int index = localZ * 32 + localX;
        int oldOffset = offsets[index];
        int oldSectorNumber = oldOffset >> 8;
        int oldSectorCount = oldOffset & 0xFF;
        
        int newSectorNumber = findFreeSectors(sectorsNeeded, oldSectorNumber);
        
        if (newSectorNumber < 0) {
            expandData(sectorsNeeded);
            newSectorNumber = findFreeSectors(sectorsNeeded, oldSectorNumber);
        }
        
        offsets[index] = (newSectorNumber << 8) | sectorsNeeded;
        timestamps[index] = (int) (System.currentTimeMillis() / 1000);
        
        ByteBuffer chunkBuffer = ByteBuffer.allocate(totalLength).order(ByteOrder.BIG_ENDIAN);
        chunkBuffer.putInt(compressedData.length);
        chunkBuffer.put((byte) 1);
        chunkBuffer.put(compressedData);
        
        int startOffset = (newSectorNumber - 2) * SECTOR_SIZE;
        while (startOffset + totalLength > data.length) {
            byte[] newData = new byte[data.length + SECTOR_SIZE];
            System.arraycopy(data, 0, newData, 0, data.length);
            data = newData;
        }
        
        System.arraycopy(chunkBuffer.array(), 0, data, startOffset, totalLength);
        
        saveRegion();
    }
    
    private int findFreeSectors(int needed, int excludeStart) {
        int consecutive = 0;
        int start = -1;
        
        for (int i = 2; i * SECTOR_SIZE < data.length; i++) {
            if (i == excludeStart || (excludeStart > 0 && i >= excludeStart && i < excludeStart + (data[excludeStart * SECTOR_SIZE - HEADER_SIZE] & 0xFF))) {
                consecutive = 0;
                start = -1;
                continue;
            }
            
            boolean used = false;
            for (int j = 0; j < 1024; j++) {
                int offset = offsets[j];
                if (offset == 0) continue;
                
                int sectorNum = offset >> 8;
                int sectorCount = offset & 0xFF;
                
                if (i >= sectorNum && i < sectorNum + sectorCount) {
                    used = true;
                    break;
                }
            }
            
            if (!used) {
                if (consecutive == 0) {
                    start = i;
                }
                consecutive++;
                
                if (consecutive >= needed) {
                    return start;
                }
            } else {
                consecutive = 0;
                start = -1;
            }
        }
        
        return -1;
    }
    
    private void expandData(int additionalSectors) {
        byte[] newData = new byte[data.length + additionalSectors * SECTOR_SIZE];
        System.arraycopy(data, 0, newData, 0, data.length);
        data = newData;
    }
    
    private byte[] compressChunkData(byte[] data) {
        return data;
    }
    
    private byte[] decompressChunkData(byte[] data, byte compressionType) {
        return data;
    }
    
    public int getRegionX() { return regionX; }
    public int getRegionZ() { return regionZ; }
    public Path getFilePath() { return filePath; }
}