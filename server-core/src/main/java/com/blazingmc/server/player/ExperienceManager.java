package com.blazingmc.server.player;

import com.blazingmc.protocol.handler.PlayerInterface;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ExperienceManager {
    private int experienceLevel;
    private float experienceProgress;
    private int totalExperience;
    
    private static final int[] XP_PER_LEVEL = {
        0, 7, 15, 25, 35, 46, 58, 71, 85, 100,
        116, 133, 151, 170, 190, 211, 233, 256, 280, 305,
        331, 358, 386, 415, 445, 476, 508, 541, 575, 610,
        646, 683, 721, 760, 800, 841, 883, 926, 970, 1015,
        1061, 1108, 1156, 1205, 1255, 1306, 1358, 1411, 1465, 1520,
        1576, 1633, 1691, 1750, 1810, 1871, 1933, 1996, 2060, 2125,
        2191, 2258, 2326, 2395, 2465, 2536, 2608, 2681, 2755, 2830,
        2906, 2983, 3061, 3140, 3220, 3301, 3383, 3466, 3550, 3635,
        3721, 3808, 3896, 3985, 4075, 4166, 4258, 4351, 4445, 4540,
        4636, 4733, 4831, 4930, 5030, 5131, 5233, 5336, 5440, 5545
    };
    
    public ExperienceManager() {
        this.experienceLevel = 0;
        this.experienceProgress = 0.0f;
        this.totalExperience = 0;
    }
    
    public void addExperience(int amount) {
        totalExperience += amount;
        
        while (experienceLevel < XP_PER_LEVEL.length - 1 && 
               totalExperience >= XP_PER_LEVEL[experienceLevel + 1]) {
            experienceLevel++;
        }
        
        if (experienceLevel < XP_PER_LEVEL.length - 1) {
            int currentLevelXp = XP_PER_LEVEL[experienceLevel];
            int nextLevelXp = XP_PER_LEVEL[experienceLevel + 1];
            experienceProgress = (float) (totalExperience - currentLevelXp) / (nextLevelXp - currentLevelXp);
        } else {
            experienceProgress = 1.0f;
        }
        
        experienceProgress = Math.max(0.0f, Math.min(1.0f, experienceProgress));
    }
    
    public boolean removeExperience(int amount) {
        if (totalExperience < amount) {
            return false;
        }
        
        totalExperience -= amount;
        
        while (experienceLevel > 0 && totalExperience < XP_PER_LEVEL[experienceLevel]) {
            experienceLevel--;
        }
        
        if (experienceLevel < XP_PER_LEVEL.length - 1) {
            int currentLevelXp = XP_PER_LEVEL[experienceLevel];
            int nextLevelXp = XP_PER_LEVEL[experienceLevel + 1];
            experienceProgress = (float) (totalExperience - currentLevelXp) / (nextLevelXp - currentLevelXp);
        } else {
            experienceProgress = 1.0f;
        }
        
        return true;
    }
    
    public boolean hasLevel(int level) {
        return experienceLevel >= level;
    }
    
    public boolean removeLevels(int levels) {
        if (experienceLevel < levels) {
            return false;
        }
        
        experienceLevel -= levels;
        totalExperience = XP_PER_LEVEL[experienceLevel];
        experienceProgress = 0.0f;
        return true;
    }
    
    public int getXpToNextLevel() {
        if (experienceLevel >= XP_PER_LEVEL.length - 1) {
            return 0;
        }
        return XP_PER_LEVEL[experienceLevel + 1] - XP_PER_LEVEL[experienceLevel];
    }
    
    public int getCurrentLevelXp() {
        if (experienceLevel >= XP_PER_LEVEL.length - 1) {
            return totalExperience - XP_PER_LEVEL[experienceLevel];
        }
        return totalExperience - XP_PER_LEVEL[experienceLevel];
    }
    
    public void sendExperienceUpdate(PlayerInterface player) {
        ByteBuffer buffer = ByteBuffer.allocate(16).order(ByteOrder.BIG_ENDIAN);
        
        buffer.putFloat(experienceProgress);
        writeVarInt(buffer, experienceLevel);
        writeVarInt(buffer, totalExperience);
        
        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        
        player.sendPacket(0x62, data);
    }
    
    public int getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(int level) { this.experienceLevel = level; }
    public float getExperienceProgress() { return experienceProgress; }
    public void setExperienceProgress(float progress) { this.experienceProgress = progress; }
    public int getTotalExperience() { return totalExperience; }
    public void setTotalExperience(int total) { this.totalExperience = total; }
    
    private void writeVarInt(ByteBuffer buffer, int value) {
        while ((value & ~0x7F) != 0) {
            buffer.put((byte) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
        buffer.put((byte) value);
    }
}
