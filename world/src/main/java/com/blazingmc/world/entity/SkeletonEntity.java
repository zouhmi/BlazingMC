package com.blazingmc.world.entity;

import com.blazingmc.world.World;
import org.bukkit.Location;

import java.util.Random;

public class SkeletonEntity extends HostileMobEntity {
    private final Random random;
    private int shootCooldown;
    private static final int SHOOT_INTERVAL = 60;
    
    public SkeletonEntity(World world, Location location) {
        super(EntityType.SKELETON, world, location);
        this.random = new Random();
        this.shootCooldown = 0;
        setAttackDamage(2.0f);
    }
    
    @Override
    protected void tickGoals() {
        super.tickGoals();
        
        if (shootCooldown > 0) {
            shootCooldown--;
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
        return 15.0;
    }
    
    @Override
    protected int getAttackCooldownMax() {
        return SHOOT_INTERVAL;
    }
    
    @Override
    protected void performAttack(Entity target) {
        if (target == null || target.isDead() || !target.isValid()) return;
        
        shootCooldown = SHOOT_INTERVAL;
        
        faceLocation(target.getLocation());
        
        spawnArrow(target);
    }
    
    private void spawnArrow(Entity target) {
        if (getWorld() == null) return;
        
        Location start = getLocation();
        if (start == null) return;
        
        double dx = target.getLocation().getX() - start.getX();
        double dy = target.getLocation().getY() + 0.5 - start.getY();
        double dz = target.getLocation().getZ() - start.getZ();
        
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        
        double velocityX = dx / distance * 1.5;
        double velocityY = dy / distance * 1.5;
        double velocityZ = dz / distance * 1.5;
        
        double gravity = 0.05;
        double time = distance / 1.5;
        velocityY += 0.5 * gravity * time;
        
        Entity arrow = new Entity(EntityType.ARROW, getWorld(), start);
        arrow.setVelocityX(velocityX);
        arrow.setVelocityY(velocityY);
        arrow.setVelocityZ(velocityZ);
        
        getWorld().addEntity(arrow);
    }
    
    public int getShootCooldown() {
        return shootCooldown;
    }
}
