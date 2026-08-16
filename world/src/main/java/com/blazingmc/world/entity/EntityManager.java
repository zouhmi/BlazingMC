package com.blazingmc.world.entity;

import com.blazingmc.world.World;
import org.bukkit.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EntityManager {
    private static final Logger logger = LoggerFactory.getLogger(EntityManager.class);
    private final Map<Integer, Entity> entities;
    private final Map<UUID, Entity> entitiesByUuid;
    private int nextEntityId;
    
    public EntityManager() {
        this.entities = new ConcurrentHashMap<>();
        this.entitiesByUuid = new ConcurrentHashMap<>();
        this.nextEntityId = 1;
    }
    
    public Entity spawnEntity(EntityType type, World world, Location location) {
        Entity entity = new Entity(type, world, location);
        registerEntity(entity);
        logger.debug("Spawned entity {} at {}, {}, {}", type.name(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        return entity;
    }
    
    public MobEntity spawnMob(EntityType type, World world, Location location) {
        MobEntity mob = new MobEntity(type, world, location);
        registerEntity(mob);
        logger.debug("Spawned mob {} at {}, {}, {}", type.name(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        return mob;
    }
    
    private void registerEntity(Entity entity) {
        entities.put(entity.getEntityId(), entity);
        entitiesByUuid.put(entity.getUniqueId(), entity);
    }
    
    public void addEntity(Entity entity) {
        registerEntity(entity);
    }
    
    public void removeEntity(int entityId) {
        Entity entity = entities.remove(entityId);
        if (entity != null) {
            entity.remove();
            entitiesByUuid.remove(entity.getUniqueId());
        }
    }
    
    public void removeEntity(UUID uuid) {
        Entity entity = entitiesByUuid.remove(uuid);
        if (entity != null) {
            entity.remove();
            entities.remove(entity.getEntityId());
        }
    }
    
    public Entity getEntity(int entityId) {
        return entities.get(entityId);
    }
    
    public Entity getEntity(UUID uuid) {
        return entitiesByUuid.get(uuid);
    }
    
    public List<Entity> getEntitiesNear(Location center, double range) {
        List<Entity> result = new ArrayList<>();
        double rangeSquared = range * range;
        
        for (Entity entity : entities.values()) {
            if (entity.getWorld() == center.getWorld()) {
                double dx = entity.getLocation().getX() - center.getX();
                double dy = entity.getLocation().getY() - center.getY();
                double dz = entity.getLocation().getZ() - center.getZ();
                double distanceSquared = dx * dx + dy * dy + dz * dz;
                
                if (distanceSquared <= rangeSquared) {
                    result.add(entity);
                }
            }
        }
        
        return result;
    }
    
    public List<MobEntity> getMobsNear(Location center, double range) {
        List<MobEntity> result = new ArrayList<>();
        double rangeSquared = range * range;
        
        for (Entity entity : entities.values()) {
            if (entity instanceof MobEntity && entity.getWorld() == center.getWorld()) {
                double dx = entity.getLocation().getX() - center.getX();
                double dy = entity.getLocation().getY() - center.getY();
                double dz = entity.getLocation().getZ() - center.getZ();
                double distanceSquared = dx * dx + dy * dy + dz * dz;
                
                if (distanceSquared <= rangeSquared) {
                    result.add((MobEntity) entity);
                }
            }
        }
        
        return result;
    }
    
    public int getEntityCount() {
        return entities.size();
    }
    
    public int getMobCount() {
        int count = 0;
        for (Entity entity : entities.values()) {
            if (entity instanceof MobEntity) {
                count++;
            }
        }
        return count;
    }
    
    public int getNextEntityId() {
        return nextEntityId++;
    }
    
    public void tickAll() {
        List<Entity> toRemove = new ArrayList<>();
        
        for (Entity entity : entities.values()) {
            if (entity.isDead() || !entity.isValid()) {
                toRemove.add(entity);
            } else {
                entity.tick();
            }
        }
        
        for (Entity entity : toRemove) {
            entities.remove(entity.getEntityId());
            entitiesByUuid.remove(entity.getUniqueId());
        }
    }
    
    public void clear() {
        for (Entity entity : entities.values()) {
            entity.remove();
        }
        entities.clear();
        entitiesByUuid.clear();
    }
    
    public Map<Integer, Entity> getEntities() {
        return entities;
    }
    
    public Map<UUID, Entity> getEntitiesByUuid() {
        return entitiesByUuid;
    }
}