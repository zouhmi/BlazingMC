package com.blazingmc.server.player;

import com.blazingmc.chat.ConsoleLogger;
import com.blazingmc.protocol.handler.PlayerInterface;
import com.blazingmc.protocol.handler.PlayerManagerInterface;
import com.blazingmc.server.BlazingServer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerManager implements PlayerManagerInterface {
    private final BlazingServer server;
    private final Map<UUID, Player> players;
    private final AtomicInteger entityIdCounter;
    
    public PlayerManager(BlazingServer server) {
        this.server = server;
        this.players = new ConcurrentHashMap<>();
        this.entityIdCounter = new AtomicInteger(0);
    }
    
    public void addPlayer(Player player) {
        players.put(player.getUuid(), player);
        ConsoleLogger.join(player.getUsername());
        
        server.getTabListManager().addPlayer(player);
        server.getTabListManager().spawnExistingPlayersToNewPlayer(player);
        player.sendChunksAround();
    }
    
    public void removePlayer(UUID uuid) {
        Player removed = players.remove(uuid);
        if (removed != null) {
            server.getTabListManager().despawnPlayerFromAll(removed);
            server.getTabListManager().removePlayer(uuid);
            ConsoleLogger.quit(removed.getUsername());
        }
    }
    
    public Player getPlayer(UUID uuid) {
        return players.get(uuid);
    }
    
    public Player getPlayer(String username) {
        for (Player player : players.values()) {
            if (player.getUsername().equalsIgnoreCase(username)) {
                return player;
            }
        }
        return null;
    }
    
    public boolean isPlayerOnline(UUID uuid) {
        return players.containsKey(uuid);
    }
    
    public boolean isPlayerOnline(String username) {
        return getPlayer(username) != null;
    }
    
    public int getOnlinePlayerCount() {
        return players.size();
    }
    
    public int getNextEntityId() {
        return entityIdCounter.incrementAndGet();
    }
    
    public void tickAll() {
        for (Player player : players.values()) {
            player.tick();
        }
    }
    
    public void broadcastPacket(int packetId, byte[] data, Player exclude) {
        for (Player player : players.values()) {
            if (player != exclude) {
                player.sendPacket(packetId, data);
            }
        }
    }
    
    public Map<UUID, PlayerInterface> getPlayers() {
        Map<UUID, PlayerInterface> result = new java.util.HashMap<>();
        for (Map.Entry<UUID, Player> entry : players.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
    
    public void shutdown() {
        for (Player player : players.values()) {
            player.disconnect("Server shutting down");
        }
        players.clear();
    }
    
    public Collection<PlayerInterface> getOnlinePlayers() {
        return new ArrayList<>(players.values());
    }
}