package com.blazingmc.world.entity;

import com.blazingmc.world.World;
import org.bukkit.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EntityTest {
    
    private World world;
    private Location spawnLoc;
    
    @BeforeEach
    void setUp() {
        world = new World("test", 12345L, 10);
        spawnLoc = new Location(null, 0, 64, 0);
    }
    
    @Test
    void testEntityCreation() {
        Entity entity = new Entity(EntityType.ZOMBIE, world, spawnLoc);
        
        assertNotNull(entity.getUniqueId());
        assertEquals(EntityType.ZOMBIE, entity.getType());
        assertEquals(world, entity.getWorld());
        assertEquals(spawnLoc, entity.getLocation());
        assertFalse(entity.isDead());
        assertTrue(entity.isValid());
        assertFalse(entity.isOnFire());
        assertEquals(0, entity.getFireTicks());
        assertEquals(300, entity.getAirSupply());
    }
    
    @Test
    void testEntityMovement() {
        Entity entity = new Entity(EntityType.ZOMBIE, world, spawnLoc);
        
        entity.setVelocityX(1.0);
        entity.setVelocityZ(0.5);
        
        assertEquals(1.0, entity.getVelocityX());
        assertEquals(0.5, entity.getVelocityZ());
    }
    
    @Test
    void testEntityFire() {
        Entity entity = new Entity(EntityType.ZOMBIE, world, spawnLoc);
        
        assertFalse(entity.isOnFire());
        assertEquals(0, entity.getFireTicks());
        
        entity.setFire(100);
        assertTrue(entity.isOnFire());
        assertEquals(100, entity.getFireTicks());
        
        entity.extinguishFire();
        assertFalse(entity.isOnFire());
        assertEquals(0, entity.getFireTicks());
    }
    
    @Test
    void testEntityTick() {
        Entity entity = new Entity(EntityType.ZOMBIE, world, spawnLoc);
        
        entity.setFire(5);
        
        entity.tick();
        assertEquals(4, entity.getFireTicks());
        
        entity.tick();
        assertEquals(3, entity.getFireTicks());
    }
    
    @Test
    void testEntityTickExtinguishesFire() {
        Entity entity = new Entity(EntityType.ZOMBIE, world, spawnLoc);
        
        entity.setFire(1);
        
        entity.tick();
        assertEquals(0, entity.getFireTicks());
        assertFalse(entity.isOnFire());
    }
    
    @Test
    void testEntityRemove() {
        Entity entity = new Entity(EntityType.ZOMBIE, world, spawnLoc);
        
        assertTrue(entity.isValid());
        assertFalse(entity.isDead());
        
        entity.remove();
        
        assertFalse(entity.isValid());
        assertTrue(entity.isDead());
    }
    
    @Test
    void testEntityTickWhenDead() {
        Entity entity = new Entity(EntityType.ZOMBIE, world, spawnLoc);
        
        entity.setFire(10);
        entity.remove();
        
        entity.tick();
        
        assertEquals(10, entity.getFireTicks());
    }
    
    @Test
    void testEntityType() {
        Entity zombie = new Entity(EntityType.ZOMBIE, world, spawnLoc);
        assertEquals(EntityType.ZOMBIE, zombie.getType());
        assertTrue(zombie.getType().isLiving());
        
        Entity arrow = new Entity(EntityType.ARROW, world, spawnLoc);
        assertEquals(EntityType.ARROW, arrow.getType());
        assertFalse(arrow.getType().isLiving());
    }
    
    @Test
    void testNonLivingEntityNoAirSupply() {
        Entity arrow = new Entity(EntityType.ARROW, world, spawnLoc);
        
        assertEquals(0, arrow.getAirSupply());
        assertEquals(0, arrow.getMaxAirSupply());
    }
    
    @Test
    void testLivingEntityAirSupply() {
        Entity zombie = new Entity(EntityType.ZOMBIE, world, spawnLoc);
        
        assertEquals(300, zombie.getAirSupply());
        assertEquals(300, zombie.getMaxAirSupply());
    }
    
    @Test
    void testEntityRotation() {
        Entity entity = new Entity(EntityType.ZOMBIE, world, spawnLoc);
        
        entity.setYaw(45.0f);
        entity.setPitch(30.0f);
        
        assertEquals(45.0f, entity.getYaw());
        assertEquals(30.0f, entity.getPitch());
    }
    
    @Test
    void testEntityOnGround() {
        Entity entity = new Entity(EntityType.ZOMBIE, world, spawnLoc);
        
        assertTrue(entity.isOnGround());
        
        entity.setOnGround(false);
        assertFalse(entity.isOnGround());
    }
    
    @Test
    void testEntitySilent() {
        Entity entity = new Entity(EntityType.ZOMBIE, world, spawnLoc);
        
        assertFalse(entity.isSilent());
        
        entity.setSilent(true);
        assertTrue(entity.isSilent());
    }
    
    @Test
    void testEntityGlowing() {
        Entity entity = new Entity(EntityType.ZOMBIE, world, spawnLoc);
        
        assertFalse(entity.isGlowing());
        
        entity.setGlowing(true);
        assertTrue(entity.isGlowing());
    }
    
    @Test
    void testEntitySpawnSent() {
        Entity entity = new Entity(EntityType.ZOMBIE, world, spawnLoc);
        
        assertFalse(entity.isSpawnSent());
        
        entity.setSpawnSent(true);
        assertTrue(entity.isSpawnSent());
    }
    
    @Test
    void testEntityUniqueIds() {
        Entity entity1 = new Entity(EntityType.ZOMBIE, world, spawnLoc);
        Entity entity2 = new Entity(EntityType.ZOMBIE, world, spawnLoc);
        
        assertNotEquals(entity1.getUniqueId(), entity2.getUniqueId());
    }
    
    @Test
    void testEntityEntityIds() {
        Entity entity1 = new Entity(EntityType.ZOMBIE, world, spawnLoc);
        Entity entity2 = new Entity(EntityType.ZOMBIE, world, spawnLoc);
        
        assertNotEquals(entity1.getEntityId(), entity2.getEntityId());
    }
    
    @Test
    void testMobEntityCreation() {
        MobEntity mob = new MobEntity(EntityType.ZOMBIE, world, spawnLoc);
        
        assertEquals(20, mob.getHealth());
        assertEquals(20, mob.getMaxHealth());
        assertEquals(0.3f, mob.getMovementSpeed());
        assertTrue(mob.hasAI());
        assertTrue(mob.isValid());
    }
    
    @Test
    void testMobEntityHealth() {
        MobEntity mob = new MobEntity(EntityType.ZOMBIE, world, spawnLoc);
        
        mob.setHealth(10);
        assertEquals(10, mob.getHealth());
        
        mob.setHealth(0);
        assertEquals(0, mob.getHealth());
        assertTrue(mob.isDead());
    }
    
    @Test
    void testMobEntityMaxHealthClamp() {
        MobEntity mob = new MobEntity(EntityType.ZOMBIE, world, spawnLoc);
        
        mob.setHealth(100);
        assertEquals(20, mob.getHealth());
        
        mob.setHealth(-5);
        assertEquals(0, mob.getHealth());
    }
    
    @Test
    void testMobEntitySetMaxHealth() {
        MobEntity mob = new MobEntity(EntityType.ZOMBIE, world, spawnLoc);
        
        mob.setHealth(20);
        mob.setMaxHealth(10);
        assertEquals(10, mob.getHealth());
    }
    
    @Test
    void testMobEntityAI() {
        MobEntity mob = new MobEntity(EntityType.ZOMBIE, world, spawnLoc);
        
        assertTrue(mob.hasAI());
        
        mob.setNoAI();
        assertFalse(mob.hasAI());
        
        mob.setAware(true);
        assertTrue(mob.hasAI());
    }
    
    @Test
    void testMobEntityMovementSpeed() {
        MobEntity mob = new MobEntity(EntityType.ZOMBIE, world, spawnLoc);
        
        mob.setMovementSpeed(0.5f);
        assertEquals(0.5f, mob.getMovementSpeed());
    }
    
    @Test
    void testMobEntityFollowRange() {
        MobEntity mob = new MobEntity(EntityType.ZOMBIE, world, spawnLoc);
        
        mob.setFollowRange(32.0f);
        assertEquals(32.0f, mob.getFollowRange());
    }
    
    @Test
    void testMobEntityBaby() {
        MobEntity mob = new MobEntity(EntityType.ZOMBIE, world, spawnLoc);
        
        assertFalse(mob.isBaby());
        
        mob.setBaby(true);
        assertTrue(mob.isBaby());
    }
    
    @Test
    void testMobEntityPersistent() {
        MobEntity mob = new MobEntity(EntityType.ZOMBIE, world, spawnLoc);
        
        assertTrue(mob.isPersistent());
        
        mob.setPersistent(false);
        assertFalse(mob.isPersistent());
    }
    
    @Test
    void testMobEntityLoot() {
        MobEntity mob = new MobEntity(EntityType.ZOMBIE, world, spawnLoc);
        
        assertFalse(mob.canPickUpLoot());
        
        mob.setCanPickUpLoot(true);
        assertTrue(mob.canPickUpLoot());
    }
    
    @Test
    void testMobEntityAge() {
        MobEntity mob = new MobEntity(EntityType.ZOMBIE, world, spawnLoc);
        
        assertEquals(0, mob.getAge());
        assertEquals(6000, mob.getMaxAge());
        
        mob.setAge(100);
        assertEquals(100, mob.getAge());
    }
}
