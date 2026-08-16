package org.bukkit;

import java.util.UUID;

public interface Entity {
    int getEntityId();
    UUID getUniqueId();
    Location getLocation();
    void setLocation(Location location);
    World getWorld();
    boolean isDead();
    void remove();
    java.util.List<Entity> getNearbyEntities(double x, double y, double z);
    void setVelocity(org.bukkit.util.Vector velocity);
    org.bukkit.util.Vector getVelocity();
    boolean isOnGround();
}