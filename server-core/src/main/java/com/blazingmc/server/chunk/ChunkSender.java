package com.blazingmc.server.chunk;

import com.blazingmc.chat.ConsoleLogger;
import com.blazingmc.server.player.Player;
import com.blazingmc.world.chunk.Chunk;
import com.blazingmc.world.chunk.ChunkManager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.Set;

public class ChunkSender {
    private static final int SECTION_HEIGHT = 16;
    private static final int MIN_Y = -64;
    private static final int MAX_Y = 320;
    private static final int SECTIONS = (MAX_Y - MIN_Y) / SECTION_HEIGHT;
    
    private final ChunkManager chunkManager;
    private final Set<Long> sentChunks;
    
    public ChunkSender(ChunkManager chunkManager) {
        this.chunkManager = chunkManager;
        this.sentChunks = new HashSet<>();
    }
    
    public void sendChunk(Player player, int chunkX, int chunkZ) {
        Chunk chunk = chunkManager.getChunk(chunkX, chunkZ);
        if (chunk == null || !chunk.isGenerated()) {
            return;
        }
        
        byte[] chunkData = serializeChunkForPacket(chunk);
        if (chunkData == null) {
            return;
        }
        
        ByteBuffer buffer = ByteBuffer.allocate(chunkData.length + 20).order(ByteOrder.BIG_ENDIAN);
        
        buffer.putInt(chunkX);
        buffer.putInt(chunkZ);
        buffer.put((byte) 0);
        buffer.putInt(chunk.getHighestBlockY(0, 0));
        buffer.putLong(System.currentTimeMillis());
        
        buffer.put(chunkData);
        
        player.sendPacket(0x20, buffer.array());
        
        long key = getKey(chunkX, chunkZ);
        sentChunks.add(key);
        
        ConsoleLogger.debug("Sent chunk " + chunkX + ", " + chunkZ + " to " + player.getUsername());
    }
    
    public void sendChunksAround(Player player, int centerChunkX, int centerChunkZ) {
        int viewDistance = chunkManager.getViewDistance();
        
        for (int x = centerChunkX - viewDistance; x <= centerChunkX + viewDistance; x++) {
            for (int z = centerChunkZ - viewDistance; z <= centerChunkZ + viewDistance; z++) {
                long key = getKey(x, z);
                if (!sentChunks.contains(key)) {
                    sendChunk(player, x, z);
                }
            }
        }
    }
    
    public void unloadDistantChunks(Player player, int centerChunkX, int centerChunkZ) {
        int viewDistance = chunkManager.getViewDistance() + 2;
        
        Set<Long> toRemove = new HashSet<>();
        for (long key : sentChunks) {
            int chunkX = (int) (key >> 32);
            int chunkZ = (int) key;
            
            int dx = Math.abs(chunkX - centerChunkX);
            int dz = Math.abs(chunkZ - centerChunkZ);
            
            if (dx > viewDistance || dz > viewDistance) {
                toRemove.add(key);
                sendUnloadChunk(player, chunkX, chunkZ);
            }
        }
        
        sentChunks.removeAll(toRemove);
    }
    
    private void sendUnloadChunk(Player player, int chunkX, int chunkZ) {
        ByteBuffer buffer = ByteBuffer.allocate(12).order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(chunkX);
        buffer.putInt(chunkZ);
        
        player.sendPacket(0x1D, buffer.array());
        
        ConsoleLogger.debug("Sent unload chunk " + chunkX + ", " + chunkZ + " to " + player.getUsername());
    }
    
    private byte[] serializeChunkForPacket(Chunk chunk) {
        int sectionCount = SECTIONS;
        int bitmask = (1 << sectionCount) - 1;
        
        int estimatedSize = 1 + 4 + (sectionCount * (1 + 1 + 1 + 1 + (16 * 16 * 16 * 2) + (16 * 16 * 16 / 2) + (16 * 16 * 16 / 2))) + (16 * 16 * 4);
        
        ByteBuffer buffer = ByteBuffer.allocate(estimatedSize).order(ByteOrder.BIG_ENDIAN);
        
        buffer.putInt(bitmask);
        buffer.putLong(System.currentTimeMillis());
        
        buffer.put((byte) sectionCount);
        
        for (int section = 0; section < sectionCount; section++) {
            int nonEmptyBlockCount = 0;
            
            for (int y = 0; y < SECTION_HEIGHT; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        int worldY = section * SECTION_HEIGHT + y + MIN_Y;
                        if (worldY >= -64 && worldY < 320) {
                            Chunk.SectionData sectionData = chunk.getSectionData(x, worldY, z);
                            if (sectionData != null && sectionData.blockId() != 0) {
                                nonEmptyBlockCount++;
                            }
                        }
                    }
                }
            }
            
