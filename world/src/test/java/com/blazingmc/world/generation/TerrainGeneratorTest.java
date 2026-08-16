package com.blazingmc.world.generation;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TerrainGeneratorTest {
    
    @Test
    void testTerrainGeneratorCreation() {
        TerrainGenerator gen = new TerrainGenerator(12345L);
        assertNotNull(gen);
    }
    
    @Test
    void testTerrainHeight() {
        TerrainGenerator gen = new TerrainGenerator(12345L);
        
        int height = gen.getTerrainHeight(0, 0);
        assertTrue(height >= -64 && height <= 319, "Height should be in valid range: " + height);
    }
    
    @Test
    void testTerrainHeightConsistency() {
        TerrainGenerator gen = new TerrainGenerator(12345L);
        
        int height1 = gen.getTerrainHeight(10, 10);
        int height2 = gen.getTerrainHeight(10, 10);
        assertEquals(height1, height2, "Same coordinates should give same height");
    }
    
    @Test
    void testTerrainHeightVariation() {
        TerrainGenerator gen = new TerrainGenerator(12345L);
        
        int height1 = gen.getTerrainHeight(0, 0);
        int height2 = gen.getTerrainHeight(100, 100);
        
        assertNotEquals(height1, height2, "Different coordinates should give different heights");
    }
    
    @Test
    void testGenerateChunk() {
        TerrainGenerator gen = new TerrainGenerator(12345L);
        short[] blockIds = new short[16 * 384 * 16];
        byte[] blockData = new byte[16 * 384 * 16];
        
        gen.generateChunk(0, 0, blockIds, blockData);
        
        boolean hasStone = false;
        boolean hasAir = false;
        boolean hasGrass = false;
        boolean hasWater = false;
        
        for (short id : blockIds) {
            Material mat = Material.values()[id];
            if (mat == Material.STONE) hasStone = true;
            if (mat == Material.AIR) hasAir = true;
            if (mat == Material.GRASS_BLOCK) hasGrass = true;
            if (mat == Material.WATER) hasWater = true;
        }
        
        assertTrue(hasStone, "Generated chunk should contain stone");
        assertTrue(hasAir, "Generated chunk should contain air");
    }
    
    @Test
    void testGenerateChunkHasBedrock() {
        TerrainGenerator gen = new TerrainGenerator(12345L);
        short[] blockIds = new short[16 * 384 * 16];
        byte[] blockData = new byte[16 * 384 * 16];
        
        gen.generateChunk(0, 0, blockIds, blockData);
        
        boolean hasBedrock = false;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int index = (0 << 8) | (z << 4) | x;
                if (Material.values()[blockIds[index]] == Material.BEDROCK) {
                    hasBedrock = true;
                    break;
                }
            }
            if (hasBedrock) break;
        }
        
        assertTrue(hasBedrock, "Generated chunk should contain bedrock at y=-64");
    }
    
    @Test
    void testGetBiome() {
        TerrainGenerator gen = new TerrainGenerator(12345L);
        
        TerrainGenerator.Biome biome = gen.getBiome(0, 0);
        assertNotNull(biome);
    }
    
    @Test
    void testBiomeValues() {
        TerrainGenerator.Biome[] biomes = TerrainGenerator.Biome.values();
        assertEquals(5, biomes.length, "Should have 5 biomes");
        
        for (TerrainGenerator.Biome biome : biomes) {
            assertNotNull(biome.getSurfaceMaterial());
            assertNotNull(biome.getSubsurfaceMaterial());
        }
    }
    
    @Test
    void testGenerateMultipleChunks() {
        TerrainGenerator gen = new TerrainGenerator(12345L);
        
        short[] blockIds1 = new short[16 * 384 * 16];
        byte[] blockData1 = new byte[16 * 384 * 16];
        short[] blockIds2 = new short[16 * 384 * 16];
        byte[] blockData2 = new byte[16 * 384 * 16];
        
        gen.generateChunk(0, 0, blockIds1, blockData1);
        gen.generateChunk(1, 0, blockIds2, blockData2);
        
        boolean different = false;
        for (int i = 0; i < blockIds1.length; i++) {
            if (blockIds1[i] != blockIds2[i]) {
                different = true;
                break;
            }
        }
        
        assertTrue(different, "Different chunks should have different content");
    }
}
