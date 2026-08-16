package com.blazingmc.server.block;

import com.blazingmc.chat.ConsoleLogger;
import com.blazingmc.world.World;
import org.bukkit.Material;

import java.util.*;

public class RedstoneManager {
    private static final int MAX_REDSTONE_POWER = 15;
    private static final int PISTON_EXTEND_TICKS = 2;
    private static final int PISTON_RETRACT_TICKS = 2;
    
    private final World world;
    private final Map<Long, Integer> redstonePower;
    private final Queue<RedstoneTick> pendingTicks;
    
    public RedstoneManager(World world) {
        this.world = world;
        this.redstonePower = new HashMap<>();
        this.pendingTicks = new ArrayDeque<>();
    }
    
    public void tick() {
        Queue<RedstoneTick> currentBatch = new ArrayDeque<>(pendingTicks);
        pendingTicks.clear();
        
        for (RedstoneTick tick : currentBatch) {
            processTick(tick);
        }
    }
    
    private void processTick(RedstoneTick tick) {
        int x = tick.x();
        int y = tick.y();
        int z = tick.z();
        Material material = world.getBlockAt(x, y, z);
        
        switch (material) {
            case REDSTONE_TORCH, REDSTONE_WALL_TORCH -> propagateRedstonePower(x, y, z, MAX_REDSTONE_POWER);
            case REPEATER -> processRepeater(x, y, z);
            case COMPARATOR -> processComparator(x, y, z);
            case LEVER -> processLever(x, y, z);
            case STONE_PRESSURE_PLATE, WOODEN_PRESSURE_PLATE, IRON_PRESSURE_PLATE -> 
                processPressurePlate(x, y, z);
            case REDSTONE_LAMP -> processRedstoneLamp(x, y, z);
            case PISTON, STICKY_PISTON -> processPiston(x, y, z);
            default -> {}
        }
    }
    
    public int getRedstonePower(int x, int y, int z) {
        return redstonePower.getOrDefault(getKey(x, y, z), 0);
    }
    
    public void setRedstonePower(int x, int y, int z, int power) {
        long key = getKey(x, y, z);
        int oldPower = redstonePower.getOrDefault(key, 0);
        
        if (power != oldPower) {
            redstonePower.put(key, power);
            updateNeighbors(x, y, z);
        }
    }
    
    public boolean isPowered(int x, int y, int z) {
        return getRedstonePower(x, y, z) > 0;
    }
    
    public boolean isPoweredAdjacent(int x, int y, int z) {
        return isPowered(x + 1, y, z) || isPowered(x - 1, y, z) ||
               isPowered(x, y, z + 1) || isPowered(x, y, z - 1) ||
               isPowered(x, y + 1, z) || isPowered(x, y - 1, z);
    }
    
    private void propagateRedstonePower(int x, int y, int z, int power) {
        if (power <= 0) return;
        
        setRedstonePower(x, y, z, power);
        
        int[][] offsets = {{1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}, {0, 1, 0}, {0, -1, 0}};
        
        for (int[] offset : offsets) {
            int nx = x + offset[0];
            int ny = y + offset[1];
            int nz = z + offset[2];
            
            Material neighbor = world.getBlockAt(nx, ny, nz);
            int newPower = power - 1;
            
            if (neighbor == Material.REDSTONE_TORCH) {
                if (getRedstonePower(nx, ny, nz) < newPower) {
                    scheduleTick(nx, ny, nz, 1);
                }
            }
        }
    }
    
    private void processRepeater(int x, int y, int z) {
        int inputPower = getInputPower(x, y, z);
        int delay = getRepeaterDelay(x, y, z);
        
        int outputPower = inputPower > 0 ? MAX_REDSTONE_POWER : 0;
        
        setRedstonePower(x, y, z, outputPower);
    }
    
    private int getRepeaterDelay(int x, int y, int z) {
        return 2;
    }
    
    private void processComparator(int x, int y, int z) {
        int inputPower = getInputPower(x, y, z);
        setRedstonePower(x, y, z, inputPower);
    }
    
    private void processLever(int x, int y, int z) {
        Material block = world.getBlockAt(x, y, z);
        if (block == Material.LEVER) {
            setRedstonePower(x, y, z, MAX_REDSTONE_POWER);
        }
    }
    
    private void processPressurePlate(int x, int y, int z) {
        setRedstonePower(x, y, z, MAX_REDSTONE_POWER);
    }
    
    private void processRedstoneLamp(int x, int y, int z) {
        boolean powered = isPoweredAdjacent(x, y, z);
        Material newType = powered ? Material.REDSTONE_LAMP : Material.AIR;
        
        if (powered && world.getBlockAt(x, y, z) != Material.REDSTONE_LAMP) {
            world.setBlockAt(x, y, z, Material.REDSTONE_LAMP);
        }
    }
    
    private void processPiston(int x, int y, int z) {
        Material block = world.getBlockAt(x, y, z);
        if (block != Material.PISTON && block != Material.STICKY_PISTON) return;
        
        boolean powered = isPoweredAdjacent(x, y, z);
        
        if (powered) {
            extendPiston(x, y, z, block);
        } else {
            retractPiston(x, y, z, block);
        }
    }
    
