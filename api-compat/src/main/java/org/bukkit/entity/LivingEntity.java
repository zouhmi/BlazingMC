package org.bukkit.entity;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.List;

public interface LivingEntity extends Entity {
    double getHealth();
    void setHealth(double health);
    double getMaxHealth();
    void setMaxHealth(double health);
    void resetMaxHealth();
    double getAbsorptionAmount();
    void setAbsorptionAmount(double amount);
    int getRemainingAir();
    void setRemainingAir(int ticks);
    int getMaximumAir();
    void setMaximumAir(int ticks);
    int getTicksWithoutAir();
    void setTicksWithoutAir(int ticks);
    boolean isLeashed();
    Entity getLeashHolder();
    boolean setLeashHolder(Entity holder);
    boolean isGliding();
    void setGliding(boolean gliding);
    boolean isSwimming();
    void setSwimming(boolean swimming);
    boolean isSleeping();
    boolean isClimbing();
    void setCanPickupItems(boolean pickup);
    boolean getCanPickupItems();
    boolean isCollidable();
    int getArrowCooldown();
    void setArrowCooldown(int ticks);
    int getArrowsInBody();
    void setArrowsInBody(int count);
    List<PotionEffect> getActivePotionEffects();
    boolean addPotionEffect(PotionEffect effect);
    boolean addPotionEffect(PotionEffect effect, boolean force);
    boolean removePotionEffect(PotionEffectType type);
    boolean hasPotionEffect(PotionEffectType type);
    void removePotionEffects(PotionEffectType type);
    double getAttribute(Attribute attribute);
}