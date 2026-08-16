package org.bukkit;

import org.bukkit.entity.EntityType;

import java.util.UUID;

public interface World {
    String getName();
    UUID getUID();
    int getMaxHeight();
    int getMinHeight();
    long getFullTime();
    long getTime();
    void setTime(long time);
    Environment getEnvironment();
    Block getBlockAt(int x, int y, int z);
    Block getBlockAt(Location location);
    Entity spawnEntity(Location loc, EntityType type);
    java.util.List<Entity> getEntities();
    java.util.List<Player> getPlayers();
}