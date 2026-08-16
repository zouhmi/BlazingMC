package com.blazingmc.server.player;

import com.blazingmc.chat.ConsoleLogger;
import com.blazingmc.protocol.handler.PlayerInterface;
import com.blazingmc.server.BlazingServer;
import com.blazingmc.server.chunk.ChunkSender;
import com.blazingmc.server.inventory.Inventory;
import com.blazingmc.server.inventory.ItemStack;
import com.blazingmc.protocol.codec.PacketEncoder;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import org.bukkit.Material;

import javax.crypto.Cipher;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

public class Player implements PlayerInterface {
    private final BlazingServer server;
    private final ChannelHandlerContext ctx;
    private final UUID uuid;
    private final String username;
    private final Cipher encryptCipher;
    private final Cipher decryptCipher;
    private final ChunkSender chunkSender;
    private final Inventory inventory;
    private final Inventory enderChest;
    private final ExperienceManager experienceManager;
    
    private double x, y, z;
    private float yaw, pitch;
    private boolean onGround;
    private int entityId;
    private int protocolVersion;
    private String locale;
    private int viewDistance;
    private boolean enabledChatColors;
    private int displaySkinParts;
    private int mainHand;
    private int gameMode;
    private int foodLevel;
    private float foodSaturation;
    private int experienceLevel;
    private float experienceProgress;
    private boolean sprinting;
    private boolean flying;
    private boolean AllowFlying;
    private boolean creativeMode;
    private boolean respawnScreen;
    private boolean reducedDebugInfo;
    private boolean enableRespawnScreen;
    private boolean isHardcore;
    private int ticksSinceLastPositionPacket;
    private long lastMovementTime;
    private double movementSpeed;
    private int starveTicks;
    private float foodExhaustion;
    private int foodExhaustionTicks;
    private int statUpdateTicks;
    private float health;
    private double lastValidX;
    private double lastValidY;
    private double lastValidZ;
    
    public Player(BlazingServer server, ChannelHandlerContext ctx, UUID uuid, String username,
                  Cipher encryptCipher, Cipher decryptCipher) {
        this.server = server;
        this.ctx = ctx;
        this.uuid = uuid;
        this.username = username;
        this.encryptCipher = encryptCipher;
        this.decryptCipher = decryptCipher;
        this.entityId = server.getPlayerManager().getNextEntityId();
        this.chunkSender = new ChunkSender(server.getWorld().getChunkManager());
        this.inventory = new Inventory(41, "Inventory", Inventory.InventoryType.PLAYER);
        this.enderChest = new Inventory(27, "Ender Chest", Inventory.InventoryType.ENDER_CHEST);
        this.experienceManager = new ExperienceManager();
        
        this.x = 0;
        this.y = 64;
        this.z = 0;
        this.yaw = 0;
        this.pitch = 0;
        this.onGround = true;
        this.gameMode = 0;
        this.foodLevel = 20;
        this.foodSaturation = 5.0f;
        this.experienceLevel = 0;
        this.experienceProgress = 0.0f;
        this.sprinting = false;
        this.flying = false;
        this.AllowFlying = false;
        this.creativeMode = false;
        this.respawnScreen = false;
        this.reducedDebugInfo = false;
        this.enableRespawnScreen = true;
        this.isHardcore = false;
        this.ticksSinceLastPositionPacket = 0;
        this.lastMovementTime = System.currentTimeMillis();
        this.movementSpeed = 0.1;
        this.starveTicks = 0;
        this.foodExhaustion = 0.0f;
        this.foodExhaustionTicks = 0;
        this.statUpdateTicks = 0;
        this.health = 20.0f;
        this.lastValidX = 0;
        this.lastValidY = 64;
        this.lastValidZ = 0;
        
        inventory.setItem(0, new ItemStack(Material.STONE_SWORD));
        inventory.setItem(1, new ItemStack(Material.DIAMOND_PICKAXE));
        inventory.setItem(2, new ItemStack(Material.DIAMOND_AXE));
        inventory.setItem(8, new ItemStack(Material.TORCH, 64));
        inventory.setItem(36, new ItemStack(Material.LEATHER_BOOTS));
        inventory.setItem(37, new ItemStack(Material.LEATHER_LEGGINGS));
        inventory.setItem(38, new ItemStack(Material.LEATHER_CHESTPLATE));
        inventory.setItem(39, new ItemStack(Material.LEATHER_HELMET));
        inventory.setItem(40, new ItemStack(Material.SHIELD));
    }
    
