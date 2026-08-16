package com.blazingmc.world.chunk;

import com.blazingmc.world.World;
import org.bukkit.Material;

import java.util.ArrayDeque;
import java.util.Queue;

public class LightEngine {
    private static final int MAX_LIGHT_LEVEL = 15;
    private static final int LIGHT_DECAY = 1;
    
    private final World world;
    
    public LightEngine(World world) {
        this.world = world;
    }
    
    public void calculateChunkLighting(Chunk chunk) {
        calculateSkyLight(chunk);
        calculateBlockLight(chunk);
    }
    
    private void calculateSkyLight(Chunk chunk) {
        for (int x = 0; x < Chunk.WIDTH; x++) {
            for (int z = 0; z < Chunk.WIDTH; z++) {
                int height = chunk.getHeightmap(x, z);
                
                for (int y = Chunk.HEIGHT - 1; y >= 0; y--) {
                    if (y > height) {
                        chunk.setSkyLight(x, y, z, MAX_LIGHT_LEVEL);
                    } else {
                        Material block = chunk.getBlock(x, y, z);
                        if (block == Material.AIR || block == Material.WATER || block == Material.LAVA) {
                            if (y == height) {
                                chunk.setSkyLight(x, y, z, MAX_LIGHT_LEVEL - 1);
                            }
                        } else {
                            break;
                        }
                    }
                }
            }
        }
    }
    
    private void calculateBlockLight(Chunk chunk) {
        for (int x = 0; x < Chunk.WIDTH; x++) {
            for (int y = 0; y < Chunk.HEIGHT; y++) {
                for (int z = 0; z < Chunk.WIDTH; z++) {
                    Material block = chunk.getBlock(x, y, z);
                    int lightEmission = getLightEmission(block);
                    
                    if (lightEmission > 0) {
                        chunk.setBlockLight(x, y, z, lightEmission);
                        propagateBlockLight(chunk, x, y, z, lightEmission);
                    }
                }
            }
        }
    }
    
    private void propagateBlockLight(Chunk chunk, int startX, int startY, int startZ, int startLevel) {
        Queue<int[]> queue = new ArrayDeque<>();
        queue.add(new int[]{startX, startY, startZ, startLevel});
        
        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int x = current[0];
            int y = current[1];
            int z = current[2];
            int level = current[3];
            
            if (level <= 1) continue;
            
            int[][] offsets = {
                {1, 0, 0}, {-1, 0, 0},
                {0, 1, 0}, {0, -1, 0},
                {0, 0, 1}, {0, 0, -1}
            };
            
            for (int[] offset : offsets) {
                int nx = x + offset[0];
                int ny = y + offset[1];
                int nz = z + offset[2];
                
                if (nx < 0 || nx >= Chunk.WIDTH || nz < 0 || nz >= Chunk.WIDTH || ny < 0 || ny >= Chunk.HEIGHT) {
                    continue;
                }
                
                Material neighbor = chunk.getBlock(nx, ny, nz);
                int opacity = getLightOpacity(neighbor);
                int newLevel = level - Math.max(LIGHT_DECAY, opacity);
                
                if (newLevel > chunk.getBlockLight(nx, ny, nz)) {
                    chunk.setBlockLight(nx, ny, nz, newLevel);
                    queue.add(new int[]{nx, ny, nz, newLevel});
                }
            }
        }
    }
    
    public void onBlockChange(int x, int y, int z, Material oldBlock, Material newBlock) {
        int oldEmission = getLightEmission(oldBlock);
        int newEmission = getLightEmission(newBlock);
        
        if (oldEmission != newEmission) {
            recalculateLightAt(x, y, z);
        }
        
        int oldOpacity = getLightOpacity(oldBlock);
        int newOpacity = getLightOpacity(newBlock);
        
        if (oldOpacity != newOpacity) {
            recalculateLightAround(x, y, z);
        }
    }
    
    private void recalculateLightAt(int x, int y, int z) {
        Chunk chunk = world.getChunkAtBlock(x, y, z);
        if (chunk == null) return;
        
        int localX = x & 15;
        int localZ = z & 15;
        
        Material block = chunk.getBlock(localX, y, localZ);
        int emission = getLightEmission(block);
        
        chunk.setBlockLight(localX, y, localZ, emission);
        
        if (emission > 0) {
            propagateBlockLight(chunk, localX, y, localZ, emission);
        }
    }
    
    private void recalculateLightAround(int x, int y, int z) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    
                    int nx = x + dx;
                    int ny = y + dy;
                    int nz = z + dz;
                    
                    if (ny < -64 || ny > 319) continue;
                    
                    Chunk chunk = world.getChunkAtBlock(nx, ny, nz);
                    if (chunk == null) continue;
                    
                    int localX = nx & 15;
                    int localZ = nz & 15;
                    
                    Material neighbor = chunk.getBlock(localX, ny, localZ);
                    int emission = getLightEmission(neighbor);
                    
                    if (emission > 0) {
                        chunk.setBlockLight(localX, ny, localZ, emission);
                        propagateBlockLight(chunk, localX, ny, localZ, emission);
                    }
                }
            }
        }
    }
    
    public static int getLightEmission(Material material) {
        if (material == null) return 0;
        return switch (material) {
            case TORCH, REDSTONE_TORCH, REDSTONE_WALL_TORCH, WALL_TORCH -> 14;
            case GLOWSTONE -> 15;
            case LAVA -> 15;
            case JACK_O_LANTERN -> 15;
            case REDSTONE_LAMP -> 15;
            case CAMPFIRE, SOUL_CAMPFIRE -> 15;
            default -> 0;
        };
    }
    
    public static int getLightOpacity(Material material) {
        if (material == null) return 0;
        if (material == Material.AIR || material == Material.WATER || material == Material.LAVA) return 0;
        if (material == Material.GLASS || material == Material.ICE || material == Material.PACKED_ICE) return 1;
        if (material.isBlock()) return 15;
        return 0;
    }
    
    public static boolean isTransparent(Material material) {
        return material == Material.AIR || material == Material.WATER || material == Material.LAVA ||
               material == Material.GLASS || material == Material.ICE;
    }
}
