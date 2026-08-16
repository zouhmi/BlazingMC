package com.blazingmc.server.inventory;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FurnaceManagerTest {
    @Test
    void recipesAndFuelValuesAreDefined() {
        FurnaceManager manager = new FurnaceManager();

        assertEquals(Material.IRON_INGOT, manager.getSmeltingResult(Material.IRON_ORE));
        assertEquals(Material.GLASS, manager.getSmeltingResult(Material.SAND));
        assertNull(manager.getSmeltingResult(Material.STONE));
        assertEquals(1600, manager.getFuelTime(Material.COAL));
        assertEquals(0, manager.getFuelTime(Material.STONE));
    }

    @Test
    void furnaceConsumesFuelAndProducesOutput() {
        FurnaceManager manager = new FurnaceManager();
        FurnaceManager.FurnaceState furnace = manager.getOrCreate(1, 64, 1);
        furnace.inventory().setItem(0, new ItemStack(Material.SAND));
        furnace.inventory().setItem(1, new ItemStack(Material.COAL));

        for (int tick = 0; tick < manager.getCookTimeTicks(); tick++) {
            manager.tick();
        }

        assertEquals(Material.GLASS, furnace.inventory().getItem(2).getType());
        assertEquals(1, furnace.inventory().getItem(1).getAmount());
        assertEquals(Material.AIR, furnace.inventory().getItem(0).getType());
    }
}
