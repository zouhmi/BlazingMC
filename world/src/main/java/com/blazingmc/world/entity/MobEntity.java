package com.blazingmc.world.entity;

import com.blazingmc.world.World;
import org.bukkit.Location;

import java.util.Random;

public class MobEntity extends Entity {
    private final Random random;
    private int noActionTicks;
    private int attackTicks;
    private int moveTicks;
    private boolean aware;
    private float movementSpeed;
    private float followRange;
    private int maxHealth;
    private int health;
    private boolean canPickUpLoot;
    private boolean persistent;
    private int persistenceRequired;
    private int leashDistance;
    private boolean leftHanded;
    private float babyChance;
    private boolean isBaby;
    private int age;
    private int maxAge;
    
    public MobEntity(EntityType type, World world, Location location) {
        super(type, world, location);
        this.random = new Random();
        this.noActionTicks = 0;
        this.attackTicks = 0;
        this.moveTicks = 0;
        this.aware = true;
        this.movementSpeed = 0.3f;
        this.followRange = 16.0f;
        this.maxHealth = 20;
        this.health = 20;
        this.canPickUpLoot = false;
        this.persistent = true;
        this.persistenceRequired = 0;
        this.leashDistance = -1;
        this.leftHanded = random.nextBoolean();
        this.babyChance = 0.05f;
        this.isBaby = false;
        this.age = 0;
        this.maxAge = 6000;
    }
    
    @Override
    public void tick() {
        if (!isDead() && isValid()) {
            super.tick();
            
            if (aware) {
                tickAI();
            }
            
            if (attackTicks > 0) {
                attackTicks--;
            }
            
            if (noActionTicks > 0) {
                noActionTicks--;
            }
            
            if (!persistent && isPersistenceRequired()) {
                persistenceRequired++;
                if (persistenceRequired > 6000) {
                    remove();
                }
            }
        }
    }
    
    protected void tickAI() {
        if (noActionTicks > 0) {
            return;
        }
        
        tickMovement();
        tickGoals();
    }
    
    protected void tickMovement() {
        if (moveTicks > 0) {
            moveTicks--;
            
            double speed = getMovementSpeed();
            double motionX = getVelocityX();
            double motionY = getVelocityY();
            double motionZ = getVelocityZ();
            
            double d0 = motionX * motionX + motionZ * motionZ;
            
            if (d0 > 0.001) {
                double d1 = Math.sqrt(d0);
                setVelocityX(motionX / d1 * speed);
                setVelocityZ(motionZ / d1 * speed);
            }
        }
    }
    
    protected void tickGoals() {
    }
    
    public void faceLocation(Location target) {
        if (target == null) return;
        
        Location current = getLocation();
        double dx = target.getX() - current.getX();
        double dz = target.getZ() - current.getZ();
        
        double angle = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;
        setYaw((float) angle);
    }
    
    public void moveTo(Location target) {
        if (target == null) return;
        
        faceLocation(target);
        
        double dx = target.getX() - getLocation().getX();
        double dz = target.getZ() - getLocation().getZ();
        
        setVelocityX(dx * 0.1);
        setVelocityZ(dz * 0.1);
        
        moveTicks = 10;
    }
    
    public void randomMove() {
        double x = (random.nextDouble() - 0.5) * 16.0;
        double z = (random.nextDouble() - 0.5) * 16.0;
        
        Location target = getLocation().add(x, 0, z);
        moveTo(target);
    }
    
    public boolean isPersistenceRequired() {
        return false;
    }
    
    public void setNoAI() {
        this.aware = false;
        this.moveTicks = 0;
    }
    
    public boolean hasAI() {
        return aware;
    }
    
    public void setAware(boolean aware) {
        this.aware = aware;
    }
    
    public float getMovementSpeed() {
        return movementSpeed;
    }
    
    public void setMovementSpeed(float speed) {
        this.movementSpeed = speed;
    }
    
    public float getFollowRange() {
        return followRange;
    }
    
    public void setFollowRange(float range) {
        this.followRange = range;
    }
    
    public int getMaxHealth() {
        return maxHealth;
    }
    
    public void setMaxHealth(int health) {
        this.maxHealth = health;
        if (this.health > health) {
            this.health = health;
        }
    }
    
    public int getHealth() {
        return health;
    }
    
    public void setHealth(int health) {
        this.health = Math.max(0, Math.min(maxHealth, health));
        if (this.health <= 0) {
            die();
        }
    }
    
    protected void die() {
        remove();
    }
    
    public boolean canPickUpLoot() {
        return canPickUpLoot;
    }
    
    public void setCanPickUpLoot(boolean canPickUpLoot) {
        this.canPickUpLoot = canPickUpLoot;
    }
    
    public boolean isPersistent() {
        return persistent;
    }
    
    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }
    
    public int getLeashDistance() {
        return leashDistance;
    }
    
    public void setLeashDistance(int distance) {
        this.leashDistance = distance;
    }
    
    public boolean isLeftHanded() {
        return leftHanded;
    }
    
    public void setLeftHanded(boolean leftHanded) {
        this.leftHanded = leftHanded;
    }
    
    public float getBabyChance() {
        return babyChance;
    }
    
    public void setBabyChance(float chance) {
        this.babyChance = chance;
    }
    
    public boolean isBaby() {
        return isBaby;
    }
    
    public void setBaby(boolean baby) {
        this.isBaby = baby;
    }
    
    public int getAge() {
        return age;
    }
    
    public void setAge(int age) {
        this.age = age;
    }
    
    public int getMaxAge() {
        return maxAge;
    }
    
    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }
    
    public int getAttackTicks() {
        return attackTicks;
    }
    
    public void setAttackTicks(int ticks) {
        this.attackTicks = ticks;
    }
    
    public int getNoActionTicks() {
        return noActionTicks;
    }
    
    public void setNoActionTicks(int ticks) {
        this.noActionTicks = ticks;
    }
}