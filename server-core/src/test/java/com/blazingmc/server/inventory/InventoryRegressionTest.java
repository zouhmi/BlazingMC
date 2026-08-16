package com.blazingmc.server.inventory;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InventoryRegressionTest {
    @Test
    void toolsDoNotStack() {
        Inventory inventory = new Inventory(2, "Inventory");
        inventory.addItem(new ItemStack(Material.DIAMOND_PICKAXE));
        ItemStack remaining = inventory.addItem(new ItemStack(Material.DIAMOND_PICKAXE));

        assertEquals(Material.DIAMOND_PICKAXE, inventory.getItem(0).getType());
        assertEquals(Material.DIAMOND_PICKAXE, inventory.getItem(1).getType());
        assertEquals(null, remaining);
    }

    @Test
    void metadataMakesStacksDifferent() {
        ItemStack named = new ItemStack(Material.STONE);
        named.setDisplayName("Named");
        ItemStack plain = new ItemStack(Material.STONE);

        org.junit.jupiter.api.Assertions.assertFalse(named.isSimilar(plain));
    }
}
