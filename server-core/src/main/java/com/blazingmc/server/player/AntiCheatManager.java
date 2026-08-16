package com.blazingmc.server.player;

import com.blazingmc.chat.ConsoleLogger;
import com.blazingmc.protocol.handler.AntiCheatManagerInterface;
import com.blazingmc.protocol.handler.PlayerInterface;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

public class AntiCheatManager implements AntiCheatManagerInterface {
    private static final double MAX_MOVEMENT_SPEED = 0.45;
    private static final double SPRINT_SPEED = 0.55;
    private static final double FLY_SPEED = 0.5;
    private static final double JUMP_VELOCITY = 0.42;
    private static final double GRAVITY = 0.08;
    private static final double MAX_ATTACK_REACH = 5.5;
    private static final double BLOCK_REACH = 5.0;
    private static final double INTERACT_REACH = 5.5;
    private static final double PICKUP_REACH = 3.5;
    private static final long MOVEMENT_CHECK_INTERVAL = 50;
    private static final int MAX_VIOLATIONS = 5;
    private static final long VIOLATION_TIMEOUT = 30000;
    private static final int MAX_CLICK_SPEED = 20;
    private static final double MAX_BLOCK_BREAK_SPEED = 1.0;
    private static final double FAST_PLACE_INTERVAL = 50;
    
    private final PlayerManager playerManager;
    private final Map<UUID, List<Long>> clickTimestamps;
    private final Map<UUID, Integer> violationCount;
    private final Map<UUID, Long> lastViolationTime;
    private final Map<UUID, Long> lastBlockPlaceTime;
    private final Map<UUID, Double> lastHealth;
    
    public AntiCheatManager(PlayerManager playerManager) {
        this.playerManager = playerManager;
        this.clickTimestamps = new HashMap<>();
        this.violationCount = new HashMap<>();
        this.lastViolationTime = new HashMap<>();
        this.lastBlockPlaceTime = new HashMap<>();
        this.lastHealth = new HashMap<>();
    }
    
    public boolean validateMovement(PlayerInterface player, double newX, double newY, double newZ) {
        if (player == null) return true;
        
        long now = System.currentTimeMillis();
        long lastMoveTime = player.getLastMovementTime();
        
        if (lastMoveTime == 0) {
            player.setLastMovementTime(now);
            player.setLastValidPosition(newX, newY, newZ);
            return true;
        }
        
        long timeDiff = now - lastMoveTime;
        if (timeDiff < MOVEMENT_CHECK_INTERVAL) {
            return true;
        }
        
        double dx = newX - player.getX();
        double dy = newY - player.getY();
        double dz = newZ - player.getZ();
        
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        double totalDistance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        
        double speed = totalDistance / (timeDiff / 1000.0);
        
        boolean isFlying = !player.isOnGround() && dy > 0;
        double maxSpeed = isFlying ? FLY_SPEED : (player.isSprinting() ? SPRINT_SPEED : MAX_MOVEMENT_SPEED);
        
        if (speed > maxSpeed * 20) {
            ConsoleLogger.warn("Movement speed violation from " + player.getUsername() + ": " + 
                                String.format("%.2f", speed) + " blocks/sec (max: " + 
                                String.format("%.2f", maxSpeed * 20) + ")");
            onViolation(player, "speed");
            revertPosition(player);
            return false;
        }
        
        if (horizontalDistance > 5.0) {
            ConsoleLogger.warn("Horizontal distance violation from " + player.getUsername() + ": " + 
                                String.format("%.2f", horizontalDistance) + " blocks");
            onViolation(player, "horizontal-distance");
            revertPosition(player);
            return false;
        }
        
        if (Math.abs(dy) > 1.5 && player.isOnGround()) {
            ConsoleLogger.warn("Vertical movement while grounded from " + player.getUsername() + ": " + 
                                String.format("%.2f", dy) + " blocks");
            onViolation(player, "vertical-grounded");
            revertPosition(player);
            return false;
        }
        
        if (!isFlying && dy > JUMP_VELOCITY + 0.1 && player.isOnGround()) {
            ConsoleLogger.warn("Invalid jump from " + player.getUsername() + ": " + 
                                String.format("%.2f", dy) + " blocks");
            onViolation(player, "jump");
            revertPosition(player);
            return false;
        }
        
        if (speed < 0.01 && timeDiff > 1000) {
            player.setLastValidPosition(newX, newY, newZ);
        } else if (speed > 0.1) {
            player.setLastValidPosition(newX, newY, newZ);
        }
        
        return true;
    }
    