    public void sendPacket(int packetId, byte[] data) {
        if (!ctx.channel().isActive()) {
            return;
        }
        
        try {
            ctx.writeAndFlush(new PacketEncoder.PacketData(packetId, Unpooled.wrappedBuffer(data)));
        } catch (Exception e) {
            ConsoleLogger.error("Failed to send packet to " + username, e);
        }
    }
    
    public void sendChatMessage(String message) {
        ByteBuffer buffer = ByteBuffer.allocate(message.length() * 3 + 32).order(ByteOrder.BIG_ENDIAN);
        
        buffer.put((byte) 0);
        writeString(buffer, UUID.randomUUID().toString());
        writeString(buffer, username);
        writeString(buffer, message);
        writeString(buffer, "chat.type.text");
        
        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        
        sendPacket(0x30, data);
    }
    
    private void writeString(ByteBuffer buffer, String str) {
        byte[] bytes = str.getBytes();
        writeVarInt(buffer, bytes.length);
        buffer.put(bytes);
    }
    
    private void writeVarInt(ByteBuffer buffer, int value) {
        while ((value & ~0x7F) != 0) {
            buffer.put((byte) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
        buffer.put((byte) value);
    }
    
    public void disconnect(String reason) {
        ByteBuffer buffer = ByteBuffer.allocate(reason.length() * 3 + 32).order(ByteOrder.BIG_ENDIAN);
        writeString(buffer, reason);
        
        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        
        sendPacket(0x17, data);
        
        ctx.close();
        ConsoleLogger.quit(username);
    }
    
    public void tick() {
        ticksSinceLastPositionPacket++;
        
        if (ticksSinceLastPositionPacket > 20) {
            onGround = true;
        }
        
        tickFood();
        tickHealth();
        sendStatsIfNeeded();
    }
    
    private void tickFood() {
        if (gameMode == 1) return;
        
        if (sprinting && foodSaturation > 0) {
            foodSaturation = Math.max(0, foodSaturation - 0.1f);
        }
        
        if (foodLevel <= 0) {
            starveTicks++;
            if (starveTicks >= 80) {
                float health = getHealth();
                if (health > 1) {
                    setHealth((int) Math.max(1, health - 1));
                }
                starveTicks = 0;
            }
        } else {
            starveTicks = 0;
        }
        
        if (foodLevel >= 18 && getHealth() < 20 && foodExhaustion >= 4.0f) {
            foodExhaustion -= 4.0f;
            float health = getHealth();
            if (health < 20) {
                setHealth((int) Math.min(20, health + 1));
            }
        }
        
        if (foodSaturation <= 0 && foodLevel > 0) {
            foodExhaustionTicks++;
            if (foodExhaustionTicks >= 80) {
                foodLevel = Math.max(0, foodLevel - 1);
                foodExhaustionTicks = 0;
                sendFoodUpdate();
            }
        }
    }
    
    private void tickHealth() {
        if (gameMode == 1) return;
    }
    
    private void sendStatsIfNeeded() {
        statUpdateTicks++;
        if (statUpdateTicks >= 20) {
            statUpdateTicks = 0;
            sendFoodUpdate();
            sendHealthUpdate();
            experienceManager.sendExperienceUpdate(this);
        }
    }
    
    public void sendFoodUpdate() {
        ByteBuffer buffer = ByteBuffer.allocate(16).order(ByteOrder.BIG_ENDIAN);
        buffer.putFloat(foodLevel);
        buffer.putFloat(foodSaturation);
        
        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        
        sendPacket(0x54, data);
    }
    
    public void sendHealthUpdate() {
        float health = getHealth();
        
        ByteBuffer buffer = ByteBuffer.allocate(32).order(ByteOrder.BIG_ENDIAN);
        buffer.putFloat(health);
        writeVarInt(buffer, foodLevel);
        buffer.putFloat(foodExhaustion);
        
        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        
        sendPacket(0x60, data);
    }
    
    public void addFoodExhaustion(float amount) {
        foodExhaustion = Math.min(40.0f, foodExhaustion + amount);
    }
    
    public void eat(float hunger, float saturation, float exhaustion) {
        if (hunger <= 0) {
            return;
        }
        
        foodLevel = Math.min(20, foodLevel + (int) hunger);
        foodSaturation = Math.min(foodLevel, foodSaturation + saturation);
        foodExhaustion = Math.min(40.0f, foodExhaustion + exhaustion);
        sendFoodUpdate();
    }
    
    public void eat(float saturation, float exhaustion) {
        eat(1, saturation, exhaustion);
    }
    
    public void updatePosition(double x, double y, double z, boolean onGround) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.onGround = onGround;
        this.ticksSinceLastPositionPacket = 0;
    }
    
    public void updateRotation(float yaw, float pitch, boolean onGround) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
    }
    
