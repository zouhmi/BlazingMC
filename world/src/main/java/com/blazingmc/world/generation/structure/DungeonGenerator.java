package com.blazingmc.world.generation.structure;

import org.bukkit.Material;

import java.util.Random;

public class DungeonGenerator {
    private static final int MIN_ROOM_SIZE = 5;
    private static final int MAX_ROOM_SIZE = 9;
    private static final int MIN_ROOM_HEIGHT = 3;
    private static final int MAX_ROOM_HEIGHT = 5;
    
    private final long seed;
    private final Random random;
    
    public DungeonGenerator(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
    }
    
    public void generate(int chunkX, int chunkZ, short[] blockIds) {
        Random dungeonRng = new Random(seed ^ ((long) chunkX * 73856093L) ^ ((long) chunkZ * 19349669L));
        
        int numDungeons = dungeonRng.nextInt(3);
        
        for (int i = 0; i < numDungeons; i++) {
            if (dungeonRng.nextInt(4) == 0) {
                int roomX = dungeonRng.nextInt(8) + 4;
                int roomZ = dungeonRng.nextInt(8) + 4;
                int roomY = findDungeonY(chunkX, chunkZ, roomX, roomZ, blockIds, dungeonRng);
                
                if (roomY > -60 && roomY < 50) {
                    int width = MIN_ROOM_SIZE + dungeonRng.nextInt(MAX_ROOM_SIZE - MIN_ROOM_SIZE + 1);
                    int depth = MIN_ROOM_SIZE + dungeonRng.nextInt(MAX_ROOM_SIZE - MIN_ROOM_SIZE + 1);
                    int height = MIN_ROOM_HEIGHT + dungeonRng.nextInt(MAX_ROOM_HEIGHT - MIN_ROOM_HEIGHT + 1);
                    
                    carveRoom(blockIds, chunkX, chunkZ, roomX, roomY, roomZ, width, depth, height);
                    placeCobbleFloor(blockIds, chunkX, chunkZ, roomX, roomY, roomZ, width, depth);
                    placeSpawner(blockIds, chunkX, chunkZ, roomX + width / 2, roomY + 1, roomZ + depth / 2);
                    placeLootChest(blockIds, chunkX, chunkZ, roomX, roomY, roomZ, width, depth, dungeonRng);
                }
            }
        }
    }
    
    private int findDungeonY(int chunkX, int chunkZ, int localX, int localZ, short[] blockIds, Random rng) {
        int worldX = chunkX * 16 + localX;
        int worldZ = chunkZ * 16 + localZ;
        
        for (int y = 50; y > -64; y--) {
            int index = getIndex(localX, y, localZ);
            if (index >= 0 && index < blockIds.length) {
                Material mat = Material.values()[blockIds[index]];
                if (mat == Material.STONE || mat == Material.COBBLESTONE) {
                    if (y > -60) {
                        return y + 1;
                    }
                }
            }
        }
        
        return 0;
    }
    
    private void carveRoom(short[] blockIds, int chunkX, int chunkZ, int startX, int startY, int startZ, 
                          int width, int depth, int height) {
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                for (int y = 0; y < height; y++) {
                    int blockX = startX + x;
                    int blockZ = startZ + z;
                    int blockY = startY + y;
                    
                    if (blockX >= 0 && blockX < 16 && blockZ >= 0 && blockZ < 16 && 
                        blockY >= -64 && blockY < 320) {
                        int index = getIndex(blockX, blockY, blockZ);
                        if (index >= 0 && index < blockIds.length) {
                            if (y == 0 || y == height - 1 || x == 0 || x == width - 1 || z == 0 || z == depth - 1) {
                                blockIds[index] = (short) Material.COBBLESTONE.ordinal();
                            } else {
                                blockIds[index] = (short) Material.AIR.ordinal();
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void placeCobbleFloor(short[] blockIds, int chunkX, int chunkZ, int startX, int startY, int startZ,
                                 int width, int depth) {
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                int blockX = startX + x;
                int blockZ = startZ + z;
                int blockY = startY - 1;
                
                if (blockX >= 0 && blockX < 16 && blockZ >= 0 && blockZ < 16) {
                    int index = getIndex(blockX, blockY, blockZ);
                    if (index >= 0 && index < blockIds.length) {
                        blockIds[index] = (short) Material.COBBLESTONE.ordinal();
                    }
                }
            }
        }
    }
    
    private void placeSpawner(short[] blockIds, int chunkX, int chunkZ, int x, int y, int z) {
        if (x >= 0 && x < 16 && z >= 0 && z < 16 && y >= -64 && y < 320) {
            int index = getIndex(x, y, z);
            if (index >= 0 && index < blockIds.length) {
                blockIds[index] = (short) Material.SPAWNER.ordinal();
            }
        }
    }
    
    private void placeLootChest(short[] blockIds, int chunkX, int chunkZ, int startX, int startY, int startZ,
                               int width, int depth, Random rng) {
        int chestX = startX + (rng.nextBoolean() ? 1 : width - 2);
        int chestZ = startZ + (rng.nextBoolean() ? 1 : depth - 2);
        int chestY = startY;
        
        if (chestX >= 0 && chestX < 16 && chestZ >= 0 && chestZ < 16) {
            int index = getIndex(chestX, chestY, chestZ);
            if (index >= 0 && index < blockIds.length) {
                blockIds[index] = (short) Material.CHEST.ordinal();
            }
        }
    }
    
    private int getIndex(int x, int y, int z) {
        int adjustedY = y + 64;
        return (adjustedY << 8) | (z << 4) | x;
    }
}