    public boolean validateRotation(PlayerInterface player, float newYaw, float newPitch) {
        if (player == null) return true;
        
        if (newPitch < -90 || newPitch > 90) {
            ConsoleLogger.warn("Invalid pitch from " + player.getUsername() + ": " + newPitch);
            onViolation(player, "pitch");
            return false;
        }
        
        float yawDiff = Math.abs(newYaw - player.getYaw());
        if (yawDiff > 180) yawDiff = 360 - yawDiff;
        
        if (yawDiff > 90) {
            ConsoleLogger.warn("Rotation speed violation from " + player.getUsername() + ": " + 
                                String.format("%.1f", yawDiff) + " degrees");
            onViolation(player, "rotation-speed");
            return false;
        }
        
        return true;
    }
    
    public boolean validateFlight(PlayerInterface player, double newY, boolean onGround) {
        if (player == null) return true;
        
        if (player.isCreativeMode() || player.isAllowFlying()) {
            return true;
        }
        
        double lastY = player.getLastValidY();
        double verticalMove = newY - lastY;
        
        if (!onGround && verticalMove > JUMP_VELOCITY + 0.05) {
            ConsoleLogger.warn("Flight attempt from " + player.getUsername() + ": " + 
                                String.format("%.2f", verticalMove) + " blocks up");
            onViolation(player, "flight");
            revertPosition(player);
            return false;
        }
        
        if (verticalMove < -3.0) {
            ConsoleLogger.warn("Invalid fall from " + player.getUsername() + ": " + 
                                String.format("%.2f", verticalMove) + " blocks");
            return false;
        }
        
        return true;
    }
    
    public boolean validateAttackReach(PlayerInterface player, double targetX, double targetY, double targetZ) {
        if (player == null) return true;
        
        double dx = targetX - player.getX();
        double dy = targetY - player.getY();
        double dz = targetZ - player.getZ();
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        
        if (distance > MAX_ATTACK_REACH) {
            ConsoleLogger.warn("Attack reach violation from " + player.getUsername() + ": " + 
                                String.format("%.2f", distance) + " blocks (max: " + MAX_ATTACK_REACH + ")");
            onViolation(player, "attack-reach");
            return false;
        }
        
        return true;
    }
    
    public boolean validateClickSpeed(PlayerInterface player) {
        if (player == null) return true;
        
        UUID uuid = player.getUuid();
        long now = System.currentTimeMillis();
        
        clickTimestamps.computeIfAbsent(uuid, k -> new ArrayList<>()).add(now);
        
        List<Long> timestamps = clickTimestamps.get(uuid);
        timestamps.removeIf(t -> now - t > 1000);
        
        if (timestamps.size() > MAX_CLICK_SPEED) {
            ConsoleLogger.warn("Click speed violation from " + player.getUsername() + ": " + 
                                timestamps.size() + " clicks/sec");
            onViolation(player, "click-speed");
            return false;
        }
        
        return true;
    }
    
    public boolean validateBlockReach(PlayerInterface player, int x, int y, int z) {
        if (player == null) return true;
        
        double dx = x + 0.5 - player.getX();
        double dy = y + 0.5 - player.getY();
        double dz = z + 0.5 - player.getZ();
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        
        if (distance > BLOCK_REACH) {
            ConsoleLogger.warn("Block reach violation from " + player.getUsername() + ": " + 
                                String.format("%.2f", distance) + " blocks (max: " + BLOCK_REACH + ")");
            onViolation(player, "block-reach");
            return false;
        }
        
        return true;
    }
    
    public boolean validateInteractReach(PlayerInterface player, double targetX, double targetY, double targetZ) {
        if (player == null) return true;
        
        double dx = targetX - player.getX();
        double dy = targetY - player.getY();
        double dz = targetZ - player.getZ();
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        
        if (distance > INTERACT_REACH) {
            ConsoleLogger.warn("Interact reach violation from " + player.getUsername() + ": " + 
                                String.format("%.2f", distance) + " blocks (max: " + INTERACT_REACH + ")");
            onViolation(player, "interact-reach");
            return false;
        }
        
        return true;
    }
    
    public boolean validateBlockPlaceSpeed(PlayerInterface player) {
        if (player == null) return true;
        
        UUID uuid = player.getUuid();
        long now = System.currentTimeMillis();
        
        Long lastPlace = lastBlockPlaceTime.get(uuid);
        if (lastPlace != null && now - lastPlace < FAST_PLACE_INTERVAL) {
            ConsoleLogger.warn("Fast place violation from " + player.getUsername() + ": " + 
                                (now - lastPlace) + "ms interval");
            onViolation(player, "fast-place");
            return false;
        }
        
        lastBlockPlaceTime.put(uuid, now);
        return true;
    }
    
