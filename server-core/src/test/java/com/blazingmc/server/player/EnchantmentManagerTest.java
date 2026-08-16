package com.blazingmc.server.player;

import com.blazingmc.server.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.Enchantment;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnchantmentManagerTest {
    @Test
    void swordOffersContainCompatibleEnchantments() {
        EnchantmentManager manager = new EnchantmentManager();
        List<EnchantmentManager.Offer> offers = manager.generateOffers(
            new ItemStack(Material.DIAMOND_SWORD), 15, 42L);

        assertEquals(3, offers.size());
        assertTrue(offers.stream().allMatch(offer ->
            offer.enchantment() == Enchantment.DAMAGE_ALL ||
            offer.enchantment() == Enchantment.KNOCKBACK ||
            offer.enchantment() == Enchantment.FIRE_ASPECT ||
            offer.enchantment() == Enchantment.LOOT_BONUS_MOBS ||
            offer.enchantment() == Enchantment.DURABILITY));
    }

    @Test
    void itemStackStoresAndClonesEnchantments() {
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 3);
        ItemStack clone = sword.clone();

        assertEquals(3, sword.getEnchantmentLevel(Enchantment.DAMAGE_ALL));
        assertEquals(3, clone.getEnchantmentLevel(Enchantment.DAMAGE_ALL));
        assertFalse(sword.getEnchantments().isEmpty());
        assertTrue(sword.isSimilar(clone));
    }
}
