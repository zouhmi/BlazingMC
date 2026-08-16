package com.blazingmc.server.block;

import com.blazingmc.chat.ConsoleLogger;
import com.blazingmc.protocol.handler.BlockManagerInterface;
import com.blazingmc.protocol.handler.PlayerInterface;
import com.blazingmc.server.BlazingServer;
import com.blazingmc.server.inventory.ItemStack;
import com.blazingmc.server.player.Player;
import com.blazingmc.world.World;
import com.blazingmc.world.chunk.Chunk;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BlockManager implements BlockManagerInterface {
    private final Map<UUID, BlockBreakSession> breakSessions;
    private final Map<Material, Float> blockHardness;
    private final Map<Material, Integer> blockBreakTimes;
    
    public BlockManager() {
        this.breakSessions = new ConcurrentHashMap<>();
        this.blockHardness = new HashMap<>();
        this.blockBreakTimes = new HashMap<>();
        initializeBlockProperties();
    }
    
    private void initializeBlockProperties() {
        blockHardness.put(Material.AIR, 0.0f);
        blockHardness.put(Material.STONE, 1.5f);
        blockHardness.put(Material.GRANITE, 1.5f);
        blockHardness.put(Material.DIORITE, 1.5f);
        blockHardness.put(Material.ANDESITE, 1.5f);
        blockHardness.put(Material.DIRT, 0.5f);
        blockHardness.put(Material.COBBLESTONE, 2.0f);
        blockHardness.put(Material.OAK_PLANKS, 2.0f);
        blockHardness.put(Material.OAK_LOG, 2.0f);
        blockHardness.put(Material.OAK_LEAVES, 0.2f);
        blockHardness.put(Material.GLASS, 0.3f);
        blockHardness.put(Material.DIAMOND_ORE, 3.0f);
        blockHardness.put(Material.IRON_ORE, 3.0f);
        blockHardness.put(Material.GOLD_ORE, 3.0f);
        blockHardness.put(Material.COAL_ORE, 3.0f);
        blockHardness.put(Material.EMERALD_ORE, 3.0f);
        blockHardness.put(Material.REDSTONE_ORE, 3.0f);
        blockHardness.put(Material.LAPIS_ORE, 3.0f);
        blockHardness.put(Material.OBSIDIAN, 50.0f);
        blockHardness.put(Material.BEDROCK, -1.0f);
        blockHardness.put(Material.CHEST, 2.5f);
        blockHardness.put(Material.ENDER_CHEST, 22.5f);
        blockHardness.put(Material.CRAFTING_TABLE, 2.5f);
        blockHardness.put(Material.FURNACE, 3.5f);
        blockHardness.put(Material.BREWING_STAND, 0.5f);
        blockHardness.put(Material.ANVIL, 5.0f);
        blockHardness.put(Material.BEACON, 3.0f);
        blockHardness.put(Material.HOPPER, 2.0f);
        blockHardness.put(Material.DISPENSER, 3.5f);
        blockHardness.put(Material.DROPPER, 3.5f);
        blockHardness.put(Material.PISTON, 1.5f);
        blockHardness.put(Material.STICKY_PISTON, 1.5f);
        blockHardness.put(Material.TORCH, 0.0f);
        blockHardness.put(Material.WALL_TORCH, 0.0f);
        blockHardness.put(Material.REDSTONE_TORCH, 0.0f);
        blockHardness.put(Material.REDSTONE_LAMP, 0.3f);
        blockHardness.put(Material.NOTE_BLOCK, 0.5f);
        blockHardness.put(Material.JUKEBOX, 2.0f);
        blockHardness.put(Material.ENCHANTING_TABLE, 5.0f);
        blockHardness.put(Material.END_PORTAL_FRAME, -1.0f);
        blockHardness.put(Material.DRAGON_EGG, 3.0f);
        blockHardness.put(Material.COMMAND_BLOCK, -1.0f);
        blockHardness.put(Material.STRUCTURE_BLOCK, -1.0f);
        blockHardness.put(Material.BARRIER, -1.0f);
        blockHardness.put(Material.SPAWNER, 5.0f);
        
        for (Material mat : Material.values()) {
            if (!blockBreakTimes.containsKey(mat)) {
                blockHardness.putIfAbsent(mat, 1.0f);
            }
        }
    }
    
    public void handleBlockBreak(PlayerInterface player, int x, int y, int z, int status) {
        if (player == null) return;
        
        UUID playerId = player.getUuid();
        
        switch (status) {
            case 0:
                handleDiggingStarted(player, x, y, z);
                break;
            case 1:
                handleDiggingCancelled(player, x, y, z);
                break;
            case 2:
                handleDiggingFinished(player, x, y, z);
                break;
            case 3:
                handleDropItemStack(player, x, y, z);
                break;
            case 4:
                handleDropItem(player, x, y, z);
                break;
            case 5:
                handleShootArrow(player, x, y, z);
                break;
            case 6:
                handleUseItem(player, x, y, z);
                break;
        }
    }
    
    private void handleDiggingStarted(PlayerInterface player, int x, int y, int z) {
        UUID playerId = player.getUuid();
        BlockBreakSession session = new BlockBreakSession(player, x, y, z);
        breakSessions.put(playerId, session);
        
        ConsoleLogger.debug("Block break started: " + x + ", " + y + ", " + z);
    }
    
    private void handleDiggingCancelled(PlayerInterface player, int x, int y, int z) {
        UUID playerId = player.getUuid();
        breakSessions.remove(playerId);
        
        sendBlockBreakReset(player, x, y, z);
        
        ConsoleLogger.debug("Block break cancelled: " + x + ", " + y + ", " + z);
    }
    
    private void handleDiggingFinished(PlayerInterface player, int x, int y, int z) {
        UUID playerId = player.getUuid();
        breakSessions.remove(playerId);
        
        World world = BlazingServer.getInstance().getWorld();
        Material blockType = world.getBlockAt(x, y, z);
        
        if (blockType == Material.BEDROCK || blockType == Material.END_PORTAL_FRAME || 
            blockType == Material.COMMAND_BLOCK || blockType == Material.STRUCTURE_BLOCK || 
            blockType == Material.BARRIER) {
            ConsoleLogger.debug("Cannot break block: " + blockType.name());
            return;
        }
        
        if (blockType != Material.AIR) {
            world.setBlockAt(x, y, z, Material.AIR);
            
            sendBlockChange(player, x, y, z, Material.AIR);
            sendBlockBreakAnimation(player, x, y, z, 10);
            
            ConsoleLogger.debug("Block broken: " + blockType.name() + " at " + x + ", " + y + ", " + z);
            
            sendBlockBreakSound(player, x, y, z, blockType);
        }
    }
    
    private void handleDropItemStack(PlayerInterface player, int x, int y, int z) {
        ConsoleLogger.debug("Drop item stack at: " + x + ", " + y + ", " + z);
    }
    
    private void handleDropItem(PlayerInterface player, int x, int y, int z) {
        ConsoleLogger.debug("Drop item at: " + x + ", " + y + ", " + z);
    }
    
    private void handleShootArrow(PlayerInterface player, int x, int y, int z) {
        if (!(player instanceof Player blazePlayer)) {
            return;
        }
        
        int heldSlot = Math.max(0, Math.min(8, blazePlayer.getMainHand()));
        ItemStack heldItem = blazePlayer.getInventory().getItem(heldSlot);
        if (heldItem == null) {
            return;
        }
        
        double yaw = Math.toRadians(blazePlayer.getYaw());
        double pitch = Math.toRadians(blazePlayer.getPitch());
        double horizontal = Math.cos(pitch);
        double velocityX = -Math.sin(yaw) * horizontal * 3.0;
        double velocityY = -Math.sin(pitch) * 3.0;
        double velocityZ = Math.cos(yaw) * horizontal * 3.0;
        
        if (heldItem.getType() == Material.BOW) {
            int arrowSlot = findArrowSlot(blazePlayer);
            if (arrowSlot < 0 && !blazePlayer.isCreativeMode()) {
                return;
            }
            if (arrowSlot >= 0) {
                ItemStack arrows = blazePlayer.getInventory().getItem(arrowSlot);
                arrows.removeAmount(1);
            }
            BlazingServer.getInstance().getProjectileManager().spawnArrow(player,
                blazePlayer.getX(), blazePlayer.getY() + 1.5, blazePlayer.getZ(),
                velocityX, velocityY, velocityZ, !blazePlayer.isOnGround());
            heldItem.setDurability((short) (heldItem.getDurability() + 1));
            if (heldItem.getDurability() >= 384) {
                blazePlayer.getInventory().setItem(heldSlot, null);
            }
        } else if (heldItem.getType() == Material.TRIDENT) {
            BlazingServer.getInstance().getProjectileManager().spawnTrident(player,
                blazePlayer.getX(), blazePlayer.getY() + 1.5, blazePlayer.getZ(),
                velocityX, velocityY, velocityZ);
            heldItem.setDurability((short) (heldItem.getDurability() + 1));
            if (heldItem.getDurability() >= 250) {
                blazePlayer.getInventory().setItem(heldSlot, null);
            }
        }
    }
    
    private int findArrowSlot(Player player) {
        for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item != null && item.getType() == Material.ARROW && !item.isEmpty()) {
                return slot;
            }
        }
        return -1;
    }
    
    private void handleUseItem(PlayerInterface player, int x, int y, int z) {
        ConsoleLogger.debug("Use item at: " + x + ", " + y + ", " + z);
    }
    
    public void handleBlockPlacement(PlayerInterface player, int hand, int x, int y, int z, 
                                    int face, float cursorX, float cursorY, float cursorZ) {
        if (player == null || hand != 0) return;
        
        World world = BlazingServer.getInstance().getWorld();
        Material clickedBlock = world.getBlockAt(x, y, z);
        
        if (isInteractiveBlock(clickedBlock)) {
            openContainer(player, clickedBlock, x, y, z);
            return;
        }
        
        int placeX = x;
        int placeY = y;
        int placeZ = z;
        
        switch (face) {
            case 0: placeY--; break;
            case 1: placeY++; break;
            case 2: placeZ--; break;
            case 3: placeZ++; break;
            case 4: placeX--; break;
            case 5: placeX++; break;
        }
        
        Material currentBlock = world.getBlockAt(placeX, placeY, placeZ);
        
        if (currentBlock != Material.AIR) {
            ConsoleLogger.debug("Cannot place block at " + placeX + ", " + placeY + ", " + placeZ + ": " + currentBlock.name());
            return;
        }
        
        Material placeBlock = Material.STONE;
        
        world.setBlockAt(placeX, placeY, placeZ, placeBlock);
        
        sendBlockChange(player, placeX, placeY, placeZ, placeBlock);
        
        ConsoleLogger.debug("Block placed: " + placeBlock.name() + " at " + placeX + ", " + placeY + ", " + placeZ);
        
        sendBlockPlaceSound(player, placeX, placeY, placeZ, placeBlock);
    }
    
    private boolean isInteractiveBlock(Material material) {
        return material == Material.CHEST || material == Material.ENDER_CHEST || 
               material == Material.FURNACE || material == Material.CRAFTING_TABLE ||
               material == Material.BREWING_STAND || material == Material.ANVIL ||
               material == Material.BEACON || material == Material.TRAPPED_CHEST ||
               material == Material.ENCHANTING_TABLE;
    }
    
    private void openContainer(PlayerInterface player, Material blockType, int x, int y, int z) {
        BlazingServer server = BlazingServer.getInstance();
        
        switch (blockType) {
            case CHEST, TRAPPED_CHEST -> server.getContainerManager().openChest(player, x, y, z);
            case ENDER_CHEST -> server.getContainerManager().openEnderChest(player, x, y, z);
            case FURNACE -> server.getContainerManager().openFurnace(player, x, y, z);
            case ENCHANTING_TABLE -> server.getContainerManager().openEnchanting(player, x, y, z);
            case CRAFTING_TABLE -> server.getContainerManager().openCraftingTable(player, x, y, z);
            default -> ConsoleLogger.debug("Interactive block not implemented: " + blockType.name());
        }
    }
    
    private void sendBlockChange(PlayerInterface player, int x, int y, int z, Material material) {
        player.sendPacket(0x09, new byte[]{(byte) x, (byte) y, (byte) z, (byte) material.ordinal()});
    }
    
    private void sendBlockBreakReset(PlayerInterface player, int x, int y, int z) {
        player.sendPacket(0x0C, new byte[]{(byte) x, (byte) y, (byte) z, (byte) 10});
    }
    
    private void sendBlockBreakAnimation(PlayerInterface player, int x, int y, int z, int stage) {
        player.sendPacket(0x0C, new byte[]{(byte) x, (byte) y, (byte) z, (byte) stage});
    }
    
    private void sendBlockBreakSound(PlayerInterface player, int x, int y, int z, Material material) {
        ConsoleLogger.debug("Playing break sound for: " + material.name());
    }
    
    private void sendBlockPlaceSound(PlayerInterface player, int x, int y, int z, Material material) {
        ConsoleLogger.debug("Playing place sound for: " + material.name());
    }
    
    public float getBlockHardness(Material material) {
        return blockHardness.getOrDefault(material, 1.0f);
    }
    
    public boolean isBreakable(Material material) {
        return material != Material.BEDROCK && material != Material.END_PORTAL_FRAME && 
               material != Material.COMMAND_BLOCK && material != Material.STRUCTURE_BLOCK && 
               material != Material.BARRIER;
    }
    
    public void cleanup() {
        breakSessions.clear();
    }
    
    private static class BlockBreakSession {
        final PlayerInterface player;
        final int x, y, z;
        final long startTime;
        
        BlockBreakSession(PlayerInterface player, int x, int y, int z) {
            this.player = player;
            this.x = x;
            this.y = y;
            this.z = z;
            this.startTime = System.currentTimeMillis();
        }
    }
}