    public void teleport(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        
        ByteBuffer buffer = ByteBuffer.allocate(64).order(ByteOrder.BIG_ENDIAN);
        
        buffer.putDouble(x);
        buffer.putDouble(y);
        buffer.putDouble(z);
        buffer.putFloat(yaw);
        buffer.putFloat(pitch);
        buffer.put((byte) 0);
        
        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        
        sendPacket(0x3C, data);
    }
    
    public void sendChunksAround() {
        int chunkX = (int) Math.floor(x) >> 4;
        int chunkZ = (int) Math.floor(z) >> 4;
        chunkSender.sendChunksAround(this, chunkX, chunkZ);
        chunkSender.unloadDistantChunks(this, chunkX, chunkZ);
    }
    
    public Inventory getInventory() {
        return inventory;
    }
    
    public Inventory getEnderChest() {
        return enderChest;
    }
    
    public ExperienceManager getExperienceManager() {
        return experienceManager;
    }
    
    public void openInventory(Inventory inventory) {
        ByteBuffer buffer = ByteBuffer.allocate(128).order(ByteOrder.BIG_ENDIAN);
        
        buffer.put((byte) 0);
        buffer.put((byte) inventory.getType().ordinal());
        writeString(buffer, inventory.getTitle());
        
        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        
        sendPacket(0x2E, data);
    }
    
    public float getHealth() {
        return health;
    }
    
    public void setHealth(float health) {
        this.health = Math.max(0, Math.min(20, health));
        sendHealthUpdate();
    }
    
    public void damage(double rawDamage) {
        if (rawDamage <= 0 || creativeMode || health <= 0) {
            return;
        }
        
        Material helmet = inventory.getItem(39) != null ? inventory.getItem(39).getType() : null;
        Material chestplate = inventory.getItem(38) != null ? inventory.getItem(38).getType() : null;
        Material leggings = inventory.getItem(37) != null ? inventory.getItem(37).getType() : null;
        Material boots = inventory.getItem(36) != null ? inventory.getItem(36).getType() : null;
        double damage = ArmorManager.applyArmorProtection(rawDamage, helmet, chestplate, leggings, boots);
        setHealth((float) Math.max(0, health - damage));
        damageArmorPiece(39);
        damageArmorPiece(38);
        damageArmorPiece(37);
        damageArmorPiece(36);
    }
    
    private void damageArmorPiece(int slot) {
        ItemStack armor = inventory.getItem(slot);
        if (armor == null || armor.isUnbreakable() || !ArmorManager.isArmor(armor.getType())) {
            return;
        }
        
        armor.setDurability((short) (armor.getDurability() + 1));
        if (armor.getDurability() >= ArmorManager.getArmorDurability(armor.getType())) {
            inventory.setItem(slot, null);
        }
    }
    
    public boolean isDead() {
        return health <= 0;
    }
    
