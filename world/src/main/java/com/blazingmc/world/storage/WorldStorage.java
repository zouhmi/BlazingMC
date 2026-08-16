package com.blazingmc.world.storage;

import com.blazingmc.world.chunk.Chunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorldStorage {
    private static final Logger logger = LoggerFactory.getLogger(WorldStorage.class);
    private static final Path WORLDS_DIR = Path.of("worlds");
    
    private final String worldName;
    private final Path worldDir;
    private final Path regionDir;
    private final Map<Long, RegionFile> regionFiles;
    
    public WorldStorage(String worldName) {
        this.worldName = worldName;
        this.worldDir = WORLDS_DIR.resolve(worldName);
        this.regionDir = worldDir.resolve("region");
        this.regionFiles = new ConcurrentHashMap<>();
        
        try {
            Files.createDirectories(regionDir);
        } catch (IOException e) {
            logger.error("Failed to create world directory: {}", regionDir, e);
        }
    }
    
    public boolean chunkExists(int chunkX, int chunkZ) {
        int regionX = chunkX >> 5;
        int regionZ = chunkZ >> 5;
        int localX = chunkX & 31;
        int localZ = chunkZ & 31;
        
        try {
            RegionFile region = getRegionFile(regionX, regionZ);
            byte[] data = region.readChunkData(localX, localZ);
            return data != null && data.length > 0;
        } catch (IOException e) {
            return false;
        }
    }
    
    public Chunk loadChunk(int chunkX, int chunkZ) {
        int regionX = chunkX >> 5;
        int regionZ = chunkZ >> 5;
        int localX = chunkX & 31;
        int localZ = chunkZ & 31;
        
        try {
            RegionFile region = getRegionFile(regionX, regionZ);
            byte[] data = region.readChunkData(localX, localZ);
            
            if (data == null || data.length == 0) {
                return null;
            }
            
            return deserializeChunk(chunkX, chunkZ, data);
        } catch (IOException e) {
            logger.error("Failed to load chunk {}, {}", chunkX, chunkZ, e);
            return null;
        }
    }
    
    public boolean saveChunk(Chunk chunk) {
        int regionX = chunk.getX() >> 5;
        int regionZ = chunk.getZ() >> 5;
        int localX = chunk.getX() & 31;
        int localZ = chunk.getZ() & 31;
        
        try {
            RegionFile region = getRegionFile(regionX, regionZ);
            byte[] data = serializeChunk(chunk);
            region.writeChunkData(localX, localZ, data);
            return true;
        } catch (IOException e) {
            logger.error("Failed to save chunk {}, {}", chunk.getX(), chunk.getZ(), e);
            return false;
        }
    }
    
    private RegionFile getRegionFile(int regionX, int regionZ) throws IOException {
        long key = getKey(regionX, regionZ);
        RegionFile region = regionFiles.get(key);
        
        if (region == null) {
            Path regionPath = regionDir.resolve("r." + regionX + "." + regionZ + ".mca");
            region = new RegionFile(regionPath, regionX, regionZ);
            regionFiles.put(key, region);
        }
        
        return region;
    }
    
    private byte[] serializeChunk(Chunk chunk) {
        int sectionCount = Chunk.SECTIONS;
        int blockSize = Chunk.WIDTH * Chunk.SECTION_HEIGHT * Chunk.WIDTH;
        
        int dataSize = 4 + (sectionCount * blockSize * 2) + (Chunk.WIDTH * Chunk.WIDTH * 4);
        ByteBuffer buffer = ByteBuffer.allocate(dataSize).order(ByteOrder.BIG_ENDIAN);
        
        buffer.putInt(sectionCount);
        
        short[] blockIds = chunk.getBlockIds();
        byte[] blockData = chunk.getBlockData();
        
        for (int section = 0; section < sectionCount; section++) {
            for (int y = 0; y < Chunk.SECTION_HEIGHT; y++) {
                for (int z = 0; z < Chunk.WIDTH; z++) {
                    for (int x = 0; x < Chunk.WIDTH; x++) {
                        int index = ((section * Chunk.SECTION_HEIGHT + y) << 8) | (z << 4) | x;
                        buffer.putShort(blockIds[index]);
                        buffer.put(blockData[index]);
                    }
                }
            }
        }
        
        for (int z = 0; z < Chunk.WIDTH; z++) {
            for (int x = 0; x < Chunk.WIDTH; x++) {
                buffer.putInt(chunk.getHeightmap(x, z));
            }
        }
        
        buffer.flip();
        byte[] result = new byte[buffer.remaining()];
        buffer.get(result);
        return result;
    }
    
    private Chunk deserializeChunk(int chunkX, int chunkZ, byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);
        
        int sectionCount = buffer.getInt();
        int blockSize = Chunk.WIDTH * Chunk.SECTION_HEIGHT * Chunk.WIDTH;
        
        Chunk chunk = new Chunk(chunkX, chunkZ);
        short[] blockIds = chunk.getBlockIds();
        byte[] blockData = chunk.getBlockData();
        
        for (int section = 0; section < sectionCount && section < Chunk.SECTIONS; section++) {
            for (int y = 0; y < Chunk.SECTION_HEIGHT; y++) {
                for (int z = 0; z < Chunk.WIDTH; z++) {
                    for (int x = 0; x < Chunk.WIDTH; x++) {
                        int index = ((section * Chunk.SECTION_HEIGHT + y) << 8) | (z << 4) | x;
                        if (buffer.remaining() >= 3) {
                            blockIds[index] = buffer.getShort();
                            blockData[index] = buffer.get();
                        }
                    }
                }
            }
        }
        
        for (int z = 0; z < Chunk.WIDTH; z++) {
            for (int x = 0; x < Chunk.WIDTH; x++) {
                if (buffer.remaining() >= 4) {
                    chunk.setHeightmap(x, z, buffer.getInt());
                }
            }
        }
        
        chunk.setGenerated(true);
        return chunk;
    }
    
    private long getKey(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }
    
    public void saveAll() {
        for (RegionFile region : regionFiles.values()) {
            try {
                region.saveRegion();
            } catch (IOException e) {
                logger.error("Failed to save region file", e);
            }
        }
    }
    
    public String getWorldName() { return worldName; }
    public Path getWorldDir() { return worldDir; }
    public Path getRegionDir() { return regionDir; }
}