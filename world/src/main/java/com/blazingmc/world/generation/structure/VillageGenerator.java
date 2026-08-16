package com.blazingmc.world.generation.structure;

import org.bukkit.Material;

import java.util.Random;

public class VillageGenerator {
    private static final int PATH_WIDTH = 3;
    private static final int PATH_LENGTH = 16;
    
    private final long seed;
    private final Random random;
    
    public VillageGenerator(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
    }
    
    public void generate(int chunkX, int chunkZ, short[] blockIds, int surfaceHeight) {
        Random villageRng = new Random(seed ^ ((long) chunkX * 537462341L) ^ ((long) chunkZ * 845762341L));
        
        if (villageRng.nextInt(10) == 0) {
            int centerX = 8;
            int centerZ = 8;
            int centerY = surfaceHeight;
            
            placePath(blockIds, chunkX, chunkZ, centerX, centerY, centerZ, 0);
            placePath(blockIds, chunkX, chunkZ, centerX, centerY, centerZ, 1);
            placePath(blockIds, chunkX, chunkZ, centerX, centerY, centerZ, 2);
            placePath(blockIds, chunkX, chunkZ, centerX, centerY, centerZ, 3);
            
            for (int i = 0; i < 4; i++) {
                int houseX = centerX + (villageRng.nextInt(6) - 3);
                int houseZ = centerZ + (villageRng.nextInt(6) - 3);
                int houseY = centerY;
                int houseWidth = 5 + villageRng.nextInt(3);
                int houseDepth = 5 + villageRng.nextInt(3);
                int houseHeight = 4 + villageRng.nextInt(2);
                
                Material wallMat = villageRng.nextBoolean() ? Material.OAK_PLANKS : Material.COBBLESTONE;
                placeHouse(blockIds, chunkX, chunkZ, houseX, houseY, houseZ, houseWidth, houseDepth, houseHeight, wallMat);
                placeDoor(blockIds, chunkX, chunkZ, houseX + houseWidth / 2, houseY, houseZ);
            }
            
            if (villageRng.nextInt(3) == 0) {
                placeFarm(blockIds, chunkX, chunkZ, centerX + 6, centerY, centerZ);
            }
        }
    }
    
    private void placePath(short[] blockIds, int chunkX, int chunkZ, int startX, int startY, int startZ, int direction) {
        int dx = 0, dz = 0;
        switch (direction) {
            case 0 -> dx = 1;
            case 1 -> dx = -1;
            case 2 -> dz = 1;
            case 3 -> dz = -1;
        }
        
        for (int i = 1; i <= PATH_LENGTH; i++) {
            int pathX = startX + dx * i;
            int pathZ = startZ + dz * i;
            
            if (pathX >= 0 && pathX < 16 && pathZ >= 0 && pathZ < 16) {
                for (int w = -PATH_WIDTH / 2; w <= PATH_WIDTH / 2; w++) {
                    int px = pathX + (direction <= 1 ? 0 : w);
                    int pz = pathZ + (direction <= 1 ? w : 0);
                    
                    if (px >= 0 && px < 16 && pz >= 0 && pz < 16) {
                        int index = getIndex(px, startY - 1, pz);
                        if (index >= 0 && index < blockIds.length) {
                            blockIds[index] = (short) Material.GRAVEL.ordinal();
                        }
                    }
                }
            }
        }
    }
    
    private void placeHouse(short[] blockIds, int chunkX, int chunkZ, int startX, int startY, int startZ,
                           int width, int depth, int height, Material wallMat) {
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
                            if (y == 0) {
                                blockIds[index] = (short) Material.COBBLESTONE.ordinal();
                            } else if (y == height - 1) {
                                blockIds[index] = (short) Material.OAK_PLANKS.ordinal();
                            } else if (x == 0 || x == width - 1 || z == 0 || z == depth - 1) {
                                blockIds[index] = (short) wallMat.ordinal();
                            } else {
                                blockIds[index] = (short) Material.AIR.ordinal();
                            }
                        }
                    }
                }
            }
        }
        
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                int blockX = startX + x;
                int blockZ = startZ + z;
                int blockY = startY + height;
                
                if (blockX >= 0 && blockX < 16 && blockZ >= 0 && blockZ < 16 && blockY < 320) {
                    int index = getIndex(blockX, blockY, blockZ);
                    if (index >= 0 && index < blockIds.length) {
                        if ((x + z) % 2 == 0) {
                            blockIds[index] = (short) Material.OAK_PLANKS.ordinal();
                        } else {
                            blockIds[index] = (short) Material.COBBLESTONE.ordinal();
                        }
                    }
                }
            }
        }
    }
    
    private void placeDoor(short[] blockIds, int chunkX, int chunkZ, int x, int y, int z) {
        if (x >= 0 && x < 16 && z >= 0 && z < 16) {
            int index1 = getIndex(x, y, z);
            int index2 = getIndex(x, y + 1, z);
            
            if (index1 >= 0 && index1 < blockIds.length) {
                blockIds[index1] = (short) Material.OAK_DOOR.ordinal();
            }
            if (index2 >= 0 && index2 < blockIds.length) {
                blockIds[index2] = (short) Material.OAK_DOOR.ordinal();
            }
        }
    }
    
    private void placeFarm(short[] blockIds, int chunkX, int chunkZ, int startX, int startY, int startZ) {
        for (int x = 0; x < 7; x++) {
            for (int z = 0; z < 7; z++) {
                int blockX = startX + x;
                int blockZ = startZ + z;
                int blockY = startY - 1;
                
                if (blockX >= 0 && blockX < 16 && blockZ >= 0 && blockZ < 16) {
                    int index = getIndex(blockX, blockY, blockZ);
                    if (index >= 0 && index < blockIds.length) {
                        if (x == 0 || x == 6 || z == 0 || z == 6) {
                            blockIds[index] = (short) Material.OAK_FENCE.ordinal();
                        } else {
                            blockIds[index] = (short) Material.FARMLAND.ordinal();
                            int cropIndex = getIndex(blockX, blockY + 1, blockZ);
                            if (cropIndex >= 0 && cropIndex < blockIds.length && 
                                blockIds[cropIndex] == (short) Material.AIR.ordinal()) {
                                blockIds[cropIndex] = (short) Material.WHEAT.ordinal();
                            }
                        }
                    }
                }
            }
        }
    }
    
    private int getIndex(int x, int y, int z) {
        int adjustedY = y + 64;
        return (adjustedY << 8) | (z << 4) | x;
    }
}
