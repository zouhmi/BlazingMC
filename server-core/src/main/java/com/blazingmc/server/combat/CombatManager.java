package com.blazingmc.server.combat;

import com.blazingmc.chat.ConsoleLogger;
import com.blazingmc.protocol.handler.CombatManagerInterface;
import com.blazingmc.protocol.handler.PlayerInterface;
import com.blazingmc.server.BlazingServer;
import com.blazingmc.server.entity.SpawnManager;
import com.blazingmc.server.inventory.Inventory;
import com.blazingmc.server.inventory.ItemStack;
import com.blazingmc.server.player.ArmorManager;
import com.blazingmc.server.player.PlayerManager;
import com.blazingmc.server.player.ToolDurabilityManager;
import com.blazingmc.world.entity.Entity;
import com.blazingmc.world.entity.EntityType;
import com.blazingmc.world.entity.MobEntity;
import org.bukkit.Material;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

public class CombatManager implements CombatManagerInterface {
    private final BlazingServer server;
    private final Map<UUID, Long> invincibilityTicks;
    private final Random random;
    
    private static final int INVINCIBILITY_TICKS = 20;
    private static final double PLAYER_ATTACK_DAMAGE = 1.0;
    private static final double CRITICAL_MULTIPLIER = 1.5;
    private static final double SPRINT_MULTIPLIER = 1.3;
    
    public CombatManager(BlazingServer server) {
        this.server = server;
        this.invincibilityTicks = new HashMap<>();
        this.random = new Random();
    }
    
    @Override
    public void handleAttackEntity(PlayerInterface player, int entityId, int action) {
        if (player == null) return;
        
        PlayerInterface playerTarget = findPlayerTarget(entityId);
        if (playerTarget != null) {
            if (action == 2) {
                handleAttackPlayer(player, playerTarget);
            }
            return;
        }
        
        Entity target = server.getSpawnManager().getEntity(entityId);
        if (target == null || target.isDead() || !target.isValid()) {
            return;
        }
        
        switch (action) {
            case 1 -> handleInteract(player, target);
            case 2 -> handleAttack(player, target);
            default -> {}
        }
    }
    
    private PlayerInterface findPlayerTarget(int entityId) {
        for (PlayerInterface player : server.getPlayerManager().getOnlinePlayers()) {
            if (player.getEntityId() == entityId) {
                return player;
            }
        }
        return null;
    }
    
    private void handleInteract(PlayerInterface player, Entity target) {
        ConsoleLogger.debug(player.getUsername() + " interacted with " + target.getType().getName());
    }
    
    private void handleAttackPlayer(PlayerInterface attacker, PlayerInterface target) {
        if (attacker.getUuid().equals(target.getUuid()) ||
            !server.getAntiCheatManager().validateClickSpeed(attacker) ||
            !server.getAntiCheatManager().validateAttackReach(attacker, target.getX(), target.getY(), target.getZ())) {
            return;
        }
        
        long now = System.currentTimeMillis();
        Long lastHit = invincibilityTicks.get(target.getUuid());
        if (lastHit != null && now - lastHit < INVINCIBILITY_TICKS * 50L) {
            return;
        }
        
        double damage = calculateDamage(attacker, target.isOnGround());
        invincibilityTicks.put(target.getUuid(), now);
        if (target instanceof com.blazingmc.server.player.Player blazeTarget) {
            blazeTarget.damage(damage);
        }
        durabilityOnAttack(attacker);
        broadcastAttackAnimation(attacker);
    }
    
    private void handleAttack(PlayerInterface player, Entity target) {
        UUID targetUuid = target.getUniqueId();
        long now = System.currentTimeMillis();
        
        if (invincibilityTicks.containsKey(targetUuid)) {
            long lastHit = invincibilityTicks.get(targetUuid);
            if (now - lastHit < INVINCIBILITY_TICKS * 50) {
                return;
            }
        }
        
        if (!server.getAntiCheatManager().validateClickSpeed(player)) {
            return;
        }
        
        if (!server.getAntiCheatManager().validateAttackReach(player, 
                target.getLocation().getX(), target.getLocation().getY(), target.getLocation().getZ())) {
            return;
        }
        
        double damage = calculateDamage(player, target);
        
        invincibilityTicks.put(targetUuid, now);
        
        applyDamage(target, damage);
        
        durabilityOnAttack(player);
        
        broadcastAttackAnimation(player);
        broadcastEntityVelocity(target);
        
        if (target.isDead()) {
            handleEntityDeath(player, target);
        }
        
        ConsoleLogger.debug(player.getUsername() + " attacked " + target.getType().getName() + " for " + 
                          String.format("%.1f", damage) + " damage");
    }
    
