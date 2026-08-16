package com.blazingmc.server.block;

import com.blazingmc.chat.ConsoleLogger;
import com.blazingmc.world.World;
import org.bukkit.Material;

import java.util.*;

public class DoorManager {
    private final World world;
    private final Map<Long, Boolean> doorStates;
    
    public DoorManager(World world) {
        this.world = world;
        this.doorStates = new HashMap<>();
    }
    
    public void toggleDoor(int x, int y, int z) {
        Material block = world.getBlockAt(x, y, z);
        if (!isDoorMaterial(block)) return;
        
        long key = getKey(x, y, z);
        boolean isOpen = doorStates.getOrDefault(key, false);
        
        doorStates.put(key, !isOpen);
        
        setDoorVisualState(x, y, z, !isOpen);
        
        ConsoleLogger.debug("Door " + (!isOpen ? "opened" : "closed") + " at " + x + "," + y + "," + z);
    }
    
    public void openDoor(int x, int y, int z) {
        Material block = world.getBlockAt(x, y, z);
        if (!isDoorMaterial(block)) return;
        
        long key = getKey(x, y, z);
        if (!doorStates.getOrDefault(key, false)) {
            doorStates.put(key, true);
            setDoorVisualState(x, y, z, true);
        }
    }
    
    public void closeDoor(int x, int y, int z) {
        Material block = world.getBlockAt(x, y, z);
        if (!isDoorMaterial(block)) return;
        
        long key = getKey(x, y, z);
        if (doorStates.getOrDefault(key, false)) {
            doorStates.put(key, false);
            setDoorVisualState(x, y, z, false);
        }
    }
    
    public boolean isDoorOpen(int x, int y, int z) {
        return doorStates.getOrDefault(getKey(x, y, z), false);
    }
    
    private void setDoorVisualState(int x, int y, int z, boolean open) {
        Material current = world.getBlockAt(x, y, z);
        Material newState = getDoorOpenState(current, open);
        
        if (newState != null && newState != current) {
            world.setBlockAt(x, y, z, newState);
        }
    }
    
    private Material getDoorOpenState(Material material, boolean open) {
        return switch (material) {
            case OAK_DOOR -> open ? Material.OAK_DOOR : Material.OAK_DOOR;
            case SPRUCE_DOOR -> open ? Material.SPRUCE_DOOR : Material.SPRUCE_DOOR;
            case BIRCH_DOOR -> open ? Material.BIRCH_DOOR : Material.BIRCH_DOOR;
            case JUNGLE_DOOR -> open ? Material.JUNGLE_DOOR : Material.JUNGLE_DOOR;
            case ACACIA_DOOR -> open ? Material.ACACIA_DOOR : Material.ACACIA_DOOR;
            case DARK_OAK_DOOR -> open ? Material.DARK_OAK_DOOR : Material.DARK_OAK_DOOR;
            default -> null;
        };
    }
    
    public boolean isDoorMaterial(Material material) {
        return material == Material.OAK_DOOR || material == Material.SPRUCE_DOOR ||
               material == Material.BIRCH_DOOR || material == Material.JUNGLE_DOOR ||
               material == Material.ACACIA_DOOR || material == Material.DARK_OAK_DOOR ||
               material == Material.IRON_DOOR;
    }
    
    public boolean isTrapdoor(Material material) {
        return material == Material.OAK_TRAPDOOR || material == Material.SPRUCE_TRAPDOOR ||
               material == Material.BIRCH_TRAPDOOR || material == Material.JUNGLE_TRAPDOOR ||
               material == Material.ACACIA_TRAPDOOR || material == Material.DARK_OAK_TRAPDOOR;
    }
    
    public boolean isFenceGate(Material material) {
        return material == Material.OAK_FENCE_GATE || material == Material.SPRUCE_FENCE_GATE ||
               material == Material.BIRCH_FENCE_GATE || material == Material.JUNGLE_FENCE_GATE ||
               material == Material.ACACIA_FENCE_GATE || material == Material.DARK_OAK_FENCE_GATE;
    }
    
    public void onBlockBreak(int x, int y, int z, Material material) {
        if (isDoorMaterial(material)) {
            doorStates.remove(getKey(x, y, z));
        }
    }
    
    public void onBlockPlace(int x, int y, int z, Material material) {
        if (isDoorMaterial(material)) {
            doorStates.put(getKey(x, y, z), false);
        }
    }
    
    public int getDoorCount() {
        return doorStates.size();
    }
    
    private long getKey(int x, int y, int z) {
        return ((long) x << 40) | ((long) y << 20) | (z & 0xFFFFF);
    }
}
