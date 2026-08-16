package com.blazingmc.server.inventory;

import org.bukkit.Material;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InventoryTest {
    
    private Inventory inventory;
    
    @BeforeEach
    void setUp() {
        inventory = new Inventory(27, "Test Chest");
    }
    
    @Test
    void testInventoryCreation() {
        assertEquals(27, inventory.getSize());
        assertEquals("Test Chest", inventory.getTitle());
        assertEquals(Inventory.InventoryType.CHEST, inventory.getType());
    }
    
    @Test
    void testInventoryType() {
        Inventory playerInv = new Inventory(41, "Inventory", Inventory.InventoryType.PLAYER);
        assertEquals(Inventory.InventoryType.PLAYER, playerInv.getType());
        
        Inventory enderChest = new Inventory(27, "Ender Chest", Inventory.InventoryType.ENDER_CHEST);
        assertEquals(Inventory.InventoryType.ENDER_CHEST, enderChest.getType());
    }
    
    @Test
    void testGetItemEmptySlot() {
        ItemStack item = inventory.getItem(0);
        
        assertNotNull(item);
        assertTrue(item.isEmpty());
        assertEquals(Material.AIR, item.getType());
    }
    
    @Test
    void testGetItemOutOfBounds() {
        assertNull(inventory.getItem(-1));
        assertNull(inventory.getItem(27));
    }
    
    @Test
    void testSetItem() {
        ItemStack stone = new ItemStack(Material.STONE, 64);
        
        inventory.setItem(0, stone);
        
        assertEquals(Material.STONE, inventory.getItem(0).getType());
        assertEquals(64, inventory.getItem(0).getAmount());
    }
    
    @Test
    void testSetItemOutOfBounds() {
        ItemStack stone = new ItemStack(Material.STONE);
        
        assertDoesNotThrow(() -> inventory.setItem(-1, stone));
        assertDoesNotThrow(() -> inventory.setItem(27, stone));
    }
    
    @Test
    void testSetItemNull() {
        inventory.setItem(0, new ItemStack(Material.STONE));
        
        inventory.setItem(0, null);
        
        assertEquals(Material.AIR, inventory.getItem(0).getType());
    }
    
    @Test
    void testAddItem() {
        ItemStack stone = new ItemStack(Material.STONE, 32);
        
        ItemStack remaining = inventory.addItem(stone);
        
        assertNull(remaining);
        assertEquals(Material.STONE, inventory.getItem(0).getType());
        assertEquals(32, inventory.getItem(0).getAmount());
    }
    
    @Test
    void testAddItemFullStack() {
        ItemStack stone1 = new ItemStack(Material.STONE, 32);
        ItemStack stone2 = new ItemStack(Material.STONE, 32);
        
        inventory.addItem(stone1);
        ItemStack remaining = inventory.addItem(stone2);
        
        assertNull(remaining);
        assertEquals(Material.STONE, inventory.getItem(0).getType());
        assertEquals(64, inventory.getItem(0).getAmount());
    }
    
    @Test
    void testAddItemOverflows() {
        Inventory smallInv = new Inventory(1, "Small");
        
        ItemStack stone1 = new ItemStack(Material.STONE, 64);
        ItemStack stone2 = new ItemStack(Material.STONE, 32);
        
        smallInv.addItem(stone1);
        ItemStack remaining = smallInv.addItem(stone2);
        
        assertNotNull(remaining);
        assertEquals(32, remaining.getAmount());
    }
    
    @Test
    void testAddItemEmpty() {
        ItemStack empty = new ItemStack(Material.AIR);
        
        ItemStack remaining = inventory.addItem(empty);
        
        assertNull(remaining);
    }
    
    @Test
    void testAddItemNull() {
        ItemStack remaining = inventory.addItem(null);
        
        assertNull(remaining);
    }
    
    @Test
    void testRemoveItem() {
        inventory.setItem(0, new ItemStack(Material.STONE, 32));
        
        ItemStack remaining = inventory.removeItem(new ItemStack(Material.STONE, 16));
        
        assertNull(remaining);
        assertEquals(16, inventory.getItem(0).getAmount());
    }
    
    @Test
    void testRemoveItemCompletely() {
        inventory.setItem(0, new ItemStack(Material.STONE, 32));
        
        ItemStack remaining = inventory.removeItem(new ItemStack(Material.STONE, 32));
        
        assertNull(remaining);
        assertEquals(Material.AIR, inventory.getItem(0).getType());
    }
    
    @Test
    void testRemoveItemMoreThanExists() {
        inventory.setItem(0, new ItemStack(Material.STONE, 32));
        
        ItemStack remaining = inventory.removeItem(new ItemStack(Material.STONE, 64));
        
        assertNotNull(remaining);
        assertEquals(32, remaining.getAmount());
    }
    
    @Test
    void testContains() {
        assertFalse(inventory.contains(Material.STONE));
        
        inventory.setItem(0, new ItemStack(Material.STONE, 32));
        
        assertTrue(inventory.contains(Material.STONE));
        assertFalse(inventory.contains(Material.DIAMOND_BLOCK));
    }
    
    @Test
    void testContainsAmount() {
        inventory.setItem(0, new ItemStack(Material.STONE, 32));
        
        assertTrue(inventory.contains(Material.STONE, 32));
        assertFalse(inventory.contains(Material.STONE, 64));
    }
    
    @Test
    void testCount() {
        assertEquals(0, inventory.count(Material.STONE));
        
        inventory.setItem(0, new ItemStack(Material.STONE, 32));
        inventory.setItem(1, new ItemStack(Material.STONE, 16));
        
        assertEquals(48, inventory.count(Material.STONE));
    }
    
    @Test
    void testClear() {
        inventory.setItem(0, new ItemStack(Material.STONE, 32));
        inventory.setItem(1, new ItemStack(Material.DIAMOND_BLOCK, 5));
        
        inventory.clear();
        
        assertEquals(Material.AIR, inventory.getItem(0).getType());
        assertEquals(Material.AIR, inventory.getItem(1).getType());
    }
    
    @Test
    void testClearSlot() {
        inventory.setItem(0, new ItemStack(Material.STONE, 32));
        
        inventory.clear(0);
        
        assertEquals(Material.AIR, inventory.getItem(0).getType());
    }
    
    @Test
    void testClearSlotOutOfBounds() {
        assertDoesNotThrow(() -> inventory.clear(-1));
        assertDoesNotThrow(() -> inventory.clear(27));
    }
    
    @Test
    void testFirstEmpty() {
        assertEquals(0, inventory.firstEmpty());
        
        inventory.setItem(0, new ItemStack(Material.STONE));
        assertEquals(1, inventory.firstEmpty());
        
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, new ItemStack(Material.STONE));
        }
        assertEquals(-1, inventory.firstEmpty());
    }
    
    @Test
    void testIsFull() {
        assertFalse(inventory.isFull());
        
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, new ItemStack(Material.STONE));
        }
        
        assertTrue(inventory.isFull());
    }
    
    @Test
    void testGetContents() {
        inventory.setItem(0, new ItemStack(Material.STONE));
        
        ItemStack[] contents = inventory.getContents();
        assertEquals(27, contents.length);
        assertEquals(Material.STONE, contents[0].getType());
    }
    
    @Test
    void testAddItemAll() {
        ItemStack stone = new ItemStack(Material.STONE, 32);
        ItemStack diamond = new ItemStack(Material.DIAMOND_BLOCK, 5);
        
        java.util.Map<Integer, ItemStack> leftover = inventory.addItemAll(stone, diamond);
        
        assertTrue(leftover.isEmpty());
        assertTrue(inventory.contains(Material.STONE));
        assertTrue(inventory.contains(Material.DIAMOND_BLOCK));
    }
    
    @Test
    void testItemStackClone() {
        ItemStack original = new ItemStack(Material.DIAMOND_BLOCK, 32);
        original.setDisplayName("Test");
        
        ItemStack clone = original.clone();
        
        assertEquals(original.getType(), clone.getType());
        assertEquals(original.getAmount(), clone.getAmount());
        assertEquals(original.getDisplayName(), clone.getDisplayName());
        
        clone.setAmount(16);
        assertEquals(32, original.getAmount());
    }
    
    @Test
    void testItemStackSimilar() {
        ItemStack stone1 = new ItemStack(Material.STONE, 32);
        ItemStack stone2 = new ItemStack(Material.STONE, 16);
        ItemStack diamond = new ItemStack(Material.DIAMOND_BLOCK, 32);
        
        assertTrue(stone1.isSimilar(stone2));
        assertFalse(stone1.isSimilar(diamond));
        assertFalse(stone1.isSimilar(null));
    }
    
    @Test
    void testItemStackAmountLimits() {
        ItemStack stone = new ItemStack(Material.STONE);
        
        stone.setAmount(100);
        assertEquals(64, stone.getAmount());
        
        stone.setAmount(-5);
        assertEquals(0, stone.getAmount());
    }
    
    @Test
    void testItemStackGrowShrink() {
        ItemStack stone = new ItemStack(Material.STONE, 32);
        
        stone.grow(10);
        assertEquals(42, stone.getAmount());
        
        stone.shrink(5);
        assertEquals(37, stone.getAmount());
    }
    
    @Test
    void testItemStackHasEnough() {
        ItemStack stone = new ItemStack(Material.STONE, 32);
        
        assertTrue(stone.hasEnough(32));
        assertTrue(stone.hasEnough(16));
        assertFalse(stone.hasEnough(64));
    }
    
    @Test
    void testItemStackDisplayAndLore() {
        ItemStack stone = new ItemStack(Material.STONE);
        
        stone.setDisplayName("My Stone");
        assertEquals("My Stone", stone.getDisplayName());
        
        String[] lore = {"Line 1", "Line 2"};
        stone.setLore(lore);
        assertArrayEquals(lore, stone.getLore());
    }
    
    @Test
    void testItemStackUnbreakable() {
        ItemStack stone = new ItemStack(Material.STONE);
        
        assertFalse(stone.isUnbreakable());
        
        stone.setUnbreakable(true);
        assertTrue(stone.isUnbreakable());
    }
    
    @Test
    void testItemStackRepairCost() {
        ItemStack stone = new ItemStack(Material.STONE);
        
        assertEquals(0, stone.getRepairCost());
        
        stone.setRepairCost(5);
        assertEquals(5, stone.getRepairCost());
    }
    
    @Test
    void testItemStackDurability() {
        ItemStack stone = new ItemStack(Material.STONE);
        
        assertEquals(0, stone.getDurability());
        
        stone.setDurability((short) 100);
        assertEquals(100, stone.getDurability());
    }
}
