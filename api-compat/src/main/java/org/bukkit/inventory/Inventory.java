package org.bukkit.inventory;

import org.bukkit.Material;

public interface Inventory {
    int getSize();
    int getMaxStackSize();
    void setMaxStackSize(int size);
    ItemStack getItem(int index);
    java.util.HashMap<Integer, ItemStack> addItem(ItemStack... items);
    java.util.HashMap<Integer, ItemStack> removeItem(ItemStack... items);
    ItemStack[] getContents();
    void setContents(ItemStack[] items);
    ItemStack[] getStorageContents();
    void setStorageContents(ItemStack[] items) throws IllegalArgumentException;
    boolean contains(Material material);
    boolean contains(Material material, int amount);
    boolean contains(ItemStack item);
    boolean contains(ItemStack item, int amount);
    boolean containsAtLeast(ItemStack item, int amount);
    java.util.HashMap<Integer, ? extends ItemStack> all(Material material);
    java.util.HashMap<Integer, ? extends ItemStack> all(ItemStack item);
    int first(Material material);
    int first(ItemStack item);
    int firstEmpty();
    boolean isEmpty();
    void remove(Material material);
    void remove(ItemStack item);
    void clear(int index);
    void clear();
    java.util.List<HumanEntity> getViewers();
    String getTitle();
    InventoryType getType();
}