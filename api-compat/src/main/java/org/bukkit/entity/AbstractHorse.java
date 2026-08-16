package org.bukkit.entity;

public interface AbstractHorse extends Animal {
    boolean isTamed();
    void setTamed(boolean tamed);
    Entity getOwner();
    void setOwner(Entity owner);
    int getDomestication();
    void setDomestication(int domestication);
    int getMaxDomestication();
    int getJumpStrength();
    void setJumpStrength(double strength);
    boolean isEating();
    boolean isEatingGrass();
    boolean isTired();
}