package com.blazingmc.world.entity;

import com.blazingmc.world.World;
import org.bukkit.Location;

import java.util.Random;

public class HostileMobEntity extends MobEntity {
    protected Entity target;
    protected int targetSearchTicks;
    protected int attackCooldown;
    protected float attackDamage;
    protected boolean hostile;
    
    public HostileMobEntity(EntityType type, World world, Location location) {
        super(type, world, location);
        this.target = null;
        this.targetSearchTicks = 0;
        this.attackCooldown = 0;
        this.attackDamage = 2.0f;
        this.hostile = true;
        
        configureForType(type);
    }
    
    private void configureForType(EntityType type) {
        switch (type) {
            case ZOMBIE -> {
                setMaxHealth(20);
                setHealth(20);
                setMovementSpeed(0.23f);
                setFollowRange(40.0f);
                attackDamage = 3.0f;
                attackCooldown = 40;
            }
            case SKELETON -> {
                setMaxHealth(20);
                setHealth(20);
                setMovementSpeed(0.25f);
                setFollowRange(16.0f);
                attackDamage = 2.0f;
                attackCooldown = 60;
            }
            case CREEPER -> {
                setMaxHealth(20);
                setHealth(20);
                setMovementSpeed(0.25f);
                setFollowRange(16.0f);
                attackDamage = 0.0f;
                attackCooldown = 30;
            }
            case SPIDER -> {
                setMaxHealth(16);
                setHealth(16);
                setMovementSpeed(0.3f);
                setFollowRange(16.0f);
                attackDamage = 2.0f;
                attackCooldown = 30;
            }
            default -> {}
        }
    }
    
    @Override
    protected void tickGoals() {
        if (hostile) {
            tickHostileAI();
        }
    }
    
    protected void tickHostileAI() {
        if (attackCooldown > 0) {
            attackCooldown--;
        }
        
        if (target == null || target.isDead() || !target.isValid()) {
            target = null;
            targetSearchTicks++;
            
            if (targetSearchTicks >= 20) {
                targetSearchTicks = 0;
                findTarget();
            }
        } else {
            targetSearchTicks = 0;
            
            double distance = distanceTo(target);
            
            if (distance > getFollowRange()) {
                target = null;
                return;
            }
            
            faceLocation(target.getLocation());
            
            if (distance > getAttackRange()) {
                moveTo(target.getLocation());
            } else if (attackCooldown <= 0) {
                performAttack(target);
                attackCooldown = getAttackCooldownMax();
            }
        }
    }
    
    protected void findTarget() {
    }
    
    protected double getAttackRange() {
        return 2.0;
    }
    
    protected int getAttackCooldownMax() {
        return 40;
    }
    
    protected void performAttack(Entity target) {
    }
    
    protected double distanceTo(Entity entity) {
        if (entity == null || entity.getLocation() == null || getLocation() == null) {
            return Double.MAX_VALUE;
        }
        
        double dx = entity.getLocation().getX() - getLocation().getX();
        double dy = entity.getLocation().getY() - getLocation().getY();
        double dz = entity.getLocation().getZ() - getLocation().getZ();
        
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    
    protected double distanceTo(Location location) {
        if (location == null || getLocation() == null) {
            return Double.MAX_VALUE;
        }
        
        double dx = location.getX() - getLocation().getX();
        double dy = location.getY() - getLocation().getY();
        double dz = location.getZ() - getLocation().getZ();
        
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    
    public Entity getTarget() {
        return target;
    }
    
    public void setTarget(Entity target) {
        this.target = target;
    }
    
    public float getAttackDamage() {
        return attackDamage;
    }
    
    public void setAttackDamage(float damage) {
        this.attackDamage = damage;
    }
    
    public boolean isHostile() {
        return hostile;
    }
    
    public void setHostile(boolean hostile) {
        this.hostile = hostile;
    }
}
