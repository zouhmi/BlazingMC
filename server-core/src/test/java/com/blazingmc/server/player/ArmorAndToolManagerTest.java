package com.blazingmc.server.player;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArmorAndToolManagerTest {
    @Test
    void armorReductionReturnsDamageAfterProtection() {
        assertEquals(10.0, ArmorManager.calculateDamageReduction(10.0, 0, 0), 0.0001);
        assertTrue(ArmorManager.applyArmorProtection(10.0, Material.DIAMOND_HELMET,
            Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS) < 10.0);
        assertEquals(10.0, ArmorManager.applyArmorProtection(10.0, null, null, null, null), 0.0001);
    }

    @Test
    void toolClassificationOnlyRecognizesSupportedTools() {
        assertTrue(ToolDurabilityManager.isTool(Material.DIAMOND_PICKAXE));
        assertTrue(ToolDurabilityManager.isPickaxe(Material.DIAMOND_PICKAXE));
        assertFalse(ToolDurabilityManager.isTool(Material.BOW));
        assertFalse(ToolDurabilityManager.isTool(null));
    }

    @Test
    void harvestingUsesToolTierForObsidian() {
        assertFalse(ToolDurabilityManager.canHarvestBlock(Material.IRON_PICKAXE, Material.OBSIDIAN));
        assertTrue(ToolDurabilityManager.canHarvestBlock(Material.DIAMOND_PICKAXE, Material.OBSIDIAN));
        assertTrue(ToolDurabilityManager.calculateMiningTime(Material.IRON_PICKAXE,
            Material.IRON_ORE, false) > 0);
        assertEquals(-1, ToolDurabilityManager.calculateMiningTime(null, Material.STONE, false));
    }
}
