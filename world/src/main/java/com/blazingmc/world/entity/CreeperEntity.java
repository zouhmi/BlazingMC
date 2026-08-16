package com.blazingmc.world.entity;

import com.blazingmc.world.World;
import org.bukkit.Location;

import java.util.Random;

public class CreeperEntity extends HostileMobEntity {
    private final Random random;
    private int fuseTicks;
    private boolean ignited;
    private boolean charged;
    private int maxFuseTime;
    
    public CreeperEntity(World world, Location location) {
        super(EntityType.CREEPER, world, location);
        this.random = new Random();
        this.fuseTicks = 0;
        this.ignited = false;
        this.charged = false;
        this.maxFuseTime = 30;
        setAttackDamage(0.0f);
    }
    
    @Override
    protected void tickGoals() {
        super.tickGoals();
        
        if (ignited) {
            fuseTicks++;
            
            if (fuseTicks >= maxFuseTime) {
                explode();
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
    protected void tickHostileAI() {
        super.tickHostileAI();
        
        if (target != null && !target.isDead() && target.isValid()) {
            double distance = distanceTo(target);
            
            if (distance <= 3.0 && !ignited) {
                ignite();
            } else if (distance > 5.0 && ignited) {
                defuse();
            }
        } else {
            if (ignited) {
                defuse();
            }
        }
    }
    
    @Override
    protected double getAttackRange() {
        return 3.0;
    }
    
    @Override
    protected int getAttackCooldownMax() {
        return 30;
    }
    
    @Override
    protected void performAttack(Entity target) {
        if (!ignited) {
            ignite();
        }
    }
    
    public void ignite() {
        ignited = true;
        fuseTicks = 0;
    }
    
    public void defuse() {
        ignited = false;
        fuseTicks = 0;
    }
    
    private void explode() {
        double explosionRadius = charged ? 6.0 : 3.0;
        int damage = charged ? 64 : 43;
        
        if (getWorld() == null) return;
        
        Location loc = getLocation();
        if (loc == null) return;
        
        for (Entity entity : getWorld().getEntities()) {
            if (entity == this || entity.isDead() || !entity.isValid()) continue;
            
            double dist = distanceTo(entity);
            if (dist <= explosionRadius) {
                int damageAmount = (int) (damage * (1.0 - dist / explosionRadius));
                
                if (entity instanceof MobEntity mob) {
                    mob.setHealth(mob.getHealth() - damageAmount);
                }
            }
        }
        
        destroyBlocks(loc, explosionRadius);
        
        remove();
    }
    
    private void destroyBlocks(Location center, double radius) {
        if (getWorld() == null) return;
        
        int cx = (int) Math.floor(center.getX());
        int cy = (int) Math.floor(center.getY());
        int cz = (int) Math.floor(center.getZ());
        
        int r = (int) Math.ceil(radius);
        
        for (int x = cx - r; x <= cx + r; x++) {
            for (int y = cy - r; y <= cy + r; y++) {
                for (int z = cz - r; z <= cz + r; z++) {
                    double dist = Math.sqrt(
                        (x + 0.5 - center.getX()) * (x + 0.5 - center.getX()) +
                        (y + 0.5 - center.getY()) * (y + 0.5 - center.getY()) +
                        (z + 0.5 - center.getZ()) * (z + 0.5 - center.getZ())
                    );
                    
                    if (dist <= radius) {
                        getWorld().setBlockAt(x, y, z, org.bukkit.Material.AIR);
                    }
                }
            }
        }
    }
    
    public boolean isIgnited() {
        return ignited;
    }
    
    public boolean isCharged() {
        return charged;
    }
    
    public void setCharged(boolean charged) {
        this.charged = charged;
    }
    
    public int getFuseTicks() {
        return fuseTicks;
    }
    
    public int getMaxFuseTime() {
        return maxFuseTime;
    }
    
    public void setMaxFuseTime(int time) {
        this.maxFuseTime = time;
    }
}