    private double calculateDamage(PlayerInterface player, Entity target) {
        return calculateDamage(player, target.isOnGround());
    }
    
    private double calculateDamage(PlayerInterface player, boolean targetOnGround) {
        double damage = PLAYER_ATTACK_DAMAGE;
        
        if (player instanceof com.blazingmc.server.player.Player blazePlayer) {
            int heldSlot = Math.max(0, Math.min(8, blazePlayer.getMainHand()));
            Material weapon = blazePlayer.getInventory().getItem(heldSlot) != null ?
                blazePlayer.getInventory().getItem(heldSlot).getType() : null;
            if (weapon != null && ToolDurabilityManager.isTool(weapon)) {
                damage = ToolDurabilityManager.getToolDamage(weapon);
            }
        }
        
        if (player.isSprinting()) {
            damage *= SPRINT_MULTIPLIER;
        }
        
        if (!player.isOnGround() && targetOnGround) {
            damage *= CRITICAL_MULTIPLIER;
        }
        
        return damage;
    }
    
    private double applyArmorReduction(double rawDamage, PlayerInterface player) {
        if (!(player instanceof com.blazingmc.server.player.Player blazePlayer)) {
            return rawDamage;
        }
        
        Inventory inventory = blazePlayer.getInventory();
        Material helmet = getArmorMaterial(inventory, 39);
        Material chestplate = getArmorMaterial(inventory, 38);
        Material leggings = getArmorMaterial(inventory, 37);
        Material boots = getArmorMaterial(inventory, 36);
        
        return ArmorManager.applyArmorProtection(rawDamage, helmet, chestplate, leggings, boots);
    }
    
    private Material getArmorMaterial(Inventory inventory, int slot) {
        ItemStack item = inventory.getItem(slot);
        if (item != null && ArmorManager.isArmor(item.getType())) {
            return item.getType();
        }
        return null;
    }
    
    private void durabilityOnAttack(PlayerInterface player) {
        if (!(player instanceof com.blazingmc.server.player.Player blazePlayer)) {
            return;
        }
        
        Inventory inventory = blazePlayer.getInventory();
        int heldSlot = Math.max(0, Math.min(8, blazePlayer.getMainHand()));
        ItemStack weapon = inventory.getItem(heldSlot);
        
        if (weapon != null && !weapon.isUnbreakable() && ToolDurabilityManager.isTool(weapon.getType())) {
            weapon.setDurability((short) (weapon.getDurability() + 1));
            if (weapon.getDurability() >= ToolDurabilityManager.getToolDurability(weapon.getType())) {
                inventory.setItem(heldSlot, null);
                ConsoleLogger.debug(player.getUsername() + "'s " + weapon.getType().name() + " broke!");
            }
        }
    }
    
    private void applyDamage(Entity entity, double damage) {
        if (entity instanceof MobEntity mob) {
            int currentHealth = mob.getHealth();
            int newHealth = (int) Math.max(0, currentHealth - damage);
            mob.setHealth(newHealth);
        }
    }
    
    private void handleEntityDeath(PlayerInterface killer, Entity entity) {
        ConsoleLogger.debug(entity.getType().getName() + " killed by " + killer.getUsername());
        
        List<ItemDrop> drops = calculateDrops(entity);
        
        SpawnManager spawnManager = server.getSpawnManager();
        spawnManager.removeEntity(entity);
        
        for (ItemDrop drop : drops) {
            broadcastItemSpawn(drop);
        }
        
        if (killer instanceof com.blazingmc.server.player.Player player) {
            int xp = getXpDrop(entity);
            if (xp > 0) {
                player.getExperienceManager().addExperience(xp);
                player.getExperienceManager().sendExperienceUpdate(player);
            }
        }
    }
    