    private void extendPiston(int x, int y, int z, Material pistonType) {
        int facing = getPistonFacing(x, y, z);
        int[] dir = getFacingDirection(facing);
        
        int headX = x + dir[0];
        int headY = y + dir[1];
        int headZ = z + dir[2];
        
        if (headY < -64 || headY > 319) return;
        
        Material target = world.getBlockAt(headX, headY, headZ);
        if (target == Material.AIR || target == Material.WATER || target == Material.LAVA) {
            world.setBlockAt(headX, headY, headZ, Material.STONE);
            ConsoleLogger.debug("Piston extended at " + x + "," + y + "," + z);
        }
    }
    
    private void retractPiston(int x, int y, int z, Material pistonType) {
        int facing = getPistonFacing(x, y, z);
        int[] dir = getFacingDirection(facing);
        
        int headX = x + dir[0];
        int headY = y + dir[1];
        int headZ = z + dir[2];
        
        if (headY < -64 || headY > 319) return;
        
        Material head = world.getBlockAt(headX, headY, headZ);
        if (head == Material.STONE) {
            world.setBlockAt(headX, headY, headZ, Material.AIR);
            
            if (pistonType == Material.STICKY_PISTON) {
                int pullX = headX + dir[0];
                int pullY = headY + dir[1];
                int pullZ = headZ + dir[2];
                
                if (pullY >= -64 && pullY <= 319) {
                    Material pullBlock = world.getBlockAt(pullX, pullY, pullZ);
                    if (canMoveBlock(pullBlock)) {
                        world.setBlockAt(pullX, pullY, pullZ, Material.AIR);
                        world.setBlockAt(headX, headY, headZ, pullBlock);
                    }
                }
            }
            
            ConsoleLogger.debug("Piston retracted at " + x + "," + y + "," + z);
        }
    }
    
    private boolean canMoveBlock(Material material) {
        return material != Material.BEDROCK && material != Material.END_PORTAL && 
               material != Material.END_PORTAL_FRAME && material != Material.DRAGON_EGG &&
               material != Material.COMMAND_BLOCK;
    }
    
    private int getPistonFacing(int x, int y, int z) {
        return 0;
    }
    
    private int[] getFacingDirection(int facing) {
        return switch (facing) {
            case 0 -> new int[]{0, 1, 0};
            case 1 -> new int[]{0, -1, 0};
            case 2 -> new int[]{0, 0, -1};
            case 3 -> new int[]{0, 0, 1};
            case 4 -> new int[]{-1, 0, 0};
            case 5 -> new int[]{1, 0, 0};
            default -> new int[]{0, 1, 0};
        };
    }
    
    private int getInputPower(int x, int y, int z) {
        int maxPower = 0;
        
        int[][] offsets = {{1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}, {0, 1, 0}, {0, -1, 0}};
        
        for (int[] offset : offsets) {
            int nx = x + offset[0];
            int ny = y + offset[1];
            int nz = z + offset[2];
            
            int power = getRedstonePower(nx, ny, nz);
            maxPower = Math.max(maxPower, power);
        }
        
        return maxPower;
    }
    
    private void updateNeighbors(int x, int y, int z) {
        int[][] offsets = {{1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}, {0, 1, 0}, {0, -1, 0}};
        
        for (int[] offset : offsets) {
            int nx = x + offset[0];
            int ny = y + offset[1];
            int nz = z + offset[2];
            
            Material neighbor = world.getBlockAt(nx, ny, nz);
            if (isRedstoneComponent(neighbor)) {
                scheduleTick(nx, ny, nz, 1);
            }
        }
    }
    
    private boolean isRedstoneComponent(Material material) {
        return material == Material.REPEATER || material == Material.COMPARATOR ||
               material == Material.REDSTONE_LAMP || material == Material.PISTON ||
               material == Material.STICKY_PISTON || material == Material.DISPENSER ||
               material == Material.DROPPER || material == Material.NOTE_BLOCK;
    }
    
    public void onBlockPlace(int x, int y, int z, Material material) {
        if (material == Material.REDSTONE_TORCH || material == Material.REDSTONE_WALL_TORCH) {
            propagateRedstonePower(x, y, z, MAX_REDSTONE_POWER);
        } else if (material == Material.LEVER || material == Material.STONE_PRESSURE_PLATE ||
                   material == Material.WOODEN_PRESSURE_PLATE || material == Material.IRON_PRESSURE_PLATE) {
            scheduleTick(x, y, z, 1);
        }
    }
    
    public void onBlockBreak(int x, int y, int z, Material material) {
        if (material == Material.REDSTONE_TORCH || material == Material.REDSTONE_WALL_TORCH) {
            setRedstonePower(x, y, z, 0);
        } else if (material == Material.LEVER) {
            setRedstonePower(x, y, z, 0);
        }
    }
    
    public void scheduleTick(int x, int y, int z, int delay) {
        pendingTicks.add(new RedstoneTick(x, y, z, delay));
    }
    
    public void setPowered(int x, int y, int z, boolean powered) {
        setRedstonePower(x, y, z, powered ? MAX_REDSTONE_POWER : 0);
    }
    
    public Map<Long, Integer> getRedstonePowerMap() {
        return Collections.unmodifiableMap(redstonePower);
    }
    
    private long getKey(int x, int y, int z) {
        return ((long) x << 40) | ((long) y << 20) | (z & 0xFFFFF);
    }
    
    public record RedstoneTick(int x, int y, int z, int delay) {}
}
