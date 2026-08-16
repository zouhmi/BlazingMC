package com.blazingmc.world.chunk;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChunkTest {
    
    @Test
    void testChunkCreation() {
        Chunk chunk = new Chunk(0, 0);
        
        assertEquals(0, chunk.getX());
        assertEquals(0, chunk.getZ());
        assertFalse(chunk.isDirty());
        assertFalse(chunk.isGenerated());
    }
    
    @Test
    void testChunkCreationNegativeCoords() {
        Chunk chunk = new Chunk(-5, -10);
        
        assertEquals(-5, chunk.getX());
        assertEquals(-10, chunk.getZ());
    }
    
    @Test
    void testSetAndGetBlock() {
        Chunk chunk = new Chunk(0, 0);
        
        chunk.setBlock(5, 64, 5, Material.STONE);
        
        assertEquals(Material.STONE, chunk.getBlock(5, 64, 5));
    }
    
    @Test
    void testSetAndGetBlockBoundary() {
        Chunk chunk = new Chunk(0, 0);
        
        chunk.setBlock(0, 0, 0, Material.DIAMOND_BLOCK);
        assertEquals(Material.DIAMOND_BLOCK, chunk.getBlock(0, 0, 0));
        
        chunk.setBlock(15, 319, 15, Material.DIAMOND_BLOCK);
        assertEquals(Material.DIAMOND_BLOCK, chunk.getBlock(15, 319, 15));
        
        chunk.setBlock(0, -64, 0, Material.BEDROCK);
        assertEquals(Material.BEDROCK, chunk.getBlock(0, -64, 0));
    }
    
    @Test
    void testGetBlockOutOfBounds() {
        Chunk chunk = new Chunk(0, 0);
        
        assertEquals(Material.AIR, chunk.getBlock(-1, 0, 0));
        assertEquals(Material.AIR, chunk.getBlock(16, 0, 0));
        assertEquals(Material.AIR, chunk.getBlock(0, -65, 0));
        assertEquals(Material.AIR, chunk.getBlock(0, 320, 0));
    }
    
    @Test
    void testSetBlockOutOfBoundsDoesNotThrow() {
        Chunk chunk = new Chunk(0, 0);
        
        assertDoesNotThrow(() -> chunk.setBlock(-1, 0, 0, Material.STONE));
        assertDoesNotThrow(() -> chunk.setBlock(16, 0, 0, Material.STONE));
        assertDoesNotThrow(() -> chunk.setBlock(0, -65, 0, Material.STONE));
        assertDoesNotThrow(() -> chunk.setBlock(0, 320, 0, Material.STONE));
    }
    
    @Test
    void testBlockData() {
        Chunk chunk = new Chunk(0, 0);
        
        assertEquals(0, chunk.getBlockData(5, 64, 5));
        
        chunk.setBlockData(5, 64, 5, (byte) 7);
        assertEquals(7, chunk.getBlockData(5, 64, 5));
    }
    
    @Test
    void testBlockDataOutOfBounds() {
        Chunk chunk = new Chunk(0, 0);
        
        assertEquals(0, chunk.getBlockData(-1, 0, 0));
        
        assertDoesNotThrow(() -> chunk.setBlockData(-1, 0, 0, (byte) 5));
    }
    
    @Test
    void testHeightmap() {
        Chunk chunk = new Chunk(0, 0);
        
        assertEquals(0, chunk.getHeightmap(5, 5));
        
        chunk.setHeightmap(5, 5, 72);
        assertEquals(72, chunk.getHeightmap(5, 5));
    }
    
    @Test
    void testHeightmapOutOfBounds() {
        Chunk chunk = new Chunk(0, 0);
        
        assertEquals(0, chunk.getHeightmap(-1, 0));
        assertEquals(0, chunk.getHeightmap(16, 0));
        assertEquals(0, chunk.getHeightmap(0, -1));
        assertEquals(0, chunk.getHeightmap(0, 16));
    }
    
    @Test
    void testGetHighestBlockY() {
        Chunk chunk = new Chunk(0, 0);
        
        chunk.setBlock(5, 72, 5, Material.STONE);
        chunk.setHeightmap(5, 5, 72);
        
        assertEquals(72, chunk.getHighestBlockY(5, 5));
    }
    
    @Test
    void testDirtyFlag() {
        Chunk chunk = new Chunk(0, 0);
        
        assertFalse(chunk.isDirty());
        
        chunk.setBlock(0, 0, 0, Material.STONE);
        assertTrue(chunk.isDirty());
        
        chunk.setDirty(false);
        assertFalse(chunk.isDirty());
    }
    
    @Test
    void testGeneratedFlag() {
        Chunk chunk = new Chunk(0, 0);
        
        assertFalse(chunk.isGenerated());
        
        chunk.setGenerated(true);
        assertTrue(chunk.isGenerated());
    }
    
    @Test
    void testSectionData() {
        Chunk chunk = new Chunk(0, 0);
        
        chunk.setBlock(5, 64, 5, Material.STONE);
        chunk.setBlockData(5, 64, 5, (byte) 3);
        
        Chunk.SectionData data = chunk.getSectionData(5, 64, 5);
        assertNotNull(data);
        assertEquals(Material.STONE.ordinal(), data.blockId());
        assertEquals(3, data.data());
    }
    
    @Test
    void testSectionDataOutOfBounds() {
        Chunk chunk = new Chunk(0, 0);
        
        assertNull(chunk.getSectionData(-1, 0, 0));
    }
    
    @Test
    void testBlockIdsArray() {
        Chunk chunk = new Chunk(0, 0);
        
        short[] blockIds = chunk.getBlockIds();
        assertNotNull(blockIds);
        assertEquals(Chunk.WIDTH * Chunk.HEIGHT * Chunk.WIDTH, blockIds.length);
    }
    
    @Test
    void testBlockDataArray() {
        Chunk chunk = new Chunk(0, 0);
        
        byte[] blockData = chunk.getBlockData();
        assertNotNull(blockData);
        assertEquals(Chunk.WIDTH * Chunk.HEIGHT * Chunk.WIDTH, blockData.length);
    }
    
    @Test
    void testBlockTypeAliases() {
        Chunk chunk = new Chunk(0, 0);
        
        chunk.setBlockType(5, 64, 5, Material.DIAMOND_BLOCK);
        assertEquals(Material.DIAMOND_BLOCK, chunk.getBlockType(5, 64, 5));
    }
    
    @Test
    void testChunkConstants() {
        assertEquals(16, Chunk.WIDTH);
        assertEquals(384, Chunk.HEIGHT);
        assertEquals(16, Chunk.SECTION_HEIGHT);
        assertEquals(24, Chunk.SECTIONS);
    }
}
