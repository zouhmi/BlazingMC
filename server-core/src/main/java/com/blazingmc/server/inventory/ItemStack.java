package com.blazingmc.server.inventory;

import com.blazingmc.chat.ConsoleLogger;
import org.bukkit.Material;
import org.bukkit.inventory.Enchantment;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ItemStack {
    private Material material;
    private int amount;
    private short durability;
    private String displayName;
    private String[] lore;
    private boolean unbreakable;
    private int repairCost;
    private final Map<Enchantment, Integer> enchantments;
    
    public ItemStack(Material material) {
        this(material, 1);
    }
    
    public ItemStack(Material material, int amount) {
        this(material, amount, (short) 0);
    }
    
    public ItemStack(Material material, int amount, short durability) {
        this.material = material != null ? material : Material.AIR;
        this.amount = Math.max(0, Math.min(64, amount));
        this.durability = (short) Math.max(0, durability);
        this.displayName = null;
        this.lore = null;
        this.unbreakable = false;
        this.repairCost = 0;
        this.enchantments = new HashMap<>();
    }
    
    public ItemStack clone() {
        ItemStack clone = new ItemStack(material, amount, durability);
        clone.displayName = displayName;
        clone.lore = lore != null ? lore.clone() : null;
        clone.unbreakable = unbreakable;
        clone.repairCost = repairCost;
        clone.enchantments.putAll(enchantments);
        return clone;
    }
    
    public boolean isSimilar(ItemStack other) {
        if (other == null) return false;
        return material == other.material && durability == other.durability &&
               java.util.Objects.equals(displayName, other.displayName) &&
               java.util.Arrays.equals(lore, other.lore) &&
               unbreakable == other.unbreakable && repairCost == other.repairCost &&
               enchantments.equals(other.enchantments);
    }
    
    public void addEnchantment(Enchantment enchantment, int level) {
        if (enchantment == null || level < enchantment.getStartLevel() || level > enchantment.getMaxLevel()) {
            throw new IllegalArgumentException("Invalid enchantment");
        }
        enchantments.put(enchantment, level);
    }

    public int getEnchantmentLevel(Enchantment enchantment) {
        return enchantments.getOrDefault(enchantment, 0);
    }

    public Map<Enchantment, Integer> getEnchantments() {
        return Collections.unmodifiableMap(enchantments);
    }

    public boolean isEmpty() {
        return material == Material.AIR || amount <= 0;
    }
    
    public void setAmount(int amount) {
        this.amount = Math.max(0, Math.min(64, amount));
    }
    
    public void addAmount(int amount) {
        setAmount(this.amount + amount);
    }
    
    public void removeAmount(int amount) {
        setAmount(this.amount - amount);
    }
    
    public boolean hasEnough(int amount) {
        return this.amount >= amount;
    }
    
    public void shrink(int amount) {
        removeAmount(amount);
    }
    
    public void grow(int amount) {
        addAmount(amount);
    }
    
    public Material getType() { return material; }
    public void setType(Material material) { this.material = material != null ? material : Material.AIR; }
    public int getAmount() { return amount; }
    public short getDurability() { return durability; }
    public void setDurability(short durability) { this.durability = (short) Math.max(0, durability); }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String[] getLore() { return lore != null ? lore.clone() : null; }
    public void setLore(String[] lore) { this.lore = lore != null ? lore.clone() : null; }
    public boolean isUnbreakable() { return unbreakable; }
    public void setUnbreakable(boolean unbreakable) { this.unbreakable = unbreakable; }
    public int getRepairCost() { return repairCost; }
    public void setRepairCost(int repairCost) { this.repairCost = repairCost; }
}