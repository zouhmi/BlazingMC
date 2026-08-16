package com.blazingmc.server.block;

import com.blazingmc.chat.ConsoleLogger;
import com.blazingmc.protocol.handler.PlayerInterface;
import com.blazingmc.server.BlazingServer;
import com.blazingmc.world.World;
import org.bukkit.Material;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SignManager {
    private static final int MAX_SIGN_LINES = 4;
    private static final int MAX_SIGN_LINE_LENGTH = 384;
    
    private final World world;
    private final Map<Long, SignData> signs;
    
    public SignManager(World world) {
        this.world = world;
        this.signs = new HashMap<>();
    }
    
    public void createSign(int x, int y, int z, String[] lines) {
        if (lines == null || lines.length == 0) return;
        
        String[] signLines = new String[MAX_SIGN_LINES];
        for (int i = 0; i < MAX_SIGN_LINES; i++) {
            signLines[i] = i < lines.length ? truncateLine(lines[i]) : "";
        }
        
        long key = getKey(x, y, z);
        signs.put(key, new SignData(x, y, z, signLines));
        
        broadcastSignUpdate(x, y, z, signLines);
        
        ConsoleLogger.debug("Sign created at " + x + "," + y + "," + z);
    }
    
    public void updateSign(int x, int y, int z, String[] lines) {
        if (lines == null || lines.length == 0) return;
        
        long key = getKey(x, y, z);
        SignData existing = signs.get(key);
        
        if (existing != null) {
            String[] signLines = new String[MAX_SIGN_LINES];
            for (int i = 0; i < MAX_SIGN_LINES; i++) {
                signLines[i] = i < lines.length ? truncateLine(lines[i]) : "";
            }
            
            signs.put(key, new SignData(x, y, z, signLines));
            broadcastSignUpdate(x, y, z, signLines);
        }
    }
    
    public String[] getSignLines(int x, int y, int z) {
        long key = getKey(x, y, z);
        SignData sign = signs.get(key);
        return sign != null ? sign.lines() : null;
    }
    
    public boolean hasSign(int x, int y, int z) {
        return signs.containsKey(getKey(x, y, z));
    }
    
    public void removeSign(int x, int y, int z) {
        signs.remove(getKey(x, y, z));
    }
    
    public void openSignEditor(PlayerInterface player, int x, int y, int z) {
        ByteBuffer buffer = ByteBuffer.allocate(128).order(ByteOrder.BIG_ENDIAN);
        
        writeVarInt(buffer, x);
        writeVarInt(buffer, y);
        writeVarInt(buffer, z);
        
        String[] existingLines = getSignLines(x, y, z);
        if (existingLines != null) {
            for (String line : existingLines) {
                writeString(buffer, line != null ? line : "");
            }
        } else {
            for (int i = 0; i < MAX_SIGN_LINES; i++) {
                writeString(buffer, "");
            }
        }
        
        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        
        player.sendPacket(0x2C, data);
    }
    
    public void handleSignUpdate(int x, int y, int z, String[] lines) {
        Material block = world.getBlockAt(x, y, z);
        if (!isSignMaterial(block)) return;
        
        updateSign(x, y, z, lines);
    }
    
    public void onBlockBreak(int x, int y, int z, Material material) {
        if (isSignMaterial(material)) {
            removeSign(x, y, z);
        }
    }
    
    private boolean isSignMaterial(Material material) {
        return material == Material.OAK_SIGN ||
               material == Material.SPRUCE_SIGN ||
               material == Material.BIRCH_SIGN ||
               material == Material.JUNGLE_SIGN ||
               material == Material.ACACIA_SIGN ||
               material == Material.DARK_OAK_SIGN;
    }
    
    private void broadcastSignUpdate(int x, int y, int z, String[] lines) {
        ByteBuffer buffer = ByteBuffer.allocate(512).order(ByteOrder.BIG_ENDIAN);
        
        writeVarInt(buffer, x);
        writeVarInt(buffer, y);
        writeVarInt(buffer, z);
        
        for (String line : lines) {
            writeString(buffer, line != null ? line : "");
        }
        
        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        
        for (PlayerInterface player : BlazingServer.getInstance().getPlayerManager().getOnlinePlayers()) {
            player.sendPacket(0x46, data);
        }
    }
    
    private String truncateLine(String line) {
        if (line == null) return "";
        if (line.length() > MAX_SIGN_LINE_LENGTH) {
            return line.substring(0, MAX_SIGN_LINE_LENGTH);
        }
        return line;
    }
    
    private void writeVarInt(ByteBuffer buffer, int value) {
        while ((value & ~0x7F) != 0) {
            buffer.put((byte) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
        buffer.put((byte) value);
    }
    
    private void writeString(ByteBuffer buffer, String str) {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        writeVarInt(buffer, bytes.length);
        buffer.put(bytes);
    }
    
    private long getKey(int x, int y, int z) {
        return ((long) x << 40) | ((long) y << 20) | (z & 0xFFFFF);
    }
    
    public int getSignCount() {
        return signs.size();
    }
    
    public record SignData(int x, int y, int z, String[] lines) {}
}
