package com.blazingmc.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ServerConfig {
    private static final Logger logger = LoggerFactory.getLogger(ServerConfig.class);
    private static final Path CONFIG_PATH = Path.of("server.yml");
    
    private String serverName = "BlazingMC";
    private int port = 25565;
    private String motd = "A BlazingMC Server";
    private int maxPlayers = 200;
    private int viewDistance = 10;
    private int simulationDistance = 10;
    private int regionSize = 8;
    private int tickRate = 20;
    private boolean compatMode = false;
    private boolean onlineMode = false;
    private String serverIp = "0.0.0.0";
    private int networkCompressionThreshold = 256;
    private boolean rateLimitEnabled = false;
    private int rateLimit = 5;
    private boolean enforceWhitelist = false;
    private boolean whiteList = false;
    private int spawnProtection = 16;
    private boolean allowNether = true;
    private boolean hardcore = false;
    private boolean pvp = true;
    private boolean commandBlocks = false;
    private int maxTickTime = 60000;
    private boolean spawnMonsters = true;
    private boolean spawnAnimals = true;
    private boolean spawnNpcs = true;
    private String levelName = "world";
    private long levelSeed = 0;
    private String levelType = "minecraft:normal";
    private boolean generateStructures = true;
    private boolean allowFlight = false;
    private boolean forceGamemode = false;
    private int maxWorldSize = 29999984;
    private int playerIdleTimeout = 0;
    private int viewDistanceChunks = 10;
    
    private boolean chatEnabled = true;
    private boolean chatFilter = false;
    private int chatDistance = 0;
    private boolean chatFormatting = true;
    private String chatPrefix = "<";
    private String chatSuffix = ">";
    
    private boolean antiXrayEnabled = true;
    private int antiXrayMode = 1;
    private boolean[] antiXrayEngineMode = {true, false, true};
    private int antiXrayMaxWeight = 1;
    private int[] antiXrayUpdateRadius = {1, 2, 4};
    private boolean[] antiXrayChunkSendExecution = {true, false, true};
    
    private boolean entityActivationEnabled = true;
    private int entityActivationRangeAnimal = 32;
    private int entityActivationRangeMonster = 32;
    private int entityActivationRangeMisc = 16;
    private int entityActivationRangeFlying = 32;
    
    private boolean chunksPerTickEnabled = true;
    private int chunksPerTick = 5;
    private boolean autoSaveEnabled = true;
    private int autoSaveInterval = 6000;
    
    private boolean soundsEnabled = true;
    private boolean particlesEnabled = true;
    private boolean weatherEnabled = true;
    private boolean daylightCycle = true;
    
    private boolean logPlayerJoinLeave = true;
    private boolean logChatMessages = true;
    private boolean logCommands = true;
    private boolean logBlockBreaks = false;
    
    private int networkCompressionLevel = -1;
    private boolean proxyProtocol = false;
    private boolean bungeeCord = false;
    private String bungeeCordSecret = "";
    
    public static ServerConfig load() {
        ServerConfig config = new ServerConfig();
        
        if (Files.exists(CONFIG_PATH)) {
            try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
                Yaml yaml = new Yaml();
                Object loaded = yaml.load(in);
                if (loaded instanceof Map<?, ?> map) {
                    Map<String, Object> data = new HashMap<>();
                    for (Map.Entry<?, ?> entry : map.entrySet()) {
                        if (entry.getKey() != null) {
                            data.put(String.valueOf(entry.getKey()), entry.getValue());
                        }
                    }
                    config.applyProperties(data);
                }
                config.validate();
                logger.info("Loaded server configuration from {}", CONFIG_PATH);
            } catch (IOException e) {
                logger.error("Failed to load server configuration", e);
            }
        } else {
            config.save();
            logger.info("Created default server configuration at {}", CONFIG_PATH);
        }
        
        return config;
    }

    private void validate() {
        port = Math.max(1, Math.min(65535, port));
        maxPlayers = Math.max(1, maxPlayers);
        viewDistance = Math.max(2, Math.min(32, viewDistance));
        simulationDistance = Math.max(2, Math.min(32, simulationDistance));
        regionSize = Math.max(1, regionSize);
        tickRate = Math.max(1, Math.min(100, tickRate));
        networkCompressionThreshold = Math.max(-1, networkCompressionThreshold);
        rateLimit = Math.max(1, rateLimit);
        spawnProtection = Math.max(0, spawnProtection);
        maxTickTime = Math.max(1, maxTickTime);
        maxWorldSize = Math.max(1, maxWorldSize);
        playerIdleTimeout = Math.max(0, playerIdleTimeout);
        viewDistanceChunks = Math.max(2, Math.min(32, viewDistanceChunks));
        chatDistance = Math.max(0, chatDistance);
        antiXrayMode = Math.max(1, antiXrayMode);
        antiXrayMaxWeight = Math.max(0, antiXrayMaxWeight);
        entityActivationRangeAnimal = Math.max(0, entityActivationRangeAnimal);
        entityActivationRangeMonster = Math.max(0, entityActivationRangeMonster);
        entityActivationRangeMisc = Math.max(0, entityActivationRangeMisc);
        entityActivationRangeFlying = Math.max(0, entityActivationRangeFlying);
        chunksPerTick = Math.max(1, chunksPerTick);
        autoSaveInterval = Math.max(1, autoSaveInterval);
        networkCompressionLevel = Math.max(-1, Math.min(9, networkCompressionLevel));
    }
    
    private void applyProperties(Map<String, Object> data) {
        serverName = getString(data, "server-name", serverName);
        port = getInt(data, "port", port);
        motd = getString(data, "motd", motd);
        maxPlayers = getInt(data, "max-players", maxPlayers);
        viewDistance = getInt(data, "view-distance", viewDistance);
        simulationDistance = getInt(data, "simulation-distance", simulationDistance);
        regionSize = getInt(data, "region-size", regionSize);
        tickRate = getInt(data, "tick-rate", tickRate);
        compatMode = getBoolean(data, "compat-mode", compatMode);
        onlineMode = getBoolean(data, "online-mode", onlineMode);
        serverIp = getString(data, "server-ip", serverIp);
        networkCompressionThreshold = getInt(data, "network-compression-threshold", networkCompressionThreshold);
        rateLimitEnabled = getBoolean(data, "rate-limit-enabled", rateLimitEnabled);
        rateLimit = getInt(data, "rate-limit", rateLimit);
        enforceWhitelist = getBoolean(data, "enforce-whitelist", enforceWhitelist);
        whiteList = getBoolean(data, "white-list", whiteList);
        spawnProtection = getInt(data, "spawn-protection", spawnProtection);
        allowNether = getBoolean(data, "allow-nether", allowNether);
        hardcore = getBoolean(data, "hardcore", hardcore);
        pvp = getBoolean(data, "pvp", pvp);
        commandBlocks = getBoolean(data, "command-blocks", commandBlocks);
        maxTickTime = getInt(data, "max-tick-time", maxTickTime);
        spawnMonsters = getBoolean(data, "spawn-monsters", spawnMonsters);
        spawnAnimals = getBoolean(data, "spawn-animals", spawnAnimals);
        spawnNpcs = getBoolean(data, "spawn-npcs", spawnNpcs);
        levelName = getString(data, "level-name", levelName);
        levelSeed = getLong(data, "level-seed", levelSeed);
        levelType = getString(data, "level-type", levelType);
        generateStructures = getBoolean(data, "generate-structures", generateStructures);
        allowFlight = getBoolean(data, "allow-flight", allowFlight);
        forceGamemode = getBoolean(data, "force-gamemode", forceGamemode);
        maxWorldSize = getInt(data, "max-world-size", maxWorldSize);
        playerIdleTimeout = getInt(data, "player-idle-timeout", playerIdleTimeout);
        viewDistanceChunks = getInt(data, "view-distance-chunks", viewDistanceChunks);
        
        chatEnabled = getBoolean(data, "chat-enabled", chatEnabled);
        chatFilter = getBoolean(data, "chat-filter", chatFilter);
        chatDistance = getInt(data, "chat-distance", chatDistance);
        chatFormatting = getBoolean(data, "chat-formatting", chatFormatting);
        chatPrefix = getString(data, "chat-prefix", chatPrefix);
        chatSuffix = getString(data, "chat-suffix", chatSuffix);
        
        antiXrayEnabled = getBoolean(data, "anti-xray-enabled", antiXrayEnabled);
        antiXrayMode = getInt(data, "anti-xray-mode", antiXrayMode);
        antiXrayMaxWeight = getInt(data, "anti-xray-max-weight", antiXrayMaxWeight);
        
        entityActivationEnabled = getBoolean(data, "entity-activation-enabled", entityActivationEnabled);
        entityActivationRangeAnimal = getInt(data, "entity-activation-range-animal", entityActivationRangeAnimal);
        entityActivationRangeMonster = getInt(data, "entity-activation-range-monster", entityActivationRangeMonster);
        entityActivationRangeMisc = getInt(data, "entity-activation-range-misc", entityActivationRangeMisc);
        entityActivationRangeFlying = getInt(data, "entity-activation-range-flying", entityActivationRangeFlying);
        
        chunksPerTickEnabled = getBoolean(data, "chunks-per-tick-enabled", chunksPerTickEnabled);
        chunksPerTick = getInt(data, "chunks-per-tick", chunksPerTick);
        autoSaveEnabled = getBoolean(data, "auto-save-enabled", autoSaveEnabled);
        autoSaveInterval = getInt(data, "auto-save-interval", autoSaveInterval);
        
        soundsEnabled = getBoolean(data, "sounds-enabled", soundsEnabled);
        particlesEnabled = getBoolean(data, "particles-enabled", particlesEnabled);
        weatherEnabled = getBoolean(data, "weather-enabled", weatherEnabled);
        daylightCycle = getBoolean(data, "daylight-cycle", daylightCycle);
        
        logPlayerJoinLeave = getBoolean(data, "log-player-join-leave", logPlayerJoinLeave);
        logChatMessages = getBoolean(data, "log-chat-messages", logChatMessages);
        logCommands = getBoolean(data, "log-commands", logCommands);
        logBlockBreaks = getBoolean(data, "log-block-breaks", logBlockBreaks);
        
        networkCompressionLevel = getInt(data, "network-compression-level", networkCompressionLevel);
        proxyProtocol = getBoolean(data, "proxy-protocol", proxyProtocol);
        bungeeCord = getBoolean(data, "bungee-cord", bungeeCord);
        bungeeCordSecret = getString(data, "bungee-cord-secret", bungeeCordSecret);
    }
    
    private String getString(Map<String, Object> data, String key, String defaultValue) {
        Object value = data.get(key);
        return value instanceof String ? (String) value : defaultValue;
    }
    
    private int getInt(Map<String, Object> data, String key, int defaultValue) {
        Object value = data.get(key);
        return value instanceof Number ? ((Number) value).intValue() : defaultValue;
    }
    
    private long getLong(Map<String, Object> data, String key, long defaultValue) {
        Object value = data.get(key);
        return value instanceof Number ? ((Number) value).longValue() : defaultValue;
    }
    
    private boolean getBoolean(Map<String, Object> data, String key, boolean defaultValue) {
        Object value = data.get(key);
        return value instanceof Boolean ? (Boolean) value : defaultValue;
    }
    
    public void save() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        
        Yaml yaml = new Yaml(options);
        Map<String, Object> data = new HashMap<>();
        
        data.put("server-name", serverName);
        data.put("port", port);
        data.put("motd", motd);
        data.put("max-players", maxPlayers);
        data.put("view-distance", viewDistance);
        data.put("simulation-distance", simulationDistance);
        data.put("region-size", regionSize);
        data.put("tick-rate", tickRate);
        data.put("compat-mode", compatMode);
        data.put("online-mode", onlineMode);
        data.put("server-ip", serverIp);
        data.put("network-compression-threshold", networkCompressionThreshold);
        data.put("rate-limit-enabled", rateLimitEnabled);
        data.put("rate-limit", rateLimit);
        data.put("enforce-whitelist", enforceWhitelist);
        data.put("white-list", whiteList);
        data.put("spawn-protection", spawnProtection);
        data.put("allow-nether", allowNether);
        data.put("hardcore", hardcore);
        data.put("pvp", pvp);
        data.put("command-blocks", commandBlocks);
        data.put("max-tick-time", maxTickTime);
        data.put("spawn-monsters", spawnMonsters);
        data.put("spawn-animals", spawnAnimals);
        data.put("spawn-npcs", spawnNpcs);
        data.put("level-name", levelName);
        data.put("level-seed", levelSeed);
        data.put("level-type", levelType);
        data.put("generate-structures", generateStructures);
        data.put("allow-flight", allowFlight);
        data.put("force-gamemode", forceGamemode);
        data.put("max-world-size", maxWorldSize);
        data.put("player-idle-timeout", playerIdleTimeout);
        data.put("view-distance-chunks", viewDistanceChunks);
        
        data.put("chat-enabled", chatEnabled);
        data.put("chat-filter", chatFilter);
        data.put("chat-distance", chatDistance);
        data.put("chat-formatting", chatFormatting);
        data.put("chat-prefix", chatPrefix);
        data.put("chat-suffix", chatSuffix);
        
        data.put("anti-xray-enabled", antiXrayEnabled);
        data.put("anti-xray-mode", antiXrayMode);
        data.put("anti-xray-max-weight", antiXrayMaxWeight);
        
        data.put("entity-activation-enabled", entityActivationEnabled);
        data.put("entity-activation-range-animal", entityActivationRangeAnimal);
        data.put("entity-activation-range-monster", entityActivationRangeMonster);
        data.put("entity-activation-range-misc", entityActivationRangeMisc);
        data.put("entity-activation-range-flying", entityActivationRangeFlying);
        
        data.put("chunks-per-tick-enabled", chunksPerTickEnabled);
        data.put("chunks-per-tick", chunksPerTick);
        data.put("auto-save-enabled", autoSaveEnabled);
        data.put("auto-save-interval", autoSaveInterval);
        
        data.put("sounds-enabled", soundsEnabled);
        data.put("particles-enabled", particlesEnabled);
        data.put("weather-enabled", weatherEnabled);
        data.put("daylight-cycle", daylightCycle);
        
        data.put("log-player-join-leave", logPlayerJoinLeave);
        data.put("log-chat-messages", logChatMessages);
        data.put("log-commands", logCommands);
        data.put("log-block-breaks", logBlockBreaks);
        
        data.put("network-compression-level", networkCompressionLevel);
        data.put("proxy-protocol", proxyProtocol);
        data.put("bungee-cord", bungeeCord);
        data.put("bungee-cord-secret", bungeeCordSecret);
        
        try (java.io.BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH)) {
            yaml.dump(data, writer);
        } catch (IOException e) {
            logger.error("Failed to save server configuration", e);
        }
    }
    
    public String getServerName() { return serverName; }
    public int getPort() { return port; }
    public String getMotd() { return motd; }
    public int getMaxPlayers() { return maxPlayers; }
    public int getViewDistance() { return viewDistance; }
    public int getSimulationDistance() { return simulationDistance; }
    public int getRegionSize() { return regionSize; }
    public int getTickRate() { return tickRate; }
    public boolean isCompatMode() { return compatMode; }
    public boolean isOnlineMode() { return onlineMode; }
    public String getServerIp() { return serverIp; }
    public int getNetworkCompressionThreshold() { return networkCompressionThreshold; }
    public boolean isRateLimitEnabled() { return rateLimitEnabled; }
    public int getRateLimit() { return rateLimit; }
    public boolean isEnforceWhitelist() { return enforceWhitelist; }
    public boolean isWhiteList() { return whiteList; }
    public int getSpawnProtection() { return spawnProtection; }
    public boolean isAllowNether() { return allowNether; }
    public boolean isHardcore() { return hardcore; }
    public boolean isPvp() { return pvp; }
    public boolean isCommandBlocks() { return commandBlocks; }
    public int getMaxTickTime() { return maxTickTime; }
    public boolean isSpawnMonsters() { return spawnMonsters; }
    public boolean isSpawnAnimals() { return spawnAnimals; }
    public boolean isSpawnNpcs() { return spawnNpcs; }
    public String getLevelName() { return levelName; }
    public long getLevelSeed() { return levelSeed; }
    public String getLevelType() { return levelType; }
    public boolean isGenerateStructures() { return generateStructures; }
    public boolean isAllowFlight() { return allowFlight; }
    public boolean isForceGamemode() { return forceGamemode; }
    public int getMaxWorldSize() { return maxWorldSize; }
    public int getPlayerIdleTimeout() { return playerIdleTimeout; }
    public int getViewDistanceChunks() { return viewDistanceChunks; }
    
    public boolean isChatEnabled() { return chatEnabled; }
    public boolean isChatFilter() { return chatFilter; }
    public int getChatDistance() { return chatDistance; }
    public boolean isChatFormatting() { return chatFormatting; }
    public String getChatPrefix() { return chatPrefix; }
    public String getChatSuffix() { return chatSuffix; }
    
    public boolean isAntiXrayEnabled() { return antiXrayEnabled; }
    public int getAntiXrayMode() { return antiXrayMode; }
    public boolean[] getAntiXrayEngineMode() { return antiXrayEngineMode; }
    public int getAntiXrayMaxWeight() { return antiXrayMaxWeight; }
    public int[] getAntiXrayUpdateRadius() { return antiXrayUpdateRadius; }
    public boolean[] getAntiXrayChunkSendExecution() { return antiXrayChunkSendExecution; }
    
    public boolean isEntityActivationEnabled() { return entityActivationEnabled; }
    public int getEntityActivationRangeAnimal() { return entityActivationRangeAnimal; }
    public int getEntityActivationRangeMonster() { return entityActivationRangeMonster; }
    public int getEntityActivationRangeMisc() { return entityActivationRangeMisc; }
    public int getEntityActivationRangeFlying() { return entityActivationRangeFlying; }
    
    public boolean isChunksPerTickEnabled() { return chunksPerTickEnabled; }
    public int getChunksPerTick() { return chunksPerTick; }
    public boolean isAutoSaveEnabled() { return autoSaveEnabled; }
    public int getAutoSaveInterval() { return autoSaveInterval; }
    
    public boolean isSoundsEnabled() { return soundsEnabled; }
    public boolean isParticlesEnabled() { return particlesEnabled; }
    public boolean isWeatherEnabled() { return weatherEnabled; }
    public boolean isDaylightCycle() { return daylightCycle; }
    
    public boolean isLogPlayerJoinLeave() { return logPlayerJoinLeave; }
    public boolean isLogChatMessages() { return logChatMessages; }
    public boolean isLogCommands() { return logCommands; }
    public boolean isLogBlockBreaks() { return logBlockBreaks; }
    
    public int getNetworkCompressionLevel() { return networkCompressionLevel; }
    public boolean isProxyProtocol() { return proxyProtocol; }
    public boolean isBungeeCord() { return bungeeCord; }
    public String getBungeeCordSecret() { return bungeeCordSecret; }
}