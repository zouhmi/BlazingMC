package org.bukkit;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MaterialTest {
    
    @Test
    void testMaterialEnumValues() {
        assertTrue(Material.values().length > 100, "Material enum should have many values");
    }
    
    @Test
    void testAirIsNotBlock() {
        assertFalse(Material.AIR.isBlock(), "AIR should not be a block");
    }
    
    @Test
    void testStoneIsBlock() {
        assertTrue(Material.STONE.isBlock(), "STONE should be a block");
    }
    
    @Test
    void testDirtIsBlock() {
        assertTrue(Material.DIRT.isBlock(), "DIRT should be a block");
    }
    
    @Test
    void testWaterIsBlock() {
        assertTrue(Material.WATER.isBlock(), "WATER should be a block");
    }
    
    @Test
    void testLavaIsBlock() {
        assertTrue(Material.LAVA.isBlock(), "LAVA should be a block");
    }
    
    @Test
    void testBedrockIsBlock() {
        assertTrue(Material.BEDROCK.isBlock(), "BEDROCK should be a block");
    }
    
    @Test
    void testDiamondOreIsBlock() {
        assertTrue(Material.DIAMOND_ORE.isBlock(), "DIAMOND_ORE should be a block");
    }
    
    @Test
    void testChestIsBlock() {
        assertTrue(Material.CHEST.isBlock(), "CHEST should be a block");
    }
    
    @Test
    void testToolsAreItems() {
        assertTrue(Material.DIAMOND_SWORD.isItem(), "DIAMOND_SWORD should be an item");
        assertTrue(Material.DIAMOND_PICKAXE.isItem(), "DIAMOND_PICKAXE should be an item");
        assertTrue(Material.DIAMOND_AXE.isItem(), "DIAMOND_AXE should be an item");
    }
    
    @Test
    void testArmorAreItems() {
        assertTrue(Material.LEATHER_HELMET.isItem(), "LEATHER_HELMET should be an item");
        assertTrue(Material.IRON_CHESTPLATE.isItem(), "IRON_CHESTPLATE should be an item");
        assertTrue(Material.DIAMOND_LEGGINGS.isItem(), "DIAMOND_LEGGINGS should be an item");
        assertTrue(Material.GOLDEN_BOOTS.isItem(), "GOLDEN_BOOTS should be an item");
    }
    
    @Test
    void testEdibleItems() {
        assertFalse(Material.COMPASS.isEdible(), "COMPASS should not be edible");
    }
    
    @Test
    void testMaterialOrdinalsAreSequential() {
        Material[] values = Material.values();
        for (int i = 0; i < values.length; i++) {
            assertEquals(i, values[i].ordinal(), "Ordinal should match index for " + values[i].name());
        }
    }
    
    @Test
    void testMaterialByName() {
        assertEquals(Material.STONE, Material.valueOf("STONE"));
        assertEquals(Material.AIR, Material.valueOf("AIR"));
        assertEquals(Material.DIAMOND_BLOCK, Material.valueOf("DIAMOND_BLOCK"));
    }
    
    @Test
    void testBlockItemsBeforeSpawner() {
        Material[] values = Material.values();
        int spawnerOrdinal = Material.SPAWNER.ordinal();
        
        for (Material mat : values) {
            if (mat.ordinal() < spawnerOrdinal && mat != Material.AIR && 
                mat != Material.WATER && mat != Material.LAVA) {
                assertTrue(mat.isBlock(), mat.name() + " should be a block");
            }
        }
    }
    
    @Test
    void testNewlyAddedMaterialsExist() {
        assertDoesNotThrow(() -> Material.valueOf("LAPIS_ORE"));
        assertDoesNotThrow(() -> Material.valueOf("OBSIDIAN"));
        assertDoesNotThrow(() -> Material.valueOf("ENDER_CHEST"));
        assertDoesNotThrow(() -> Material.valueOf("WALL_TORCH"));
        assertDoesNotThrow(() -> Material.valueOf("REDSTONE_LAMP"));
        assertDoesNotThrow(() -> Material.valueOf("ENCHANTING_TABLE"));
    }
}
