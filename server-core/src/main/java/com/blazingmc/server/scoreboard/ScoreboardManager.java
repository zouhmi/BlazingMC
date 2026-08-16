package com.blazingmc.server.scoreboard;

import com.blazingmc.chat.ConsoleLogger;
import com.blazingmc.protocol.handler.PlayerInterface;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ScoreboardManager {
    private final Map<String, ScoreboardObjective> objectives;
    private final Map<String, ScoreboardTeam> teams;
    private final Map<String, Map<String, Integer>> scores;
    
    public ScoreboardManager() {
        this.objectives = new ConcurrentHashMap<>();
        this.teams = new ConcurrentHashMap<>();
        this.scores = new ConcurrentHashMap<>();
    }
    
    public void addObjective(PlayerInterface player, String name, String displayName, int renderType) {
        ScoreboardObjective obj = new ScoreboardObjective(name, displayName, renderType);
        objectives.put(name, obj);
        
        ByteBuffer buffer = ByteBuffer.allocate(256).order(ByteOrder.BIG_ENDIAN);
        
        buffer.put((byte) 0x00);
        writeString(buffer, name);
        writeString(buffer, displayName);
        writeVarInt(buffer, renderType);
        
        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        
        player.sendPacket(0x52, data);
    }
    
    public void removeObjective(PlayerInterface player, String name) {
        objectives.remove(name);
        
        ByteBuffer buffer = ByteBuffer.allocate(128).order(ByteOrder.BIG_ENDIAN);
        
        buffer.put((byte) 0x01);
        writeString(buffer, name);
        
        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        
        player.sendPacket(0x52, data);
    }
    
    public void updateObjective(PlayerInterface player, String name, String displayName, int renderType) {
        ScoreboardObjective obj = objectives.get(name);
        if (obj != null) {
            obj.displayName = displayName;
            obj.renderType = renderType;
        }
        
        ByteBuffer buffer = ByteBuffer.allocate(256).order(ByteOrder.BIG_ENDIAN);
        
        buffer.put((byte) 0x02);
        writeString(buffer, name);
        writeString(buffer, displayName);
        writeVarInt(buffer, renderType);
        
        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        
        player.sendPacket(0x52, data);
    }
    
    public void setDisplayObjective(PlayerInterface player, int slot, String objectiveName) {
        ByteBuffer buffer = ByteBuffer.allocate(128).order(ByteOrder.BIG_ENDIAN);
        
        buffer.put((byte) (byte) slot);
        writeString(buffer, objectiveName);
        
        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        
        player.sendPacket(0x57, data);
    }
    
    public void setScore(PlayerInterface player, String objectiveName, String scoreName, int value) {
        Map<String, Integer> objScores = scores.computeIfAbsent(objectiveName, k -> new ConcurrentHashMap<>());
        objScores.put(scoreName, value);
        
        ByteBuffer buffer = ByteBuffer.allocate(256).order(ByteOrder.BIG_ENDIAN);
        
        buffer.put((byte) 0x00);
        writeString(buffer, scoreName);
        buffer.put((byte) 0x00);
        writeString(buffer, objectiveName);
        writeVarInt(buffer, value);
        writeVarInt(buffer, 0);
        
        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        
        player.sendPacket(0x55, data);
    }
    
    public void removeScore(PlayerInterface player, String objectiveName, String scoreName) {
        Map<String, Integer> objScores = scores.get(objectiveName);
        if (objScores != null) {
            objScores.remove(scoreName);
        }
        
        ByteBuffer buffer = ByteBuffer.allocate(256).order(ByteOrder.BIG_ENDIAN);
        
        buffer.put((byte) 0x01);
        writeString(buffer, scoreName);
        writeString(buffer, objectiveName);
        
        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        
        player.sendPacket(0x55, data);
    }
    
    public void addTeam(PlayerInterface player, String teamName, String displayName, String prefix, String suffix, int friendlyFlags, int collisionRule, int nameTagVisibility) {
        ScoreboardTeam team = new ScoreboardTeam(teamName, displayName, prefix, suffix);
        teams.put(teamName, team);
        
        ByteBuffer buffer = ByteBuffer.allocate(512).order(ByteOrder.BIG_ENDIAN);
        
        buffer.put((byte) 0x00);
        writeString(buffer, teamName);
        writeVarInt(buffer, 0);
        writeString(buffer, displayName);
        writeString(buffer, prefix);
        writeString(buffer, suffix);
        writeVarInt(buffer, friendlyFlags);
        writeVarInt(buffer, nameTagVisibility);
        writeVarInt(buffer, collisionRule);
        writeVarInt(buffer, 0);
        
        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        
        player.sendPacket(0x58, data);
    }
    
    public void removeTeam(PlayerInterface player, String teamName) {
        teams.remove(teamName);
        
        ByteBuffer buffer = ByteBuffer.allocate(128).order(ByteOrder.BIG_ENDIAN);
        
        buffer.put((byte) 0x01);
        writeString(buffer, teamName);
        
        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        
        player.sendPacket(0x58, data);
    }
    
    public void addPlayerToTeam(PlayerInterface player, String teamName, String playerName) {
        ScoreboardTeam team = teams.get(teamName);
        if (team != null) {
            team.players.add(playerName);
        }
        
        ByteBuffer buffer = ByteBuffer.allocate(256).order(ByteOrder.BIG_ENDIAN);
        
        buffer.put((byte) 0x03);
        writeString(buffer, teamName);
        writeVarInt(buffer, 3);
        writeVarInt(buffer, 1);
        writeString(buffer, playerName);
        
        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        
        player.sendPacket(0x58, data);
    }
    
    public void removePlayerFromTeam(PlayerInterface player, String teamName, String playerName) {
        ScoreboardTeam team = teams.get(teamName);
        if (team != null) {
            team.players.remove(playerName);
        }
        
        ByteBuffer buffer = ByteBuffer.allocate(256).order(ByteOrder.BIG_ENDIAN);
        
        buffer.put((byte) 0x04);
        writeString(buffer, teamName);
        writeVarInt(buffer, 1);
        writeString(buffer, playerName);
        
        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        
        player.sendPacket(0x58, data);
    }
    
    public ScoreboardObjective getObjective(String name) {
        return objectives.get(name);
    }
    
    public ScoreboardTeam getTeam(String name) {
        return teams.get(name);
    }
    
    public Map<String, Integer> getScores(String objectiveName) {
        return scores.getOrDefault(objectiveName, Collections.emptyMap());
    }
    
    public Collection<ScoreboardObjective> getObjectives() {
        return Collections.unmodifiableCollection(objectives.values());
    }
    
    public Collection<ScoreboardTeam> getTeams() {
        return Collections.unmodifiableCollection(teams.values());
    }
    
    public void sendScoreboard(PlayerInterface player) {
        for (ScoreboardObjective obj : objectives.values()) {
            addObjective(player, obj.name, obj.displayName, obj.renderType);
        }
        
        for (Map.Entry<String, Map<String, Integer>> entry : scores.entrySet()) {
            String objName = entry.getKey();
            for (Map.Entry<String, Integer> scoreEntry : entry.getValue().entrySet()) {
                setScore(player, objName, scoreEntry.getKey(), scoreEntry.getValue());
            }
        }
    }
    
    public void broadcastObjective(String name, String displayName, int renderType) {
        for (ScoreboardObjective obj : objectives.values()) {
            if (obj.name.equals(name)) {
                obj.displayName = displayName;
                obj.renderType = renderType;
            }
        }
    }
    
    private void writeString(ByteBuffer buffer, String str) {
        byte[] bytes = str.getBytes();
        writeVarInt(buffer, bytes.length);
        buffer.put(bytes);
    }
    
    private void writeVarInt(ByteBuffer buffer, int value) {
        while ((value & ~0x7F) != 0) {
            buffer.put((byte) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
        buffer.put((byte) value);
    }
    
    public static class ScoreboardObjective {
        public final String name;
        public String displayName;
        public int renderType;
        
        public ScoreboardObjective(String name, String displayName, int renderType) {
            this.name = name;
            this.displayName = displayName;
            this.renderType = renderType;
        }
    }
    
    public static class ScoreboardTeam {
        public final String name;
        public String displayName;
        public String prefix;
        public String suffix;
        public final Set<String> players;
        
        public ScoreboardTeam(String name, String displayName, String prefix, String suffix) {
            this.name = name;
            this.displayName = displayName;
            this.prefix = prefix;
            this.suffix = suffix;
            this.players = ConcurrentHashMap.newKeySet();
        }
    }
}
