package org.bukkit.entity;

public interface AbstractProjectile extends Entity {
    Entity getShooter();
    void setShooter(Entity shooter);
    double getDamage();
    void setDamage(double damage);
    boolean isBounce();
    void setBounce(boolean bounce);
}