package com.blazingmc.world.chunk;

import com.blazingmc.world.World;
import org.bukkit.Material;

import java.util.ArrayDeque;
import java.util.Queue;

public class FluidManager {
    private static final int MAX_WATER_FLOW = 7;
    private static final int MAX_LAVA_FLOW = 4;
    private static final int TICK_DELAY_WATER = 5;
    private static final int TICK_DELAY_LAVA = 30;
    
    private final World world;
    private final Queue<FluidTick> pendingTicks;
    
    public FluidManager(World world) {
        this.world = world;
        this.pendingTicks = new ArrayDeque<>();
    }
    
    public void tick() {
        Queue<FluidTick> currentBatch = new ArrayDeque<>(pendingTicks);
        pendingTicks.clear();
        
        for (FluidTick tick : currentBatch) {
            processFluidTick(tick);
        }
    }
    
    public void onBlockPlace(int x, int y, int z, Material material) {
        if (material == Material.WATER || material == Material.LAVA) {
            scheduleFluidSpread(x, y, z, material);
        }
    }
    
    public void onBlockBreak(int x, int y, int z, Material material) {
        if (material == Material.WATER || material == Material.LAVA) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        
                        int nx = x + dx;
                        int ny = y + dy;
                        int nz = z + dz;
                        
                        if (ny < -64 || ny > 319) continue;
                        
                        Material neighbor = world.getBlockAt(nx, ny, nz);
                        if (neighbor == Material.WATER || neighbor == Material.LAVA) {
                            scheduleFluidSpread(nx, ny, nz, neighbor);
                        }
                    }
                }
            }
        }
    }
    
    private void processFluidTick(FluidTick tick) {
        int x = tick.x();
        int y = tick.y();
        int z = tick.z();
        Material fluidType = tick.material();
        
        Material current = world.getBlockAt(x, y, z);
        if (current != fluidType && current != Material.AIR) {
            return;
        }
        
        int maxFlow = fluidType == Material.WATER ? MAX_WATER_FLOW : MAX_LAVA_FLOW;
        int currentLevel = getFluidLevel(x, y, z, fluidType);
        
        if (currentLevel <= 1) return;
        
        spreadDownward(x, y, z, fluidType, currentLevel);
        spreadHorizontal(x, y, z, fluidType, currentLevel, maxFlow);
    }
    
    private void spreadDownward(int x, int y, int z, Material fluidType, int currentLevel) {
        int belowY = y - 1;
        if (belowY < -64) return;
        
        Material below = world.getBlockAt(x, belowY, z);
        if (below == Material.AIR) {
            world.setBlockAt(x, belowY, z, fluidType);
            scheduleFluidSpread(x, belowY, z, fluidType);
        } else if (below == fluidType) {
            int belowLevel = getFluidLevel(x, belowY, z, fluidType);
            if (belowLevel < currentLevel) {
                world.setBlockAt(x, belowY, z, fluidType);
                scheduleFluidSpread(x, belowY, z, fluidType);
            }
        }
    }
    
    private void spreadHorizontal(int x, int y, int z, Material fluidType, int currentLevel, int maxFlow) {
        int[][] offsets = {{1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}};
        
        for (int[] offset : offsets) {
            int nx = x + offset[0];
            int nz = z + offset[2];
            
            if (nx < -30000000 || nx > 30000000 || nz < -30000000 || nz > 30000000) continue;
            
            Material neighbor = world.getBlockAt(nx, y, nz);
            if (neighbor == Material.AIR) {
                int newLevel = currentLevel - 1;
                if (newLevel >= 1) {
                    world.setBlockAt(nx, y, nz, fluidType);
                    int delay = fluidType == Material.WATER ? TICK_DELAY_WATER : TICK_DELAY_LAVA;
                    scheduleFluidSpread(nx, y, nz, fluidType, delay);
                }
            } else if (neighbor == fluidType) {
                int neighborLevel = getFluidLevel(nx, y, nz, fluidType);
                if (neighborLevel < currentLevel - 1) {
                    world.setBlockAt(nx, y, nz, fluidType);
                    int delay = fluidType == Material.WATER ? TICK_DELAY_WATER : TICK_DELAY_LAVA;
                    scheduleFluidSpread(nx, y, nz, fluidType, delay);
                }
            }
        }
    }
    
    private int getFluidLevel(int x, int y, int z, Material fluidType) {
        Material current = world.getBlockAt(x, y, z);
        if (current != fluidType) return 0;
        
        boolean hasSourceAbove = world.getBlockAt(x, y + 1, z) == fluidType;
        if (hasSourceAbove) return 8;
        
        return 8;
    }
    
    private void scheduleFluidSpread(int x, int y, int z, Material fluidType) {
        int delay = fluidType == Material.WATER ? TICK_DELAY_WATER : TICK_DELAY_LAVA;
        scheduleFluidSpread(x, y, z, fluidType, delay);
    }
    
    private void scheduleFluidSpread(int x, int y, int z, Material fluidType, int delay) {
        pendingTicks.add(new FluidTick(x, y, z, fluidType, delay));
    }
    
    public boolean canFluidFlow(int x, int y, int z, Material fluidType) {
        Material current = world.getBlockAt(x, y, z);
        return current == Material.AIR || current == fluidType;
    }
    
    public boolean isFluid(Material material) {
        return material == Material.WATER || material == Material.LAVA;
    }
    
    public boolean isWater(Material material) {
        return material == Material.WATER;
    }
    
    public boolean isLava(Material material) {
        return material == Material.LAVA;
    }
    
    public int getFluidHeight(int x, int y, int z) {
        Material block = world.getBlockAt(x, y, z);
        if (block == Material.WATER) return 8;
        if (block == Material.LAVA) return 8;
        return 0;
    }
    
    public record FluidTick(int x, int y, int z, Material material, int delay) {}
}
