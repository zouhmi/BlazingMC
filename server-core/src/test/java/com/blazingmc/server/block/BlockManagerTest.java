package com.blazingmc.server.block;

import org.bukkit.Material;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BlockManagerTest {
    
    private BlockManager blockManager;
    
    @BeforeEach
    void setUp() {
        blockManager = new BlockManager();
    }
    
    @Test
    void testBlockHardnessStone() {
        assertEquals(1.5f, blockManager.getBlockHardness(Material.STONE));
    }
    
    @Test
    void testBlockHardnessDiamondOre() {
        assertEquals(3.0f, blockManager.getBlockHardness(Material.DIAMOND_ORE));
    }
    
    @Test
    void testBlockHardnessObsidian() {
        assertEquals(50.0f, blockManager.getBlockHardness(Material.OBSIDIAN));
    }
    
    @Test
    void testBlockHardnessBedrock() {
        assertEquals(-1.0f, blockManager.getBlockHardness(Material.BEDROCK));
    }
    
    @Test
    void testBlockHardnessUnknown() {
        assertEquals(1.0f, blockManager.getBlockHardness(Material.COMPASS));
    }
    
    @Test
    void testBlockHardnessLeaves() {
        assertEquals(0.2f, blockManager.getBlockHardness(Material.OAK_LEAVES));
    }
    
    @Test
    void testBlockHardnessGlass() {
        assertEquals(0.3f, blockManager.getBlockHardness(Material.GLASS));
    }
    
    @Test
    void testBlockHardnessDirt() {
        assertEquals(0.5f, blockManager.getBlockHardness(Material.DIRT));
    }
    
    @Test
    void testBlockHardnessCobblestone() {
        assertEquals(2.0f, blockManager.getBlockHardness(Material.COBBLESTONE));
    }
    
    @Test
    void testBlockHardnessAnvil() {
        assertEquals(5.0f, blockManager.getBlockHardness(Material.ANVIL));
    }
    
    @Test
    void testBlockHardnessBeacon() {
        assertEquals(3.0f, blockManager.getBlockHardness(Material.BEACON));
    }
    
    @Test
    void testBlockHardnessHopper() {
        assertEquals(2.0f, blockManager.getBlockHardness(Material.HOPPER));
    }
    
    @Test
    void testBlockHardnessSpawner() {
        assertEquals(5.0f, blockManager.getBlockHardness(Material.SPAWNER));
    }
    
    @Test
    void testIsBreakableStone() {
        assertTrue(blockManager.isBreakable(Material.STONE));
    }
    
    @Test
    void testIsBreakableBedrock() {
        assertFalse(blockManager.isBreakable(Material.BEDROCK));
    }
    
    @Test
    void testIsBreakableCommandBlock() {
        assertFalse(blockManager.isBreakable(Material.COMMAND_BLOCK));
    }
    
    @Test
    void testIsBreakableEndPortalFrame() {
        assertFalse(blockManager.isBreakable(Material.END_PORTAL_FRAME));
    }
    
    @Test
    void testIsBreakableStructureBlock() {
        assertFalse(blockManager.isBreakable(Material.STRUCTURE_BLOCK));
    }
    
    @Test
    void testIsBreakableBarrier() {
        assertFalse(blockManager.isBreakable(Material.BARRIER));
    }
    
    @Test
    void testIsBreakableDiamondOre() {
        assertTrue(blockManager.isBreakable(Material.DIAMOND_ORE));
    }
    
    @Test
    void testIsBreakableChest() {
        assertTrue(blockManager.isBreakable(Material.CHEST));
    }
    
    @Test
    void testCleanup() {
        assertDoesNotThrow(() -> blockManager.cleanup());
    }
}
