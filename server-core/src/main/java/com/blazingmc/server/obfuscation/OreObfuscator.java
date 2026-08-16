package com.blazingmc.server.obfuscation;

import com.blazingmc.chat.ConsoleLogger;
import com.blazingmc.world.chunk.Chunk;
import org.bukkit.Material;

import java.util.*;

public class OreObfuscator {
    private static final int AIR_BLOCK = 0;
    private static final int STONE_BLOCK = 1;
    private static final int DEEPSLATE_BLOCK = 11;
    
    private static final Set<Integer> ORE_BLOCK_IDS = new HashSet<>(Arrays.asList(
        14,
        15,
        16,
        42,
        48,
        49,
        56,
        129,
        130,
        213,
        214
    ));
    
    private static final Set<Integer> CHEST_BLOCK_IDS = new HashSet<>(Arrays.asList(
        54,
        55,
        146,
        154,
        158,
        159,
        138,
        140,
        143,
        137,
        210
    ));
    
    private static final Set<Integer> ENTITY_BLOCK_IDS = new HashSet<>(Arrays.asList(
        52,
        138,
        140,
        143,
        137,
        210,
        146
    ));
    
    private static final int MAX_OBFUSCATION_RADIUS = 4;
    private static final int MAX_CHEST_RADIUS = 2;
    private static final int MAX_ENTITY_RADIUS = 3;
    
    private static final Random random = new Random();
    
    private final Map<Long, Set<BlockPosition>> obfuscatedBlocks;
    private final Map<Long, Set<BlockPosition>> obfuscatedChests;
    private final Map<Long, Set<BlockPosition>> obfuscatedEntities;
    
    private boolean enabled;
    private int mode;
    private boolean[] engineMode;
    private int maxWeight;
    private int[] updateRadius;
    private boolean[] isChunkSendExecution;
    
    public OreObfuscator(boolean enabled) {
        this.enabled = enabled;
        this.mode = 1;
        this.engineMode = new boolean[]{true, false, true};
        this.maxWeight = 1;
        this.updateRadius = new int[]{1, 2, 4};
        this.isChunkSendExecution = new boolean[]{true, false, true};
        
        this.obfuscatedBlocks = new HashMap<>();
        this.obfuscatedChests = new HashMap<>();
        this.obfuscatedEntities = new HashMap<>();
    }
    
    public void obfuscateChunk(Chunk chunk) {
        if (!enabled) return;
        
        long chunkKey = getKey(chunk.getX(), chunk.getZ());
        Set<BlockPosition> orePositions = new HashSet<>();
        Set<BlockPosition> chestPositions = new HashSet<>();
        Set<BlockPosition> entityPositions = new HashSet<>();
        
        for (int x = 0; x < Chunk.WIDTH; x++) {
            for (int y = -64; y < 320; y++) {
                for (int z = 0; z < Chunk.WIDTH; z++) {
                    int blockId = chunk.getSectionData(x, y, z).blockId();
                    
                    if (ORE_BLOCK_IDS.contains(blockId)) {
                        orePositions.add(new BlockPosition(x, y, z, blockId));
                    }
                    
                    if (CHEST_BLOCK_IDS.contains(blockId)) {
                        chestPositions.add(new BlockPosition(x, y, z, blockId));
                    }
                    
                    if (ENTITY_BLOCK_IDS.contains(blockId)) {
                        entityPositions.add(new BlockPosition(x, y, z, blockId));
                    }
                }
            }
        }
        
        obfuscatedBlocks.put(chunkKey, orePositions);
        obfuscatedChests.put(chunkKey, chestPositions);
        obfuscatedEntities.put(chunkKey, entityPositions);
        
        ConsoleLogger.debug("Obfuscated " + orePositions.size() + " ores, " + 
                           chestPositions.size() + " chests, " + 
                           entityPositions.size() + " entities in chunk " + 
                           chunk.getX() + ", " + chunk.getZ());
    }
    
    public int getObfuscatedBlockId(Chunk chunk, int x, int y, int z, boolean[] shouldModify) {
        if (!enabled) return -1;
        
        long chunkKey = getKey(chunk.getX(), chunk.getZ());
        Set<BlockPosition> orePositions = obfuscatedBlocks.get(chunkKey);
        
        if (orePositions == null) return -1;
        
        for (BlockPosition pos : orePositions) {
            if (pos.x == x && pos.y == y && pos.z == z) {
                if (isInRadius(chunk, x, y, z, orePositions, MAX_OBFUSCATION_RADIUS)) {
                    return getReplacementBlockId(pos.blockId, y);
                }
            }
        }
        
        return -1;
    }
    
    public int getObfuscatedChestId(Chunk chunk, int x, int y, int z) {
        if (!enabled) return -1;
        
        long chunkKey = getKey(chunk.getX(), chunk.getZ());
        Set<BlockPosition> chestPositions = obfuscatedChests.get(chunkKey);
        
        if (chestPositions == null) return -1;
        
        for (BlockPosition pos : chestPositions) {
            if (pos.x == x && pos.y == y && pos.z == z) {
                if (isInRadius(chunk, x, y, z, chestPositions, MAX_CHEST_RADIUS)) {
                    return getReplacementBlockId(pos.blockId, y);
                }
            }
        }
        
        return -1;
    }
    
