package org.bukkit.inventory;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public interface HumanEntity extends LivingEntity {
    String getName();
    Inventory getInventory();
    Inventory getEnderChest();
    GameMode getGameMode();
    void setGameMode(GameMode mode);
    boolean isSleeping();
    void openInventory(Inventory inventory);
    void closeInventory();
    Inventory getOpenInventory();
    void sendActionBar(String message);
    void sendTitle(String title, String subtitle);
    void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut);
    int getExpToLevel();
}