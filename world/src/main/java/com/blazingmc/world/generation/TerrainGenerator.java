package com.blazingmc.world.generation;

import com.blazingmc.world.generation.noise.OctaveNoise;
import com.blazingmc.world.generation.structure.DungeonGenerator;
import com.blazingmc.world.generation.structure.VillageGenerator;
import org.bukkit.Material;

import java.util.Random;

public class TerrainGenerator {
    private static final int SEA_LEVEL = 63;
    private static final int BEDROCK_LEVEL = -64;
    private static final int HEIGHT_LIMIT = 320;
    private static final int CAVE_MIN_Y = -60;
    private static final int CAVE_MAX_Y = 256;
    
    private final OctaveNoise terrainNoise;
    private final OctaveNoise caveNoise;
    private final OctaveNoise caveNoise2;
    private final OctaveNoise biomeNoise;
    private final OctaveNoise oreNoise;
    private final DungeonGenerator dungeonGenerator;
    private final VillageGenerator villageGenerator;
    private final long seed;
    private final Random random;
    
    public TerrainGenerator(long seed) {
        this.seed = seed;
        this.terrainNoise = new OctaveNoise(seed, 6, 0.5, 2.0);
        this.caveNoise = new OctaveNoise(seed + 1, 4, 0.5, 2.0);
        this.caveNoise2 = new OctaveNoise(seed + 100, 4, 0.5, 2.0);
        this.biomeNoise = new OctaveNoise(seed + 2, 2, 0.5, 2.0);
        this.oreNoise = new OctaveNoise(seed + 3, 3, 0.5, 2.0);
        this.dungeonGenerator = new DungeonGenerator(seed);
        this.villageGenerator = new VillageGenerator(seed);
        this.random = new Random(seed);
    }
    
    public void generateChunk(int chunkX, int chunkZ, short[] blockIds, byte[] blockData) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkX * 16 + x;
                int worldZ = chunkZ * 16 + z;
                
                int height = getTerrainHeight(worldX, worldZ);
                Biome biome = getBiome(worldX, worldZ);
                
                for (int y = BEDROCK_LEVEL; y < HEIGHT_LIMIT; y++) {
                    int index = getIndex(x, y, z);
                    
                    Material material = generateBlock(worldX, y, worldZ, height, biome);
                    
                    if (material != null) {
                        blockIds[index] = (short) material.ordinal();
                    }
                }
                