            buffer.putShort((short) nonEmptyBlockCount);
            
            buffer.put((byte) 15);
            buffer.put((byte) 0);
            buffer.put((byte) 0);
            buffer.put((byte) 0);
            
            int bitsPerBlock = 15;
            int paletteSize = 1 << bitsPerBlock;
            
            int longsNeeded = (4096 * bitsPerBlock + 63) / 64;
            buffer.putInt(longsNeeded);
            
            long[] blockStates = new long[longsNeeded];
            int longIndex = 0;
            int bitIndex = 0;
            
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        int worldY = section * SECTION_HEIGHT + y + MIN_Y;
                        int blockStateId = 0;
                        
                        if (worldY >= -64 && worldY < 320) {
                            Chunk.SectionData sectionData = chunk.getSectionData(x, worldY, z);
                            if (sectionData != null) {
                                blockStateId = sectionData.blockId();
                            }
                        }
                        
                        long value = blockStateId & ((1L << bitsPerBlock) - 1);
                        blockStates[longIndex] |= value << bitIndex;
                        
                        bitIndex += bitsPerBlock;
                        if (bitIndex >= 64) {
                            bitIndex = 0;
                            longIndex++;
                        }
                    }
                }
            }
            
            for (long l : blockStates) {
                buffer.putLong(l);
            }
            
            byte[] skyLightSection = new byte[2048];
            byte[] blockLightSection = new byte[2048];
            
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        int worldY = section * SECTION_HEIGHT + y + MIN_Y;
                        if (worldY >= -64 && worldY < 320) {
                            int skyLight = chunk.getSkyLight(x, worldY, z);
                            int blockLight = chunk.getBlockLight(x, worldY, z);
                            
                            int index = (y * 16 + z) * 16 + x;
                            int byteIndex = index >> 1;
                            
                            if ((index & 1) == 0) {
                                skyLightSection[byteIndex] = (byte) ((skyLightSection[byteIndex] & 0xF0) | skyLight);
                                blockLightSection[byteIndex] = (byte) ((blockLightSection[byteIndex] & 0xF0) | blockLight);
                            } else {
                                skyLightSection[byteIndex] = (byte) ((skyLightSection[byteIndex] & 0x0F) | (skyLight << 4));
                                blockLightSection[byteIndex] = (byte) ((blockLightSection[byteIndex] & 0x0F) | (blockLight << 4));
                            }
                        }
                    }
                }
            }
            
            buffer.put(skyLightSection);
            buffer.put(blockLightSection);
            
            int biomePaletteSize = 1;
            buffer.putInt(biomePaletteSize);
            buffer.putInt(0);
            
            int biomeLongs = 1;
            buffer.putInt(biomeLongs);
            buffer.putLong(0L);
        }
        
        int heightmapLongs = 37;
        buffer.putInt(heightmapLongs);
        
        long[] heightmapData = new long[heightmapLongs];
        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                int height = chunk.getHeightmap(x, z);
                int index = z * 16 + x;
                int longIndex = index / 7;
                int bitIndex = (index % 7) * 9;
                
                if (longIndex < heightmapLongs) {
                    heightmapData[longIndex] |= ((long) (height + 64) & 0x1FFL) << bitIndex;
                }
            }
        }
        
        for (long l : heightmapData) {
            buffer.putLong(l);
        }
        
        buffer.flip();
        byte[] result = new byte[buffer.remaining()];
        buffer.get(result);
        return result;
    }
    
    public void reset() {
        sentChunks.clear();
    }
    
    public boolean hasSentChunk(int chunkX, int chunkZ) {
        return sentChunks.contains(getKey(chunkX, chunkZ));
    }
    
    public int getSentChunkCount() {
        return sentChunks.size();
    }
    
    private long getKey(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }
}