package com.blazingmc.server.entity;

import com.blazingmc.chat.ConsoleLogger;
import com.blazingmc.protocol.handler.PlayerInterface;
import com.blazingmc.server.BlazingServer;
import com.blazingmc.server.player.PlayerManager;
import com.blazingmc.world.World;
import com.blazingmc.world.entity.CreeperEntity;
import com.blazingmc.world.entity.Entity;
import com.blazingmc.world.entity.EntityType;
import com.blazingmc.world.entity.MobEntity;
import com.blazingmc.world.entity.SkeletonEntity;
import com.blazingmc.world.entity.ZombieEntity;
import org.bukkit.Location;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SpawnManager {
    private final BlazingServer server;
    private final Map<Integer, Entity> entities;
    private final Random random;
    private int spawnTickCounter;
    
    public SpawnManager(BlazingServer server) {
        this.server = server;
        this.entities = new ConcurrentHashMap<>();
        this.random = new Random();
        this.spawnTickCounter = 0;
    }
    
    public void tick() {
        spawnTickCounter++;
        
        if (spawnTickCounter >= 100) {
            spawnTickCounter = 0;
            attemptNaturalSpawn();
        }
        
        tickEntities();
    }
    
    private void tickEntities() {
        for (Entity entity : entities.values()) {
            entity.tick();
        }
    }
    
    private void attemptNaturalSpawn() {
        PlayerManager playerManager = server.getPlayerManager();
        if (playerManager.getOnlinePlayerCount() == 0) return;
        
        for (PlayerInterface player : playerManager.getOnlinePlayers()) {
            if (random.nextInt(100) < 5) {
                spawnNearPlayer(player);
            }
        }
    }
    
    private void spawnNearPlayer(PlayerInterface player) {
        World world = server.getWorld();
        
        int spawnX = (int) player.getX() + random.nextInt(32) - 16;
        int spawnZ = (int) player.getZ() + random.nextInt(32) - 16;
        int spawnY = world.getHighestBlockY(spawnX, spawnZ) + 1;
        
        Location loc = new Location(null, spawnX, spawnY, spawnZ);
        
        EntityType type = getRandomSpawnableType();
        
        spawnEntity(type, loc);
    }
    
    private EntityType getRandomSpawnableType() {
        World world = server.getWorld();
        boolean isNight = world != null && world.getTime() >= 12300;
        
        if (isNight && random.nextInt(100) < 40) {
            EntityType[] hostileTypes = {
                EntityType.ZOMBIE,
                EntityType.SKELETON,
                EntityType.CREEPER,
                EntityType.SPIDER
            };
            return hostileTypes[random.nextInt(hostileTypes.length)];
        }
        
        EntityType[] passiveTypes = {
            EntityType.PIG,
            EntityType.COW,
            EntityType.SHEEP,
            EntityType.CHICKEN
        };
        
        return passiveTypes[random.nextInt(passiveTypes.length)];
    }
    
    public Entity spawnEntity(EntityType type, Location location) {
        MobEntity entity = switch (type) {
            case ZOMBIE -> new ZombieEntity(server.getWorld(), location);
            case SKELETON -> new SkeletonEntity(server.getWorld(), location);
            case CREEPER -> new CreeperEntity(server.getWorld(), location);
            default -> new MobEntity(type, server.getWorld(), location);
        };
        
        entities.put(entity.getEntityId(), entity);
        
        broadcastSpawn(entity);
        
        ConsoleLogger.debug("Spawned " + type.getName() + " at " + 
                          (int) location.getX() + ", " + (int) location.getY() + ", " + (int) location.getZ());
        
        return entity;
    }
    
    public void removeEntity(int entityId) {
        Entity entity = entities.remove(entityId);
        if (entity != null) {
            broadcastRemove(entity);
        }
    }
    
    public void removeEntity(Entity entity) {
        entities.remove(entity.getEntityId());
        broadcastRemove(entity);
    }
    
    public Entity getEntity(int entityId) {
        return entities.get(entityId);
    }
    
    public Collection<Entity> getEntities() {
        return Collections.unmodifiableCollection(entities.values());
    }
    
    public int getEntityCount() {
        return entities.size();
    }
    
    private void broadcastSpawn(Entity entity) {
        ByteBuffer buffer = ByteBuffer.allocate(128).order(ByteOrder.BIG_ENDIAN);
        
        buffer.putInt(entity.getEntityId());
        writeUUID(buffer, entity.getUniqueId());
        writeVarInt(buffer, getEntityTypeId(entity.getType()));
        buffer.putDouble(entity.getLocation().getX());
        buffer.putDouble(entity.getLocation().getY());
        buffer.putDouble(entity.getLocation().getZ());
        buffer.putFloat(entity.getYaw());
        buffer.putFloat(entity.getPitch());
        
        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        
        for (PlayerInterface player : server.getPlayerManager().getOnlinePlayers()) {
            player.sendPacket(0x01, data);
        }
    }
    
    private void broadcastRemove(Entity entity) {
        ByteBuffer buffer = ByteBuffer.allocate(16).order(ByteOrder.BIG_ENDIAN);
        
        writeVarInt(buffer, 1);
        buffer.putInt(entity.getEntityId());
        
        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        
        for (PlayerInterface player : server.getPlayerManager().getOnlinePlayers()) {
            player.sendPacket(0x3A, data);
        }
    }
    
    private int getEntityTypeId(EntityType type) {
        return switch (type) {
            case PIG -> 10;
            case COW -> 11;
            case SHEEP -> 12;
            case CHICKEN -> 13;
            case ZOMBIE -> 54;
            case SKELETON -> 51;
            case CREEPER -> 50;
            case SPIDER -> 52;
            case VILLAGER -> 120;
            case WOLF -> 95;
            case CAT -> 96;
            default -> 0;
        };
    }
    
    private void writeUUID(ByteBuffer buffer, UUID uuid) {
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
    }
    
    private void writeVarInt(ByteBuffer buffer, int value) {
        while ((value & ~0x7F) != 0) {
            buffer.put((byte) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
        buffer.put((byte) value);
    }
}