    public BlazingServer getServer() { return server; }
    public ChannelHandlerContext getCtx() { return ctx; }
    public UUID getUuid() { return uuid; }
    public String getUsername() { return username; }
    public String getDisplayName() { return username; }
    public Cipher getEncryptCipher() { return encryptCipher; }
    public Cipher getDecryptCipher() { return decryptCipher; }
    public ChunkSender getChunkSender() { return chunkSender; }
    
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public double getZ() { return z; }
    public void setZ(double z) { this.z = z; }
    public float getYaw() { return yaw; }
    public void setYaw(float yaw) { this.yaw = yaw; }
    public float getPitch() { return pitch; }
    public void setPitch(float pitch) { this.pitch = pitch; }
    public boolean isOnGround() { return onGround; }
    public void setOnGround(boolean onGround) { this.onGround = onGround; }
    public int getEntityId() { return entityId; }
    public int getProtocolVersion() { return protocolVersion; }
    public void setProtocolVersion(int protocolVersion) { this.protocolVersion = protocolVersion; }
    public String getLocale() { return locale; }
    public void setLocale(String locale) { this.locale = locale; }
    public int getViewDistance() { return viewDistance; }
    public void setViewDistance(int viewDistance) { this.viewDistance = viewDistance; }
    public boolean isEnabledChatColors() { return enabledChatColors; }
    public void setEnabledChatColors(boolean enabledChatColors) { this.enabledChatColors = enabledChatColors; }
    public int getDisplaySkinParts() { return displaySkinParts; }
    public void setDisplaySkinParts(int displaySkinParts) { this.displaySkinParts = displaySkinParts; }
    public int getMainHand() { return mainHand; }
    public void setMainHand(int mainHand) { this.mainHand = Math.max(0, Math.min(8, mainHand)); }
    public int getGameMode() { return gameMode; }
    public void setGameMode(int gameMode) { this.gameMode = gameMode; }
    public int getFoodLevel() { return foodLevel; }
    public void setFoodLevel(int foodLevel) { this.foodLevel = foodLevel; }
    public float getFoodSaturation() { return foodSaturation; }
    public void setFoodSaturation(float foodSaturation) { this.foodSaturation = foodSaturation; }
    public int getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(int experienceLevel) { this.experienceLevel = experienceLevel; }
    public float getExperienceProgress() { return experienceProgress; }
    public void setExperienceProgress(float experienceProgress) { this.experienceProgress = experienceProgress; }
    public boolean isSprinting() { return sprinting; }
    public void setSprinting(boolean sprinting) { this.sprinting = sprinting; }
    public boolean isFlying() { return flying; }
    public void setFlying(boolean flying) { this.flying = flying; }
    public boolean isAllowFlying() { return AllowFlying; }
    public void setAllowFlying(boolean allowFlying) { AllowFlying = allowFlying; }
    public boolean isCreativeMode() { return creativeMode; }
    public void setCreativeMode(boolean creativeMode) { this.creativeMode = creativeMode; }
    public boolean isRespawnScreen() { return respawnScreen; }
    public void setRespawnScreen(boolean respawnScreen) { this.respawnScreen = respawnScreen; }
    public boolean isReducedDebugInfo() { return reducedDebugInfo; }
    public void setReducedDebugInfo(boolean reducedDebugInfo) { this.reducedDebugInfo = reducedDebugInfo; }
    public boolean isEnableRespawnScreen() { return enableRespawnScreen; }
    public void setEnableRespawnScreen(boolean enableRespawnScreen) { this.enableRespawnScreen = enableRespawnScreen; }
    public boolean isHardcore() { return isHardcore; }
    public void setHardcore(boolean hardcore) { isHardcore = hardcore; }
    
    public void setPosition(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.lastMovementTime = System.currentTimeMillis();
    }
    
    public void setRotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }
    
    public long getLastMovementTime() { return lastMovementTime; }
    public void setLastMovementTime(long time) { this.lastMovementTime = time; }
    
    public double getMovementSpeed() { return movementSpeed; }
    public void setMovementSpeed(double speed) { this.movementSpeed = speed; }
    
    public double getLastValidX() { return lastValidX; }
    public double getLastValidY() { return lastValidY; }
    public double getLastValidZ() { return lastValidZ; }
    public void setLastValidPosition(double x, double y, double z) {
        this.lastValidX = x;
        this.lastValidY = y;
        this.lastValidZ = z;
    }
}