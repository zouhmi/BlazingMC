package com.blazingmc.server.player;

import com.blazingmc.chat.ConsoleLogger;
import com.blazingmc.protocol.handler.PlayerInterface;
import com.blazingmc.server.BlazingServer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

public class TabListManager {
    private final BlazingServer server;
    private final Map<UUID, TabListEntry> tabEntries;
    
    public TabListManager(BlazingServer server) {
        this.server = server;
        this.tabEntries = new LinkedHashMap<>();
    }
    
    public void addPlayer(PlayerInterface player) {
        TabListEntry entry = new TabListEntry(
            player.getUuid(),
            player.getUsername(),
            getGameMode(player),
            0,
            player.getDisplayName() != null ? player.getDisplayName() : player.getUsername()
        );
        tabEntries.put(player.getUuid(), entry);
        
        broadcastPlayerInfoAdd(player);
    }
    
    public void removePlayer(UUID uuid) {
        tabEntries.remove(uuid);
        broadcastPlayerInfoRemove(uuid);
    }
    
    public void updatePlayerList() {
        for (PlayerInterface player : server.getPlayerManager().getOnlinePlayers()) {
            sendTabListToPlayer(player);
        }
    }
    
    public void sendTabListToPlayer(PlayerInterface player) {
        ByteBuffer buffer = ByteBuffer.allocate(4096).order(ByteOrder.BIG_ENDIAN);
        
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);
        
        writeVarInt(buffer, tabEntries.size());
        
        for (TabListEntry entry : tabEntries.values()) {
            writeUUID(buffer, entry.uuid);
            
            writeVarInt(buffer, 0);
            writeString(buffer, entry.name);
            writeVarInt(buffer, entry.gameMode);
            writeVarInt(buffer, entry.ping);
            buffer.put((byte) 0);
            writeString(buffer, entry.displayName);
        }
        
        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        
        player.sendPacket(0x36, data);
    }
    
    private void broadcastPlayerInfoAdd(PlayerInterface player) {
        ByteBuffer buffer = ByteBuffer.allocate(256).order(ByteOrder.BIG_ENDIAN);
        
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);
        
        writeVarInt(buffer, 1);
        writeUUID(buffer, player.getUuid());
        
        writeVarInt(buffer, 0);
        writeString(buffer, player.getUsername());
        writeVarInt(buffer, getGameMode(player));
        writeVarInt(buffer, 0);
        buffer.put((byte) 0);
        writeString(buffer, player.getUsername());
        
        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        
        for (PlayerInterface other : server.getPlayerManager().getOnlinePlayers()) {
            if (other.getEntityId() != player.getEntityId()) {
                other.sendPacket(0x36, data);
            }
        }
    }
    
    private void broadcastPlayerInfoRemove(UUID uuid) {
        ByteBuffer buffer = ByteBuffer.allocate(64).order(ByteOrder.BIG_ENDIAN);
        
        buffer.put((byte) 0x01);
        buffer.put((byte) 0x00);
        
        writeVarInt(buffer, 1);
        writeUUID(buffer, uuid);
        
        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        
        for (PlayerInterface player : server.getPlayerManager().getOnlinePlayers()) {
            player.sendPacket(0x36, data);
        }
    }
    
    public void sendPlayerSpawn(PlayerInterface player, PlayerInterface target) {
        ByteBuffer buffer = ByteBuffer.allocate(128).order(ByteOrder.BIG_ENDIAN);
        
        buffer.putInt(target.getEntityId());
        writeUUID(buffer, target.getUuid());
        writeVarInt(buffer, 0);
        buffer.putDouble(target.getX());
        buffer.putDouble(target.getY());
        buffer.putDouble(target.getZ());
        buffer.putFloat(target.getYaw());
        buffer.putFloat(target.getPitch());
        
        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        
        player.sendPacket(0x01, data);
    }
    
    public void sendPlayerRemove(PlayerInterface player, int entityId) {
        ByteBuffer buffer = ByteBuffer.allocate(16).order(ByteOrder.BIG_ENDIAN);
        
        writeVarInt(buffer, 1);
        buffer.putInt(entityId);
        
        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        
        player.sendPacket(0x3A, data);
    }
    
    public void spawnExistingPlayersToNewPlayer(PlayerInterface newPlayer) {
        for (PlayerInterface existing : server.getPlayerManager().getOnlinePlayers()) {
            if (existing.getEntityId() != newPlayer.getEntityId()) {
                sendPlayerSpawn(newPlayer, existing);
                sendPlayerSpawn(existing, newPlayer);
            }
        }
    }
    
    public void despawnPlayerFromAll(PlayerInterface player) {
        for (PlayerInterface other : server.getPlayerManager().getOnlinePlayers()) {
            if (other.getEntityId() != player.getEntityId()) {
                sendPlayerRemove(other, player.getEntityId());
            }
        }
    }
    
    private int getGameMode(PlayerInterface player) {
        return player.getGameMode();
    }
    
    public Collection<TabListEntry> getTabEntries() {
        return Collections.unmodifiableCollection(tabEntries.values());
    }
    
    public int getTabListSize() {
        return tabEntries.size();
    }
    
    private void writeUUID(ByteBuffer buffer, UUID uuid) {
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
    }
    
    private void writeVarInt(ByteBuffer buffer, int value) {
        while ((value & ~0x7F) != 0) {
            buffer.put((byte) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
        buffer.put((byte) value);
    }
    
    private void writeString(ByteBuffer buffer, String str) {
        byte[] bytes = str.getBytes();
        writeVarInt(buffer, bytes.length);
        buffer.put(bytes);
    }
    
    public static class TabListEntry {
        public final UUID uuid;
        public final String name;
        public final int gameMode;
        public final int ping;
        public final String displayName;
        
        public TabListEntry(UUID uuid, String name, int gameMode, int ping, String displayName) {
            this.uuid = uuid;
            this.name = name;
            this.gameMode = gameMode;
            this.ping = ping;
            this.displayName = displayName;
        }
    }
}
