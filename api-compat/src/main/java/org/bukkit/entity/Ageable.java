package org.bukkit.entity;

public interface Ageable extends Creature {
    int getAge();
    void setAge(int age);
    boolean isAdult();
    void setAdult();
    void setBaby();
    boolean getAgeLock();
    void setAgeLock(boolean lock);
    boolean canBreed();
    void setCanBreed(boolean breed);
}