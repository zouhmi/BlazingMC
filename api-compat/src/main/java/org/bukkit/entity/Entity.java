package org.bukkit.entity;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;

public interface Entity {
    int getEntityId();
    UUID getUniqueId();
    Location getLocation();
    Location getLocation(Location loc);
    void setLocation(Location location);
    World getWorld();
    boolean isDead();
    void remove();
    boolean isValid();
    boolean isPersistent();
    void setPersistent(boolean persistent);
    String getCustomName();
    void setCustomName(String name);
    boolean isCustomNameVisible();
    void setCustomNameVisible(boolean visible);
    boolean isGlowing();
    void setGlowing(boolean glowing);
    boolean isInvulnerable();
    void setInvulnerable(boolean invulnerable);
    boolean hasGravity();
    void setGravity(boolean gravity);
    int getTicksLived();
    void setTicksLived(int ticks);
    boolean isOnGround();
    Vector getVelocity();
    void setVelocity(Vector velocity);
    Entity getPassenger();
    boolean setPassenger(Entity passenger);
    List<Entity> getPassengers();
    boolean addPassenger(Entity passenger);
    boolean removePassenger(Entity passenger);
    boolean isEmpty();
    boolean eject();
    float getFallDistance();
    void setFallDistance(float distance);
    void fireTicks(int ticks);
    int getFireTicks();
    void setFireTicks(int ticks);
    boolean isVisualFire();
    void setVisualFire(boolean fire);
    int getMaxFireTicks();
    void setSilent(boolean flag);
    boolean isSilent();
    void setNoPhysics(boolean flag);
    boolean hasNoPhysics();
    void spigot();
    String getName();
}