    public int getObfuscatedEntityId(Chunk chunk, int x, int y, int z) {
        if (!enabled) return -1;
        
        long chunkKey = getKey(chunk.getX(), chunk.getZ());
        Set<BlockPosition> entityPositions = obfuscatedEntities.get(chunkKey);
        
        if (entityPositions == null) return -1;
        
        for (BlockPosition pos : entityPositions) {
            if (pos.x == x && pos.y == y && pos.z == z) {
                if (isInRadius(chunk, x, y, z, entityPositions, MAX_ENTITY_RADIUS)) {
                    return getReplacementBlockId(pos.blockId, y);
                }
            }
        }
        
        return -1;
    }
    
    private boolean isInRadius(Chunk chunk, int x, int y, int z, Set<BlockPosition> positions, int radius) {
        for (BlockPosition pos : positions) {
            int dx = Math.abs(pos.x - x);
            int dy = Math.abs(pos.y - y);
            int dz = Math.abs(pos.z - z);
            
            if (dx <= radius && dy <= radius && dz <= radius) {
                return true;
            }
        }
        return false;
    }
    
    private int getReplacementBlockId(int blockId, int y) {
        if (y >= 0) {
            return STONE_BLOCK;
        } else {
            return DEEPSLATE_BLOCK;
        }
    }
    
    public void revealBlock(Chunk chunk, int x, int y, int z) {
        long chunkKey = getKey(chunk.getX(), chunk.getZ());
        Set<BlockPosition> orePositions = obfuscatedBlocks.get(chunkKey);
        
        if (orePositions != null) {
            orePositions.removeIf(pos -> pos.x == x && pos.y == y && pos.z == z);
        }
        
        Set<BlockPosition> chestPositions = obfuscatedChests.get(chunkKey);
        if (chestPositions != null) {
            chestPositions.removeIf(pos -> pos.x == x && pos.y == y && pos.z == z);
        }
        
        Set<BlockPosition> entityPositions = obfuscatedEntities.get(chunkKey);
        if (entityPositions != null) {
            entityPositions.removeIf(pos -> pos.x == x && pos.y == y && pos.z == z);
        }
    }
    
    public void revealNearby(Chunk chunk, int centerX, int centerY, int centerZ, int radius) {
        long chunkKey = getKey(chunk.getX(), chunk.getZ());
        Set<BlockPosition> orePositions = obfuscatedBlocks.get(chunkKey);
        
        if (orePositions != null) {
            orePositions.removeIf(pos -> {
                int dx = Math.abs(pos.x - centerX);
                int dy = Math.abs(pos.y - centerY);
                int dz = Math.abs(pos.z - centerZ);
                return dx <= radius && dy <= radius && dz <= radius;
            });
        }
        
        Set<BlockPosition> chestPositions = obfuscatedChests.get(chunkKey);
        if (chestPositions != null) {
            chestPositions.removeIf(pos -> {
                int dx = Math.abs(pos.x - centerX);
                int dy = Math.abs(pos.y - centerY);
                int dz = Math.abs(pos.z - centerZ);
                return dx <= radius && dy <= radius && dz <= radius;
            });
        }
        
        Set<BlockPosition> entityPositions = obfuscatedEntities.get(chunkKey);
        if (entityPositions != null) {
            entityPositions.removeIf(pos -> {
                int dx = Math.abs(pos.x - centerX);
                int dy = Math.abs(pos.y - centerY);
                int dz = Math.abs(pos.z - centerZ);
                return dx <= radius && dy <= radius && dz <= radius;
            });
        }
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setMode(int mode) {
        this.mode = mode;
    }
    
    public int getMode() {
        return mode;
    }
    
    public void setEngineMode(boolean[] engineMode) {
        this.engineMode = engineMode;
    }
    
    public boolean[] getEngineMode() {
        return engineMode;
    }
    
    public void setMaxWeight(int maxWeight) {
        this.maxWeight = maxWeight;
    }
    
    public int getMaxWeight() {
        return maxWeight;
    }
    
    public void setUpdateRadius(int[] updateRadius) {
        this.updateRadius = updateRadius;
    }
    
    public int[] getUpdateRadius() {
        return updateRadius;
    }
    
    public void setChunkSendExecution(boolean[] isChunkSendExecution) {
        this.isChunkSendExecution = isChunkSendExecution;
    }
    
    public boolean[] getChunkSendExecution() {
        return isChunkSendExecution;
    }
    
    private long getKey(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }
    
    private static class BlockPosition {
        final int x;
        final int y;
        final int z;
        final int blockId;
        
        BlockPosition(int x, int y, int z, int blockId) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.blockId = blockId;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BlockPosition that = (BlockPosition) o;
            return x == that.x && y == that.y && z == that.z;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(x, y, z);
        }
    }
}