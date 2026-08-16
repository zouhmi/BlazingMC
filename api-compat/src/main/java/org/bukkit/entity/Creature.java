package org.bukkit.entity;

import org.bukkit.Location;

public interface Creature extends Entity {
    void moveTo(Location location);
    void moveTo(Location location, double speed);
    void move(double x, double y, double z);
    void move(double x, double y, double z, double speed);
    void setTarget(Entity target);
    Entity getTarget();
}