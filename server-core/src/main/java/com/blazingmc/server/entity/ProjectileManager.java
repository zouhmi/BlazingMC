package com.blazingmc.server.entity;

import com.blazingmc.chat.ConsoleLogger;
import com.blazingmc.protocol.handler.PlayerInterface;
import com.blazingmc.server.BlazingServer;
import com.blazingmc.server.player.Player;
import com.blazingmc.world.entity.Entity;
import com.blazingmc.world.entity.MobEntity;
import org.bukkit.Material;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

public class ProjectileManager {
    private static final int MAX_AGE_TICKS = 1200;
    private static final double ENTITY_HIT_RADIUS = 1.0;
    private final BlazingServer server;
    private final Map<Integer, Projectile> projectiles;
    private int nextProjectileId;

    public ProjectileManager(BlazingServer server) {
        this.server = server;
        this.projectiles = new HashMap<>();
        this.nextProjectileId = 10000;
    }

    public void tick() {
        Iterator<Map.Entry<Integer, Projectile>> iterator = projectiles.entrySet().iterator();
        while (iterator.hasNext()) {
            Projectile projectile = iterator.next().getValue();
            projectile.tick();

            if (!projectile.isDead() && projectile.getAge() <= MAX_AGE_TICKS) {
                if (isInsideSolidBlock(projectile)) {
                    projectile.setDead(true);
                } else {
                    checkEntityCollisions(projectile);
                }
            }

            if (projectile.isDead() || projectile.getAge() > MAX_AGE_TICKS) {
                iterator.remove();
                broadcastRemoveProjectile(projectile);
            } else {
                broadcastProjectileMotion(projectile);
            }
        }
    }

    public void spawnArrow(PlayerInterface shooter, double x, double y, double z,
                           double velocityX, double velocityY, double velocityZ, boolean critical) {
        if (shooter == null) {
            return;
        }
        spawn(new Projectile(nextProjectileId++, ProjectileType.ARROW, shooter.getEntityId(),
            x, y, z, velocityX, velocityY, velocityZ, critical));
    }

    public void spawnTrident(PlayerInterface shooter, double x, double y, double z,
                             double velocityX, double velocityY, double velocityZ) {
        if (shooter == null) {
            return;
        }
        spawn(new Projectile(nextProjectileId++, ProjectileType.TRIDENT, shooter.getEntityId(),
            x, y, z, velocityX, velocityY, velocityZ, false));
    }

    private void spawn(Projectile projectile) {
        projectiles.put(projectile.getEntityId(), projectile);
        broadcastSpawnProjectile(projectile);
    }

    private boolean isInsideSolidBlock(Projectile projectile) {
        Material material = server.getWorld().getBlockAt(
            (int) Math.floor(projectile.getX()),
            (int) Math.floor(projectile.getY()),
            (int) Math.floor(projectile.getZ()));
        return material != Material.AIR && material != Material.WATER && material != Material.LAVA;
    }

    private void checkEntityCollisions(Projectile projectile) {
        for (Entity entity : server.getSpawnManager().getEntities()) {
            if (entity.getEntityId() == projectile.getShooterId() || entity.isDead()) {
                continue;
            }

            if (distanceSquared(entity.getLocation().getX(), entity.getLocation().getY(),
                    entity.getLocation().getZ(), projectile.getX(), projectile.getY(), projectile.getZ())
                    <= ENTITY_HIT_RADIUS * ENTITY_HIT_RADIUS) {
                double damage = calculateProjectileDamage(projectile);
                applyProjectileDamage(entity, damage);
                projectile.setDead(true);
                ConsoleLogger.debug("Projectile hit " + entity.getType().getName() + " for " + damage + " damage");
                return;
            }
        }

        for (PlayerInterface playerInterface : server.getPlayerManager().getOnlinePlayers()) {
            if (playerInterface.getEntityId() == projectile.getShooterId()) {
                continue;
            }

            if (distanceSquared(playerInterface.getX(), playerInterface.getY(), playerInterface.getZ(),
                    projectile.getX(), projectile.getY(), projectile.getZ())
                    <= ENTITY_HIT_RADIUS * ENTITY_HIT_RADIUS) {
                if (playerInterface instanceof Player player) {
                    player.damage(calculateProjectileDamage(projectile));
                    projectile.setDead(true);
                    ConsoleLogger.debug("Projectile hit player " + player.getUsername());
                }
                return;
            }
        }
    }

    private double distanceSquared(double firstX, double firstY, double firstZ,
                                   double secondX, double secondY, double secondZ) {
        double dx = firstX - secondX;
        double dy = firstY - secondY;
        double dz = firstZ - secondZ;
        return dx * dx + dy * dy + dz * dz;
    }

    private double calculateProjectileDamage(Projectile projectile) {
        return switch (projectile.getType()) {
            case ARROW -> projectile.isCritical() ? 6.0 : 2.0;
            case TRIDENT -> 8.0;
            default -> 1.0;
        };
    }

    private void applyProjectileDamage(Entity entity, double damage) {
        if (entity instanceof MobEntity mob) {
            int newHealth = (int) Math.max(0, mob.getHealth() - damage);
            mob.setHealth(newHealth);
            if (mob.isDead()) {
                server.getSpawnManager().removeEntity(entity);
            }
        }
    }

