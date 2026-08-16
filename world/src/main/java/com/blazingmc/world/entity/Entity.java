package com.blazingmc.world.entity;

import com.blazingmc.world.World;
import org.bukkit.Location;

import java.util.UUID;

public class Entity {
    private static int nextEntityId = 1;
    
    private final int entityId;
    private final UUID uuid;
    private final EntityType type;
    private World world;
    private Location location;
    private boolean dead;
    private boolean valid;
    private boolean spawnSent;
    private float yaw;
    private float pitch;
    private boolean onGround;
    private double velocityX;
    private double velocityY;
    private double velocityZ;
    private int fireTicks;
    private int airSupply;
    private int maxAirSupply;
    private boolean silent;
    private boolean glowing;
    private boolean customNameVisible;
    private boolean glowingEffect;
    
    public Entity(EntityType type, World world, Location location) {
        this.entityId = nextEntityId++;
        this.uuid = UUID.randomUUID();
        this.type = type;
        this.world = world;
        this.location = location;
        this.dead = false;
        this.valid = true;
        this.spawnSent = false;
        this.yaw = 0;
        this.pitch = 0;
        this.onGround = true;
        this.velocityX = 0;
        this.velocityY = 0;
        this.velocityZ = 0;
        this.fireTicks = 0;
        this.airSupply = type.isLiving() ? 300 : 0;
        this.maxAirSupply = type.isLiving() ? 300 : 0;
        this.silent = false;
        this.glowing = false;
        this.customNameVisible = false;
        this.glowingEffect = false;
    }
    
    public void tick() {
        if (dead || !valid) {
            return;
        }
        
        if (fireTicks > 0) {
            fireTicks--;
            if (fireTicks == 0) {
                extinguishFire();
            }
        }
        
        if (type.isLiving() && world != null) {
            tickAirSupply();
        }
        
        if (velocityX != 0 || velocityY != 0 || velocityZ != 0) {
            location = location.add(velocityX, velocityY, velocityZ);
            
            if (type.isLiving() && !onGround) {
                velocityY -= 0.08;
            }
            
            if (world != null) {
                int blockY = world.getHighestBlockY((int) location.getX(), (int) location.getZ());
                if (location.getY() <= blockY + 1) {
                    location.setY(blockY + 1);
                    velocityY = 0;
                    onGround = true;
                } else {
                    onGround = false;
                }
            }
            
            velocityX *= 0.98;
            velocityY *= 0.98;
            velocityZ *= 0.98;
        }
    }
    
    private void tickAirSupply() {
        if (isUnderwater()) {
            airSupply--;
            if (airSupply <= 0) {
                damage(2);
                airSupply = 0;
            }
        } else {
            airSupply = maxAirSupply;
        }
    }
    
    private boolean isUnderwater() {
        if (world == null) return false;
        
        int blockX = (int) Math.floor(location.getX());
        int blockY = (int) Math.floor(location.getY());
        int blockZ = (int) Math.floor(location.getZ());
        
        org.bukkit.Material block = world.getBlockAt(blockX, blockY, blockZ);
        return block == org.bukkit.Material.WATER;
    }
    
    public void damage(double amount) {
        if (dead || !valid) return;
        
        onDamage(amount);
    }
    
    protected void onDamage(double amount) {
    }
    
    public void setFire(int ticks) {
        this.fireTicks = ticks;
    }
    
    public void extinguishFire() {
        this.fireTicks = 0;
    }
    
    public boolean isOnFire() {
        return fireTicks > 0;
    }
    
    public void remove() {
        dead = true;
        valid = false;
    }
    
    public boolean isSpawnSent() { return spawnSent; }
    public void setSpawnSent(boolean spawnSent) { this.spawnSent = spawnSent; }
    
    public int getEntityId() { return entityId; }
    public UUID getUniqueId() { return uuid; }
    public EntityType getType() { return type; }
    public World getWorld() { return world; }
    public void setWorld(World world) { this.world = world; }
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    public boolean isDead() { return dead; }
    public boolean isValid() { return valid; }
    public float getYaw() { return yaw; }
    public void setYaw(float yaw) { this.yaw = yaw; }
    public float getPitch() { return pitch; }
    public void setPitch(float pitch) { this.pitch = pitch; }
    public boolean isOnGround() { return onGround; }
    public void setOnGround(boolean onGround) { this.onGround = onGround; }
    public double getVelocityX() { return velocityX; }
    public void setVelocityX(double velocityX) { this.velocityX = velocityX; }
    public double getVelocityY() { return velocityY; }
    public void setVelocityY(double velocityY) { this.velocityY = velocityY; }
    public double getVelocityZ() { return velocityZ; }
    public void setVelocityZ(double velocityZ) { this.velocityZ = velocityZ; }
    public int getFireTicks() { return fireTicks; }
    public int getAirSupply() { return airSupply; }
    public int getMaxAirSupply() { return maxAirSupply; }
    public boolean isSilent() { return silent; }
    public void setSilent(boolean silent) { this.silent = silent; }
    public boolean isGlowing() { return glowing; }
    public void setGlowing(boolean glowing) { this.glowing = glowing; }
    public boolean isCustomNameVisible() { return customNameVisible; }
    public void setCustomNameVisible(boolean customNameVisible) { this.customNameVisible = customNameVisible; }
}