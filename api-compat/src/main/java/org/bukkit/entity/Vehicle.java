package org.bukkit.entity;

public interface Vehicle extends Entity {
    double getMaxSpeed();
    void setMaxSpeed(double speed);
    boolean isSlowWhenEmpty();
    void setSlowWhenEmpty(boolean slow);
    double getOccupiedSpeed();
    void setOccupiedSpeed(double speed);
    double getFlySpeed();
    void setFlySpeed(double speed);
    boolean isSteerabile();
}