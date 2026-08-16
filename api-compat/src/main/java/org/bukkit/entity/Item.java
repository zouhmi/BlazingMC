package org.bukkit.entity;

import org.bukkit.inventory.ItemStack;

public interface Item extends Entity {
    ItemStack getItemStack();
    void setItemStack(ItemStack stack);
    int getPickupDelay();
    void setPickupDelay(int delay);
    boolean canMobPickup();
    void setCanMobPickup(boolean pickup);
    boolean canPlayerPickup();
    void setCanPlayerPickup(boolean pickup);
    boolean willAge();
    void setWillAge(boolean age);
    void setUnlimitedLifetime(boolean unlimited);
    boolean hasUnlimitedLifetime();
    boolean isAgeable();
    void setAge(int age);
    int getAge();
}