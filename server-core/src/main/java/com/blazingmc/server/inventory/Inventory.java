package com.blazingmc.server.inventory;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class Inventory {
    private final int size;
    private final ItemStack[] contents;
    private final String title;
    private final InventoryType type;
    
    public Inventory(int size, String title) {
        this(size, title, InventoryType.CHEST);
    }
    
    public Inventory(int size, String title, InventoryType type) {
        this.size = size;
        this.contents = new ItemStack[size];
        this.title = title;
        this.type = type;
        
        for (int i = 0; i < size; i++) {
            contents[i] = new ItemStack(Material.AIR);
        }
    }
    
    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= size) {
            return null;
        }
        return contents[slot];
    }
    
    public void setItem(int slot, ItemStack item) {
        if (slot < 0 || slot >= size) {
            return;
        }
        contents[slot] = item != null ? item : new ItemStack(Material.AIR);
    }
    
    public ItemStack addItem(ItemStack item) {
        if (item == null || item.isEmpty()) {
            return null;
        }
        
        ItemStack remaining = item.clone();
        
        for (int i = 0; i < size && remaining.getAmount() > 0; i++) {
            ItemStack current = contents[i];
            
            if (current.isEmpty()) {
                contents[i] = remaining.clone();
                remaining.setAmount(0);
            } else if (current.isSimilar(remaining)) {
                int maxStack = getMaxStackSize(current.getType());
                int canAdd = maxStack - current.getAmount();
                
                if (canAdd > 0) {
                    int toAdd = Math.min(canAdd, remaining.getAmount());
                    current.addAmount(toAdd);
                    remaining.removeAmount(toAdd);
                }
            }
        }
        
        return remaining.isEmpty() ? null : remaining;
    }
    
    public ItemStack removeItem(ItemStack item) {
        if (item == null || item.isEmpty()) {
            return null;
        }
        
        ItemStack remaining = item.clone();
        
        for (int i = 0; i < size && remaining.getAmount() > 0; i++) {
            ItemStack current = contents[i];
            
            if (current.isSimilar(remaining)) {
                int toRemove = Math.min(current.getAmount(), remaining.getAmount());
                current.removeAmount(toRemove);
                remaining.removeAmount(toRemove);
                
                if (current.isEmpty()) {
                    contents[i] = new ItemStack(Material.AIR);
                }
            }
        }
        
        return remaining.isEmpty() ? null : remaining;
    }
    
    public boolean contains(Material material) {
        for (ItemStack item : contents) {
            if (!item.isEmpty() && item.getType() == material) {
                return true;
            }
        }
        return false;
    }
    
    public boolean contains(Material material, int amount) {
        int count = 0;
        for (ItemStack item : contents) {
            if (!item.isEmpty() && item.getType() == material) {
                count += item.getAmount();
                if (count >= amount) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public int count(Material material) {
        int count = 0;
        for (ItemStack item : contents) {
            if (!item.isEmpty() && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }
    
    public Map<Integer, ItemStack> addItemAll(ItemStack... items) {
        Map<Integer, ItemStack> leftover = new HashMap<>();
        
        for (int i = 0; i < items.length; i++) {
            ItemStack remaining = addItem(items[i]);
            if (remaining != null) {
                leftover.put(i, remaining);
            }
        }
        
        return leftover;
    }
    
    public void clear() {
        for (int i = 0; i < size; i++) {
            contents[i] = new ItemStack(Material.AIR);
        }
    }
    
    public void clear(int slot) {
        if (slot >= 0 && slot < size) {
            contents[slot] = new ItemStack(Material.AIR);
        }
    }
    
    public int getSize() {
        return size;
    }
    
    public String getTitle() {
        return title;
    }
    
    public InventoryType getType() {
        return type;
    }
    
    public ItemStack[] getContents() {
        return contents.clone();
    }
    
    public int firstEmpty() {
        for (int i = 0; i < size; i++) {
            if (contents[i].isEmpty()) {
                return i;
            }
        }
        return -1;
    }
    
    public boolean isFull() {
        return firstEmpty() == -1;
    }
    
    private int getMaxStackSize(Material material) {
        if (material == null) {
            return 1;
        }
        
        String name = material.name();
        if (name.endsWith("_SWORD") || name.endsWith("_PICKAXE") ||
            name.endsWith("_AXE") || name.endsWith("_SHOVEL") ||
            name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") ||
            name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS")) {
            return 1;
        }
        
        return switch (material) {
            case SHIELD, BOW, CROSSBOW, TRIDENT, FLINT_AND_STEEL, SHEARS,
                 BUCKET, WATER_BUCKET, LAVA_BUCKET, MILK_BUCKET, MINECART,
                 SADDLE, NAME_TAG, LEAD, HORSE_ARMOR, IRON_HORSE_ARMOR,
                 GOLDEN_HORSE_ARMOR, DIAMOND_HORSE_ARMOR, NETHERITE_HORSE_ARMOR -> 1;
            default -> 64;
        };
    }
    
    public enum InventoryType {
        CHEST,
        PLAYER,
        ENDER_CHEST,
        ANVIL,
        BEACON,
        BLAST_FURNACE,
        BREWING_STAND,
        CRAFTING,
        DISPENSER,
        ENCHANTING,
        FURNACE,
        GRINDSTONE,
        HOPPER,
        LOOM,
        MERCHANT,
        SHULKER_BOX,
        SMOKER,
        STONECUTTER
    }
}