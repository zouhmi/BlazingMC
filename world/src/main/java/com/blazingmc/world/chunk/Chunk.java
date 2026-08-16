package com.blazingmc.world.chunk;

import com.blazingmc.world.generation.TerrainGenerator;
import org.bukkit.Material;

public class Chunk {
    public static final int WIDTH = 16;
    public static final int HEIGHT = 384;
    public static final int SECTION_HEIGHT = 16;
    public static final int SECTIONS = HEIGHT / SECTION_HEIGHT;
    
    private final int x;
    private final int z;
    private final short[] blockIds;
    private final byte[] blockData;
    private final byte[] skyLight;
    private final byte[] blockLight;
    private final int[] heightMap;
    private boolean dirty;
    private boolean generated;
    
    public Chunk(int x, int z) {
        this.x = x;
        this.z = z;
        this.blockIds = new short[WIDTH * HEIGHT * WIDTH];
        this.blockData = new byte[WIDTH * HEIGHT * WIDTH];
        this.skyLight = new byte[WIDTH * HEIGHT * WIDTH / 2];
        this.blockLight = new byte[WIDTH * HEIGHT * WIDTH / 2];
        this.heightMap = new int[WIDTH * WIDTH];
        this.dirty = false;
        this.generated = false;
    }
    
    public void generate(TerrainGenerator generator) {
        generator.generateChunk(x, z, blockIds, blockData);
        updateHeightMap();
        generated = true;
    }
    
    public int getX() { return x; }
    public int getZ() { return z; }
    
    public Material getBlock(int x, int y, int z) {
        int index = getIndex(x, y, z);
        if (index < 0 || index >= blockIds.length) {
            return Material.AIR;
        }
        return Material.values()[blockIds[index]];
    }
    
    public void setBlock(int x, int y, int z, Material material) {
        int index = getIndex(x, y, z);
        if (index < 0 || index >= blockIds.length) {
            return;
        }
        blockIds[index] = (short) material.ordinal();
        dirty = true;
    }
    
    public byte getBlockData(int x, int y, int z) {
        int index = getIndex(x, y, z);
        if (index < 0 || index >= blockData.length) {
            return 0;
        }
        return blockData[index];
    }
    
    public void setBlockData(int x, int y, int z, byte data) {
        int index = getIndex(x, y, z);
        if (index < 0 || index >= blockData.length) {
            return;
        }
        blockData[index] = data;
        dirty = true;
    }
    
    public int getHeightmap(int x, int z) {
        if (x < 0 || x >= WIDTH || z < 0 || z >= WIDTH) {
            return 0;
        }
        return heightMap[z * WIDTH + x];
    }
    
    public void setHeightmap(int x, int z, int height) {
        if (x < 0 || x >= WIDTH || z < 0 || z >= WIDTH) {
            return;
        }
        heightMap[z * WIDTH + x] = height;
    }
    
    private void updateHeightMap() {
        for (int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < WIDTH; z++) {
                int highestY = 0;
                for (int y = HEIGHT - 1; y >= 0; y--) {
                    Material material = getBlock(x, y, z);
                    if (material != Material.AIR && material != Material.WATER) {
                        highestY = y;
                        break;
                    }
                }
                heightMap[z * WIDTH + x] = highestY;
            }
        }
    }
    
    public int getHighestBlockY(int x, int z) {
        return getHeightmap(x, z);
    }
    
    public boolean isDirty() { return dirty; }
    public void setDirty(boolean dirty) { this.dirty = dirty; }
    public boolean isGenerated() { return generated; }
    public void setGenerated(boolean generated) { this.generated = generated; }
    
    public short[] getBlockIds() { return blockIds; }
    public byte[] getBlockData() { return blockData; }
    public byte[] getSkyLightArray() { return skyLight; }
    public byte[] getBlockLightArray() { return blockLight; }
    
    public int getSkyLight(int x, int y, int z) {
        int index = getIndex(x, y, z);
        if (index < 0 || index >= blockIds.length) return 15;
        int byteIndex = index >> 1;
        if ((index & 1) == 0) {
            return skyLight[byteIndex] & 0x0F;
        } else {
            return (skyLight[byteIndex] >> 4) & 0x0F;
        }
    }
    
    public void setSkyLight(int x, int y, int z, int level) {
        int index = getIndex(x, y, z);
        if (index < 0 || index >= blockIds.length) return;
        int byteIndex = index >> 1;
        level = Math.max(0, Math.min(15, level));
        if ((index & 1) == 0) {
            skyLight[byteIndex] = (byte) ((skyLight[byteIndex] & 0xF0) | level);
        } else {
            skyLight[byteIndex] = (byte) ((skyLight[byteIndex] & 0x0F) | (level << 4));
        }
        dirty = true;
    }
    
    public int getBlockLight(int x, int y, int z) {
        int index = getIndex(x, y, z);
        if (index < 0 || index >= blockIds.length) return 0;
        int byteIndex = index >> 1;
        if ((index & 1) == 0) {
            return blockLight[byteIndex] & 0x0F;
        } else {
            return (blockLight[byteIndex] >> 4) & 0x0F;
        }
    }
    
    public void setBlockLight(int x, int y, int z, int level) {
        int index = getIndex(x, y, z);
        if (index < 0 || index >= blockIds.length) return;
        int byteIndex = index >> 1;
        level = Math.max(0, Math.min(15, level));
        if ((index & 1) == 0) {
            blockLight[byteIndex] = (byte) ((blockLight[byteIndex] & 0xF0) | level);
        } else {
            blockLight[byteIndex] = (byte) ((blockLight[byteIndex] & 0x0F) | (level << 4));
        }
        dirty = true;
    }
    
    public int getLight(int x, int y, int z) {
        return Math.max(getSkyLight(x, y, z), getBlockLight(x, y, z));
    }
    
    public record SectionData(int blockId, byte data) {}
    
    public SectionData getSectionData(int x, int y, int z) {
        int index = getIndex(x, y, z);
        if (index < 0 || index >= blockIds.length) {
            return null;
        }
        return new SectionData(blockIds[index], blockData[index]);
    }
    
    public Material getBlockType(int x, int y, int z) {
        return getBlock(x, y, z);
    }
    
    public void setBlockType(int x, int y, int z, Material material) {
        setBlock(x, y, z, material);
    }
    
    private int getIndex(int x, int y, int z) {
        int adjustedY = y + 64;
        if (adjustedY < 0 || adjustedY >= HEIGHT) {
            return -1;
        }
        return (adjustedY << 8) | (z << 4) | x;
    }
}