    private void broadcastSpawnProjectile(Projectile projectile) {
        ByteBuffer buffer = ByteBuffer.allocate(96).order(ByteOrder.BIG_ENDIAN);
        writeVarInt(buffer, projectile.getEntityId());
        writeUuid(buffer, projectile.getUuid());
        writeVarInt(buffer, getEntityTypeId(projectile.getType()));
        buffer.putDouble(projectile.getX());
        buffer.putDouble(projectile.getY());
        buffer.putDouble(projectile.getZ());
        buffer.put(angleToByte(projectile.getPitch()));
        buffer.put(angleToByte(projectile.getYaw()));
        buffer.put(angleToByte(projectile.getYaw()));
        writeVarInt(buffer, 0);
        putVelocity(buffer, projectile.getVelocityX());
        putVelocity(buffer, projectile.getVelocityY());
        putVelocity(buffer, projectile.getVelocityZ());
        sendToPlayers(0x01, buffer);
    }

    private int getEntityTypeId(ProjectileType type) {
        return switch (type) {
            case ARROW -> 2;
            case TRIDENT -> 94;
            default -> 0;
        };
    }

    private void broadcastProjectileMotion(Projectile projectile) {
        ByteBuffer buffer = ByteBuffer.allocate(32).order(ByteOrder.BIG_ENDIAN);
        writeVarInt(buffer, projectile.getEntityId());
        putVelocity(buffer, projectile.getVelocityX());
        putVelocity(buffer, projectile.getVelocityY());
        putVelocity(buffer, projectile.getVelocityZ());
        sendToPlayers(0x1C, buffer);
    }

    private void broadcastRemoveProjectile(Projectile projectile) {
        ByteBuffer buffer = ByteBuffer.allocate(16).order(ByteOrder.BIG_ENDIAN);
        writeVarInt(buffer, 1);
        writeVarInt(buffer, projectile.getEntityId());
        sendToPlayers(0x3A, buffer);
    }

    private void sendToPlayers(int packetId, ByteBuffer buffer) {
        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        for (PlayerInterface player : server.getPlayerManager().getOnlinePlayers()) {
            player.sendPacket(packetId, data);
        }
    }

    private void putVelocity(ByteBuffer buffer, double velocity) {
        double clamped = Math.max(-3.9, Math.min(3.9, velocity));
        buffer.putShort((short) Math.round(clamped * 8000.0));
    }

    private byte angleToByte(double angle) {
        return (byte) Math.round(angle * 256.0 / 360.0);
    }

    private void writeVarInt(ByteBuffer buffer, int value) {
        while ((value & ~0x7F) != 0) {
            buffer.put((byte) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
        buffer.put((byte) value);
    }

    private void writeUuid(ByteBuffer buffer, UUID uuid) {
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
    }

    public Projectile getProjectile(int entityId) {
        return projectiles.get(entityId);
    }

    public Collection<Projectile> getProjectiles() {
        return Collections.unmodifiableCollection(new ArrayList<>(projectiles.values()));
    }

    public static class Projectile {
        private final int entityId;
        private final UUID uuid;
        private final ProjectileType type;
        private final int shooterId;
        private double x;
        private double y;
        private double z;
        private double velocityX;
        private double velocityY;
        private double velocityZ;
        private boolean critical;
        private boolean dead;
        private int age;

        public Projectile(int entityId, ProjectileType type, int shooterId,
                          double x, double y, double z,
                          double velocityX, double velocityY, double velocityZ, boolean critical) {
            this.entityId = entityId;
            this.uuid = UUID.randomUUID();
            this.type = type;
            this.shooterId = shooterId;
            this.x = x;
            this.y = y;
            this.z = z;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.velocityZ = velocityZ;
            this.critical = critical;
        }

        public void tick() {
            x += velocityX;
            y += velocityY;
            z += velocityZ;
            velocityY -= 0.05;
            velocityX *= 0.99;
            velocityY *= 0.99;
            velocityZ *= 0.99;
            age++;
        }

        public int getEntityId() { return entityId; }
        public UUID getUuid() { return uuid; }
        public ProjectileType getType() { return type; }
        public int getShooterId() { return shooterId; }
        public double getX() { return x; }
        public double getY() { return y; }
        public double getZ() { return z; }
        public double getVelocityX() { return velocityX; }
        public double getVelocityY() { return velocityY; }
        public double getVelocityZ() { return velocityZ; }
        public double getPitch() { return Math.toDegrees(Math.atan2(-velocityY, Math.hypot(velocityX, velocityZ))); }
        public double getYaw() { return Math.toDegrees(Math.atan2(-velocityX, velocityZ)); }
        public boolean isCritical() { return critical; }
        public boolean isDead() { return dead; }
        public int getAge() { return age; }
        public void setDead(boolean dead) { this.dead = dead; }
    }

    public enum ProjectileType {
        ARROW,
        TRIDENT,
        SNOWBALL,
        ENDER_PEARL,
        EGG
    }
}
