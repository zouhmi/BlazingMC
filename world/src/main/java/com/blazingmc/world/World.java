package com.blazingmc.world;

import com.blazingmc.protocol.handler.WorldInterface;
import com.blazingmc.world.chunk.Chunk;
import com.blazingmc.world.chunk.ChunkManager;
import com.blazingmc.world.chunk.FluidManager;
import com.blazingmc.world.chunk.LightEngine;
import com.blazingmc.world.entity.Entity;
import com.blazingmc.world.entity.EntityManager;
import org.bukkit.Material;

import java.util.Collection;
import java.util.Collections;

public class World implements WorldInterface {
    private final String name;
    private final long seed;
    private final ChunkManager chunkManager;
    private final EntityManager entityManager;
    private final LightEngine lightEngine;
    private final FluidManager fluidManager;
    private long time;
    private long fullTime;
    private int weatherDuration;
    
    public World(String name, long seed, int viewDistance) {
        this.name = name;
        this.seed = seed;
        this.chunkManager = new ChunkManager(seed, viewDistance, name);
        this.entityManager = new EntityManager();
        this.lightEngine = new LightEngine(this);
        this.fluidManager = new FluidManager(this);
        this.time = 0;
        this.fullTime = 0;
        this.weatherDuration = 0;
    }
    
    public String getName() { return name; }
    public long getSeed() { return seed; }
    public ChunkManager getChunkManager() { return chunkManager; }
    public EntityManager getEntityManager() { return entityManager; }
    public LightEngine getLightEngine() { return lightEngine; }
    public FluidManager getFluidManager() { return fluidManager; }
    
    public Collection<Entity> getEntities() {
        return Collections.unmodifiableCollection(entityManager.getEntities().values());
    }
    
    public void addEntity(Entity entity) {
        entityManager.addEntity(entity);
    }
    
    public void removeEntity(int entityId) {
        entityManager.removeEntity(entityId);
    }
    
    public Chunk getChunkAt(int x, int z) {
        return chunkManager.getOrCreateChunk(x, z);
    }
    
    public Chunk getChunkAtBlock(int x, int y, int z) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        return getChunkAt(chunkX, chunkZ);
    }
    
    public Material getBlockAt(int x, int y, int z) {
        Chunk chunk = getChunkAtBlock(x, y, z);
        int blockX = x & 15;
        int blockZ = z & 15;
        return chunk.getBlock(blockX, y, blockZ);
    }
    
    public void setBlockAt(int x, int y, int z, Material material) {
        Chunk chunk = getChunkAtBlock(x, y, z);
        int blockX = x & 15;
        int blockZ = z & 15;
        Material oldBlock = chunk.getBlock(blockX, y, blockZ);
        chunk.setBlock(blockX, y, blockZ, material);
        
        lightEngine.onBlockChange(x, y, z, oldBlock, material);
        fluidManager.onBlockPlace(x, y, z, material);
    }
    
    public int getHighestBlockY(int x, int z) {
        Chunk chunk = getChunkAt(x >> 4, z >> 4);
        return chunk.getHighestBlockY(x & 15, z & 15);
    }
    
    public long getTime() { return time; }
    public void setTime(long time) { this.time = time; }
    public long getFullTime() { return fullTime; }
    public void setFullTime(long fullTime) { this.fullTime = fullTime; }
    
    public int getWeatherDuration() { return weatherDuration; }
    public void setWeatherDuration(int weatherDuration) { this.weatherDuration = weatherDuration; }
    
    public void tick() {
        time++;
        fullTime++;
        
        fluidManager.tick();
        
        if (weatherDuration > 0) {
            weatherDuration--;
        }
    }
    
    public void shutdown() {
        chunkManager.shutdown();
    }
}