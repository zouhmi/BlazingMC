package org.bukkit;

import org.bukkit.inventory.Inventory;

import java.util.UUID;

public interface Player extends Entity {
    String getName();
    UUID getUniqueId();
    int getProtocolVersion();
    void sendMessage(String message);
    void sendMessage(String[] messages);
    GameMode getGameMode();
    void setGameMode(GameMode mode);
    float getHealth();
    void setHealth(float health);
    int getLevel();
    void setLevel(int level);
    float getExp();
    void setExp(float exp);
    Location getLocation();
    void teleport(Location location);
    Inventory getInventory();
    void kickPlayer(String reason);
    void banPlayer(String reason);
    boolean isBanned();
    boolean isOp();
    void setOp(boolean value);
    boolean hasPermission(String permission);
}