                generateOres(chunkX, chunkZ, x, z, blockIds, height);
            }
        }
        
        carveCaves(chunkX, chunkZ, blockIds);
        generateLavaPools(chunkX, chunkZ, blockIds);
        
        dungeonGenerator.generate(chunkX, chunkZ, blockIds);
        
        int villageHeight = getTerrainHeight(chunkX * 16 + 8, chunkZ * 16 + 8);
        villageGenerator.generate(chunkX, chunkZ, blockIds, villageHeight);
    }
    
    private Material generateBlock(int worldX, int y, int worldZ, int height, Biome biome) {
        if (y == BEDROCK_LEVEL) {
            return Material.BEDROCK;
        }
        
        if (y < height - 4) {
            return getStoneVariant(worldX, y, worldZ);
        } else if (y < height) {
            if (biome == Biome.DESERT) {
                return y < height - 1 ? Material.SANDSTONE : Material.SAND;
            } else if (biome == Biome.OCEAN) {
                return y < height - 1 ? Material.GRAVEL : Material.GRASS_BLOCK;
            } else {
                return y < height - 1 ? Material.DIRT : Material.GRASS_BLOCK;
            }
        } else if (y == height) {
            if (biome == Biome.OCEAN && height <= SEA_LEVEL) {
                return Material.GRAVEL;
            }
            return biome.getSurfaceMaterial();
        } else if (y <= SEA_LEVEL) {
            return Material.WATER;
        } else {
            return Material.AIR;
        }
    }
    
    private Material getStoneVariant(int worldX, int y, int worldZ) {
        double noise = oreNoise.noise3D(worldX * 0.02, y * 0.02, worldZ * 0.02);
        
        if (noise > 0.6) {
            return Material.GRANITE;
        } else if (noise < -0.6) {
            return Material.DIORITE;
        } else if (noise > 0.3 && noise < 0.5) {
            return Material.ANDESITE;
        }
        
        return Material.STONE;
    }
    
    private void carveCaves(int chunkX, int chunkZ, short[] blockIds) {
        Random caveRng = new Random(seed ^ ((long) chunkX << 32) ^ chunkZ);
        int numCaves = 3 + caveRng.nextInt(4);
        
        for (int i = 0; i < numCaves; i++) {
            int startX = chunkX * 16 + caveRng.nextInt(16);
            int startZ = chunkZ * 16 + caveRng.nextInt(16);
            int startY = CAVE_MIN_Y + caveRng.nextInt(CAVE_MAX_Y - CAVE_MIN_Y);
            
            double length = 10 + caveRng.nextDouble() * 40;
            double direction = caveRng.nextDouble() * Math.PI * 2;
            double slope = (caveRng.nextDouble() - 0.5) * 0.3;
            
            for (int step = 0; step < length; step++) {
                int cx = (int) (startX + Math.cos(direction) * step);
                int cz = (int) (startZ + Math.sin(direction) * step);
                int cy = (int) (startY + Math.sin(slope) * step);
                
                double radius = 2.0 + caveNoise.noise3D(cx * 0.03, cy * 0.03, cz * 0.03) * 2.0;
                radius = Math.max(1.5, radius);
                
                carveSphere(blockIds, cx, cy, cz, radius, chunkX, chunkZ);
                
                direction += caveNoise2.noise3D(cx * 0.01, cy * 0.01, cz * 0.01) * 0.2;
            }
        }
        
        int numSpelunk = 1 + caveRng.nextInt(2);
        for (int i = 0; i < numSpelunk; i++) {
            int centerX = chunkX * 16 + 8;
            int centerZ = chunkZ * 16 + 8;
            int centerY = 0 + caveRng.nextInt(30);
            double radius = 5 + caveRng.nextDouble() * 8;
            
            carveSphere(blockIds, centerX, centerY, centerZ, radius, chunkX, chunkZ);
        }
    }
    
    private void carveSphere(short[] blockIds, int cx, int cy, int cz, double radius, int chunkX, int chunkZ) {
        int minX = Math.max(0, (int) (cx - radius - chunkX * 16));
        int maxX = Math.min(15, (int) (cx + radius - chunkX * 16));
        int minY = Math.max(BEDROCK_LEVEL + 1, (int) (cy - radius));
        int maxY = Math.min(HEIGHT_LIMIT - 1, (int) (cy + radius));
        int minZ = Math.max(0, (int) (cz - radius - chunkZ * 16));
        int maxZ = Math.min(15, (int) (cz + radius - chunkZ * 16));
        
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    double dx = (chunkX * 16 + x) - cx;
                    double dy = y - cy;
                    double dz = (chunkZ * 16 + z) - cz;
                    double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    
                    if (dist < radius) {
                        int index = getIndex(x, y, z);
                        if (index >= 0 && index < blockIds.length) {
                            Material current = Material.values()[blockIds[index]];
                            if (current != Material.BEDROCK && current != Material.WATER) {
                                if (y <= SEA_LEVEL && dist > radius * 0.7) {
                                    blockIds[index] = (short) Material.WATER.ordinal();
                                } else {
                                    blockIds[index] = (short) Material.AIR.ordinal();
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void generateOres(int chunkX, int chunkZ, int localX, int localZ, short[] blockIds, int surfaceHeight) {
        Random oreRng = new Random(seed ^ ((long) chunkX * 1000 + chunkZ) * 1000 + localX * 100 + localZ);
        
        placeVein(blockIds, chunkX, chunkZ, localX, localZ, Material.COAL_ORE, surfaceHeight - 5, surfaceHeight - 80, 20, 8, oreRng);
        placeVein(blockIds, chunkX, chunkZ, localX, localZ, Material.IRON_ORE, surfaceHeight - 10, surfaceHeight - 120, 12, 4, oreRng);
        placeVein(blockIds, chunkX, chunkZ, localX, localZ, Material.GOLD_ORE, surfaceHeight - 20, surfaceHeight - 160, 6, 2, oreRng);
        placeVein(blockIds, chunkX, chunkZ, localX, localZ, Material.DIAMOND_ORE, surfaceHeight - 30, surfaceHeight - 200, 3, 1, oreRng);
        placeVein(blockIds, chunkX, chunkZ, localX, localZ, Material.REDSTONE_ORE, surfaceHeight - 25, surfaceHeight - 180, 5, 2, oreRng);
        placeVein(blockIds, chunkX, chunkZ, localX, localZ, Material.EMERALD_ORE, surfaceHeight - 15, surfaceHeight - 250, 2, 1, oreRng);
        placeVein(blockIds, chunkX, chunkZ, localX, localZ, Material.LAPIS_ORE, surfaceHeight - 20, surfaceHeight - 170, 4, 1, oreRng);
        placeGravelPatches(blockIds, chunkX, chunkZ, localX, localZ, surfaceHeight, oreRng);
    }
    
    private void placeVein(short[] blockIds, int chunkX, int chunkZ, int localX, int localZ, 
                          Material ore, int maxY, int minY, int tries, int veinSize, Random rng) {
        int worldX = chunkX * 16 + localX;
        int worldZ = chunkZ * 16 + localZ;
        
        for (int i = 0; i < tries; i++) {
            int oreY = minY + rng.nextInt(Math.max(1, maxY - minY));
            double noise = oreNoise.noise3D(worldX * 0.1, oreY * 0.1, worldZ * 0.1);
            
            if (noise > 0.3) {
                for (int v = 0; v < veinSize; v++) {
                    int vx = localX + rng.nextInt(3) - 1;
                    int vy = oreY + rng.nextInt(3) - 1;
                    int vz = localZ + rng.nextInt(3) - 1;
                    
                    if (vx >= 0 && vx < 16 && vz >= 0 && vz < 16 && vy >= BEDROCK_LEVEL && vy < HEIGHT_LIMIT) {
                        int index = getIndex(vx, vy, vz);
                        if (index >= 0 && index < blockIds.length) {
                            Material current = Material.values()[blockIds[index]];
                            if (current == Material.STONE || current == Material.GRANITE || 
                                current == Material.DIORITE || current == Material.ANDESITE) {
                                blockIds[index] = (short) ore.ordinal();
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void placeGravelPatches(short[] blockIds, int chunkX, int chunkZ, int localX, int localZ, int surfaceHeight, Random rng) {
        double noise = oreNoise.noise3D((chunkX * 16 + localX) * 0.05, (surfaceHeight - 30) * 0.05, (chunkZ * 16 + localZ) * 0.05);
        if (noise > 0.7 && rng.nextInt(10) < 2) {
            for (int dy = -2; dy <= 2; dy++) {
                int y = surfaceHeight - 30 + dy;
                if (y >= BEDROCK_LEVEL && y < HEIGHT_LIMIT) {
                    int index = getIndex(localX, y, localZ);
                    if (index >= 0 && index < blockIds.length) {
                        Material current = Material.values()[blockIds[index]];
                        if (current == Material.STONE) {
                            blockIds[index] = (short) Material.GRAVEL.ordinal();
                        }
                    }
                }
            }
        }
    }
    
    private void generateLavaPools(int chunkX, int chunkZ, short[] blockIds) {
        Random lavaRng = new Random(seed ^ ((long) chunkX << 16) ^ chunkZ);
        
        if (lavaRng.nextInt(20) == 0) {
            int poolX = 4 + lavaRng.nextInt(8);
            int poolZ = 4 + lavaRng.nextInt(8);
            int poolY = BEDROCK_LEVEL + 5 + lavaRng.nextInt(20);
            int radius = 2 + lavaRng.nextInt(3);
            
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dz * dz <= radius * radius) {
                        int x = poolX + dx;
                        int z = poolZ + dz;
                        if (x >= 0 && x < 16 && z >= 0 && z < 16) {
                            int index = getIndex(x, poolY, z);
                            if (index >= 0 && index < blockIds.length) {
                                Material current = Material.values()[blockIds[index]];
                                if (current != Material.BEDROCK) {
                                    blockIds[index] = (short) Material.LAVA.ordinal();
                                }
                            }
                            
                            int wallIndex = getIndex(x, poolY + 1, z);
                            if (wallIndex >= 0 && wallIndex < blockIds.length) {
                                Material above = Material.values()[blockIds[wallIndex]];
                                if (above == Material.AIR) {
                                    blockIds[wallIndex] = (short) Material.OBSIDIAN.ordinal();
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    public int getTerrainHeight(int x, int z) {
        double baseHeight = terrainNoise.noise2D(x * 0.01, z * 0.01);
        int height = (int) ((baseHeight + 1) * 32) + 64;
        
        Biome biome = getBiome(x, z);
        if (biome == Biome.MOUNTAINS) {
            double mountainNoise = terrainNoise.noise2D(x * 0.005, z * 0.005);
            height += (int) (mountainNoise * 40);
        }
        
        height = Math.max(BEDROCK_LEVEL + 1, Math.min(HEIGHT_LIMIT - 1, height));
        
        return height;
    }
    
    public Biome getBiome(int x, int z) {
        double biomeValue = biomeNoise.noise2D(x * 0.005, z * 0.005);
        
        if (biomeValue < -0.3) {
            return Biome.DESERT;
        } else if (biomeValue < 0.0) {
            return Biome.PLAINS;
        } else if (biomeValue < 0.3) {
            return Biome.FOREST;
        } else if (biomeValue < 0.5) {
            return Biome.MOUNTAINS;
        } else {
            return Biome.OCEAN;
        }
    }
    
    private int getIndex(int x, int y, int z) {
        int adjustedY = y + 64;
        return (adjustedY << 8) | (z << 4) | x;
    }
    
    public enum Biome {
        PLAINS(Material.GRASS_BLOCK, Material.DIRT),
        FOREST(Material.GRASS_BLOCK, Material.DIRT),
        DESERT(Material.SAND, Material.SANDSTONE),
        MOUNTAINS(Material.STONE, Material.STONE),
        OCEAN(Material.GRAVEL, Material.SAND);
        
        private final Material surfaceMaterial;
        private final Material subsurfaceMaterial;
        
        Biome(Material surfaceMaterial, Material subsurfaceMaterial) {
            this.surfaceMaterial = surfaceMaterial;
            this.subsurfaceMaterial = subsurfaceMaterial;
        }
        
        public Material getSurfaceMaterial() { return surfaceMaterial; }
        public Material getSubsurfaceMaterial() { return subsurfaceMaterial; }
    }
}
