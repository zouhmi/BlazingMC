package org.bukkit.inventory;

import org.bukkit.Material;

public class ItemStack {
    private Material type;
    private int amount;
    private short durability;
    private ItemMeta meta;
    
    public ItemStack(Material type) {
        this(type, 1);
    }
    
    public ItemStack(Material type, int amount) {
        this(type, amount, (short) 0);
    }
    
    public ItemStack(Material type, int amount, short durability) {
        this.type = type;
        this.amount = amount;
        this.durability = durability;
        this.meta = null;
    }
    
    public ItemStack(ItemStack stack) {
        this.type = stack.type;
        this.amount = stack.amount;
        this.durability = stack.durability;
        this.meta = stack.meta;
    }
    
    public Material getType() { return type; }
    public void setType(Material type) { this.type = type; }
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
    public short getDurability() { return durability; }
    public void setDurability(short durability) { this.durability = durability; }
    public ItemMeta getItemMeta() { return meta; }
    public void setItemMeta(ItemMeta meta) { this.meta = meta; }
    
    public boolean hasItemMeta() { return meta != null; }
    
    public boolean isSimilar(ItemStack stack) {
        if (stack == null) return false;
        return type == stack.type && durability == stack.durability;
    }
    
    @Override
    public ItemStack clone() {
        return new ItemStack(this);
    }
    
    @Override
    public String toString() {
        return "ItemStack{type=" + type + ", amount=" + amount + "}";
    }
}