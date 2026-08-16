package org.bukkit.entity;

public interface Tameable extends Animal {
    boolean isTamed();
    void setTamed(boolean tamed);
    Entity getOwner();
    void setOwner(Entity owner);
}