    public boolean validateBlockBreakSpeed(PlayerInterface player, int x, int y, int z) {
        if (player == null) return true;
        
        if (!validateBlockReach(player, x, y, z)) {
            return false;
        }
        
        return true;
    }
    
    public boolean validateHealthUpdate(PlayerInterface player, float newHealth) {
        if (player == null) return true;
        
        UUID uuid = player.getUuid();
        Double oldHealth = lastHealth.get(uuid);
        
        if (oldHealth != null) {
            float healthDiff = (float) Math.abs(newHealth - oldHealth);
            
            if (healthDiff > 10.0f) {
                ConsoleLogger.warn("Suspicious health change from " + player.getUsername() + ": " + 
                                    String.format("%.1f", oldHealth) + " -> " + String.format("%.1f", newHealth));
                onViolation(player, "health-update");
                return false;
            }
        }
        
        lastHealth.put(uuid, (double) newHealth);
        return true;
    }
    
    public boolean validateItemPickup(PlayerInterface player, double itemX, double itemY, double itemZ) {
        if (player == null) return true;
        
        double dx = itemX - player.getX();
        double dy = itemY - player.getY();
        double dz = itemZ - player.getZ();
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        
        if (distance > PICKUP_REACH) {
            ConsoleLogger.warn("Item pickup reach violation from " + player.getUsername() + ": " + 
                                String.format("%.2f", distance) + " blocks (max: " + PICKUP_REACH + ")");
            onViolation(player, "pickup-reach");
            return false;
        }
        
        return true;
    }
    
    public boolean validateEntityInteraction(PlayerInterface player, double entityX, double entityY, double entityZ) {
        if (player == null) return true;
        
        return validateInteractReach(player, entityX, entityY, entityZ);
    }
    
    public boolean validateFishingRod(PlayerInterface player, double targetX, double targetY, double targetZ) {
        if (player == null) return true;
        
        double dx = targetX - player.getX();
        double dy = targetY - player.getY();
        double dz = targetZ - player.getZ();
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        
        if (distance > 6.0) {
            ConsoleLogger.warn("Fishing rod reach violation from " + player.getUsername() + ": " + 
                                String.format("%.2f", distance) + " blocks (max: 6.0)");
            onViolation(player, "fishing-reach");
            return false;
        }
        
        return true;
    }
    
    public boolean validateProjectileShoot(PlayerInterface player, double targetX, double targetY, double targetZ) {
        if (player == null) return true;
        
        return validateInteractReach(player, targetX, targetY, targetZ);
    }
    
    public void onViolation(PlayerInterface player, String type) {
        UUID uuid = player.getUuid();
        int violations = violationCount.getOrDefault(uuid, 0) + 1;
        violationCount.put(uuid, violations);
        lastViolationTime.put(uuid, System.currentTimeMillis());
        
        ConsoleLogger.warn("Anti-cheat violation #" + violations + " from " + player.getUsername() + 
                            " [" + type + "]");
        
        if (violations >= MAX_VIOLATIONS) {
            ConsoleLogger.warn("Kicking " + player.getUsername() + " for excessive violations");
            player.disconnect("{\"text\":\"Kicked for suspicious activity.\",\"color\":\"red\"}");
        }
    }
    
    public void revertPosition(PlayerInterface player) {
        double x = player.getLastValidX();
        double y = player.getLastValidY();
        double z = player.getLastValidZ();
        
        ByteBuffer buffer = ByteBuffer.allocate(64).order(ByteOrder.BIG_ENDIAN);
        buffer.putDouble(x);
        buffer.putDouble(y);
        buffer.putDouble(z);
        buffer.putFloat(0f);
        buffer.putFloat(0f);
        buffer.put((byte) 0);
        writeVarInt(buffer, 0);
        
        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        
        player.sendPacket(0x40, data);
        
        ConsoleLogger.debug("Reverted " + player.getUsername() + " to " + 
                          String.format("%.1f", x) + ", " + String.format("%.1f", y) + ", " + 
                          String.format("%.1f", z));
    }
    
    private void writeVarInt(ByteBuffer buffer, int value) {
        while ((value & ~0x7F) != 0) {
            buffer.put((byte) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
        buffer.put((byte) value);
    }
}