    private List<ItemDrop> calculateDrops(Entity entity) {
        List<ItemDrop> drops = new ArrayList<>();
        
        switch (entity.getType()) {
            case PIG -> {
                drops.add(new ItemDrop(319, random.nextInt(3) + 1));
                if (random.nextFloat() < 0.5f) {
                    drops.add(new ItemDrop(320, 1));
                }
            }
            case COW -> {
                drops.add(new ItemDrop(363, random.nextInt(3) + 1));
                if (random.nextFloat() < 0.5f) {
                    drops.add(new ItemDrop(334, 1));
                }
            }
            case SHEEP -> {
                drops.add(new ItemDrop(359, random.nextInt(3) + 1));
                if (random.nextFloat() < 0.5f) {
                    drops.add(new ItemDrop(334, 1));
                }
            }
            case CHICKEN -> {
                drops.add(new ItemDrop(365, random.nextInt(3) + 1));
                if (random.nextFloat() < 0.3f) {
                    drops.add(new ItemDrop(344, 1));
                }
            }
            case ZOMBIE -> {
                drops.add(new ItemDrop(367, random.nextInt(2) + 1));
                if (random.nextFloat() < 0.1f) {
                    drops.add(new ItemDrop(256, 1));
                }
            }
            case SKELETON -> {
                drops.add(new ItemDrop(287, random.nextInt(2) + 1));
                if (random.nextFloat() < 0.1f) {
                    drops.add(new ItemDrop(261, 1));
                }
            }
            case CREEPER -> {
                drops.add(new ItemDrop(288, random.nextInt(2) + 1));
            }
            default -> {}
        }
        
        return drops;
    }
    
    private int getXpDrop(Entity entity) {
        return switch (entity.getType()) {
            case PIG, COW, SHEEP, CHICKEN -> random.nextInt(3) + 1;
            case ZOMBIE, SKELETON -> random.nextInt(5) + 1;
            case CREEPER -> random.nextInt(5) + 2;
            default -> 0;
        };
    }
    
    private void broadcastAttackAnimation(PlayerInterface attacker) {
        ByteBuffer buffer = ByteBuffer.allocate(16).order(ByteOrder.BIG_ENDIAN);
        
        buffer.putInt(attacker.getEntityId());
        writeVarInt(buffer, 1);
        
        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        
        for (PlayerInterface player : server.getPlayerManager().getOnlinePlayers()) {
            player.sendPacket(0x03, data);
        }
    }
    
    private void broadcastEntityVelocity(Entity entity) {
        ByteBuffer buffer = ByteBuffer.allocate(32).order(ByteOrder.BIG_ENDIAN);
        
        buffer.putInt(entity.getEntityId());
        writeVarInt(buffer, (int) (entity.getVelocityX() * 8000));
        writeVarInt(buffer, (int) (entity.getVelocityY() * 8000));
        writeVarInt(buffer, (int) (entity.getVelocityZ() * 8000));
        
        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        
        for (PlayerInterface player : server.getPlayerManager().getOnlinePlayers()) {
            player.sendPacket(0x1C, data);
        }
    }
    
    private void broadcastItemSpawn(ItemDrop drop) {
        ByteBuffer buffer = ByteBuffer.allocate(64).order(ByteOrder.BIG_ENDIAN);
        
        writeVarInt(buffer, 1);
        writeUUID(buffer, UUID.randomUUID());
        writeVarInt(buffer, 57);
        buffer.putDouble(0);
        buffer.putDouble(64);
        buffer.putDouble(0);
        buffer.putFloat(0);
        buffer.putFloat(0);
        buffer.put((byte) 0);
        writeVarInt(buffer, 1);
        writeVarInt(buffer, drop.itemId());
        buffer.put((byte) 0);
        
        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        
        for (PlayerInterface player : server.getPlayerManager().getOnlinePlayers()) {
            player.sendPacket(0x01, data);
        }
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
    
    public record ItemDrop(int itemId, int amount) {}
}
