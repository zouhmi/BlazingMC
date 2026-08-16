package com.blazingmc.world.chunk;

import com.blazingmc.world.generation.TerrainGenerator;
import com.blazingmc.world.storage.WorldStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChunkManager {
    private static final Logger logger = LoggerFactory.getLogger(ChunkManager.class);
    private final Map<Long, Chunk> chunks;
    private final TerrainGenerator terrainGenerator;
    private final WorldStorage worldStorage;
    private final ExecutorService chunkGenExecutor;
    private final int viewDistance;
    
    public ChunkManager(long worldSeed, int viewDistance, String worldName) {
        this.chunks = new ConcurrentHashMap<>();
        this.terrainGenerator = new TerrainGenerator(worldSeed);
        this.worldStorage = new WorldStorage(worldName);
        this.chunkGenExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.viewDistance = viewDistance;
    }
    
    public Chunk getChunk(int x, int z) {
        long key = getKey(x, z);
        return chunks.get(key);
    }
    
    public Chunk getOrCreateChunk(int x, int z) {
        long key = getKey(x, z);
        Chunk existingChunk = chunks.get(key);
        
        if (existingChunk == null) {
            Chunk loadedChunk = worldStorage.loadChunk(x, z);
            
            if (loadedChunk != null) {
                chunks.put(key, loadedChunk);
                return loadedChunk;
            }
            
            final Chunk newChunk = new Chunk(x, z);
            chunks.put(key, newChunk);
            
            if (!newChunk.isGenerated()) {
                final TerrainGenerator gen = terrainGenerator;
                chunkGenExecutor.submit(() -> {
                    newChunk.generate(gen);
                    logger.debug("Generated chunk at {}, {}", x, z);
                });
            }
            
            return newChunk;
        }
        
        return existingChunk;
    }
    
    public void unloadChunk(int x, int z) {
        long key = getKey(x, z);
        Chunk chunk = chunks.remove(key);
        
        if (chunk != null && chunk.isDirty()) {
            worldStorage.saveChunk(chunk);
        }
    }
    
    public void loadChunksAround(int centerX, int centerZ) {
        for (int x = centerX - viewDistance; x <= centerX + viewDistance; x++) {
            for (int z = centerZ - viewDistance; z <= centerZ + viewDistance; z++) {
                getOrCreateChunk(x, z);
            }
        }
    }
    
    public List<Chunk> getChunksAround(int centerX, int centerZ) {
        List<Chunk> result = new ArrayList<>();
        for (int x = centerX - viewDistance; x <= centerX + viewDistance; x++) {
            for (int z = centerZ - viewDistance; z <= centerZ + viewDistance; z++) {
                Chunk chunk = getChunk(x, z);
                if (chunk != null) {
                    result.add(chunk);
                }
            }
        }
        return result;
    }
    
    public int getLoadedChunkCount() {
        return chunks.size();
    }
    
    public void saveAll() {
        for (Chunk chunk : chunks.values()) {
            if (chunk.isDirty()) {
                worldStorage.saveChunk(chunk);
                chunk.setDirty(false);
            }
        }
    }
    
    public void clear() {
        saveAll();
        chunks.clear();
    }
    
    public void shutdown() {
        saveAll();
        chunkGenExecutor.shutdown();
    }
    
    public TerrainGenerator getTerrainGenerator() {
        return terrainGenerator;
    }
    
    public WorldStorage getWorldStorage() {
        return worldStorage;
    }
    
    public int getViewDistance() {
        return viewDistance;
    }
    
    private long getKey(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }
}