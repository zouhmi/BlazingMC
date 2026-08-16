package com.blazingmc.world.entity;

import com.blazingmc.world.World;
import org.bukkit.Location;

import java.util.Random;

public class ZombieEntity extends HostileMobEntity {
    private final Random random;
    private boolean burning;
    private int burnTicks;
    
    public ZombieEntity(World world, Location location) {
        super(EntityType.ZOMBIE, world, location);
        this.random = new Random();
        this.burning = false;
        this.burnTicks = 0;
    }
    
    @Override
    protected void tickGoals() {
        super.tickGoals();
        
        if (burning) {
            burnTicks++;
            if (burnTicks >= 20) {
                burnTicks = 0;
                setHealth(getHealth() - 1);
            }
        }
        
        if (isDaytime() && !burning) {
            Location loc = getLocation();
            if (loc != null && getWorld() != null) {
                int bx = (int) Math.floor(loc.getX());
                int by = (int) Math.floor(loc.getY());
                int bz = (int) Math.floor(loc.getZ());
                
                if (getWorld().getHighestBlockY(bx, bz) <= by) {
                    burning = true;
                    burnTicks = 0;
                }
            }
        }
    }
    
    @Override
    protected void findTarget() {
        if (getWorld() == null) return;
        
        double closestDistance = getFollowRange();
        Entity closest = null;
        
        for (Entity entity : getWorld().getEntities()) {
            if (entity == this || entity.isDead() || !entity.isValid()) continue;
            if (entity.getType().isHostile()) continue;
            
            double dist = distanceTo(entity);
            if (dist < closestDistance) {
                closestDistance = dist;
                closest = entity;
            }
        }
        
        target = closest;
    }
    
    @Override
    protected double getAttackRange() {
        return 2.0;
    }
    
    @Override
    protected int getAttackCooldownMax() {
        return 40;
    }
    
    @Override
    protected void performAttack(Entity target) {
        if (target instanceof MobEntity mob) {
            mob.setHealth(mob.getHealth() - (int) attackDamage);
        }
    }
    
    private boolean isDaytime() {
        if (getWorld() == null) return false;
        long time = getWorld().getTime();
        return time >= 0 && time < 12300;
    }
    
    public boolean isBurning() {
        return burning;
    }
    
    public void setBurning(boolean burning) {
        this.burning = burning;
        this.burnTicks = 0;
    }
}
