package com.blazingmc.server.plugin;

import com.blazingmc.server.BlazingServer;
import com.blazingmc.plugin.NativePluginManager;
import org.bukkit.World;
import org.bukkit.command.CommandMap;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.List;
import java.util.UUID;

public class BukkitServerAdapter implements org.bukkit.Server {
    private final BlazingServer server;
    private final NativePluginManager pluginManager;
    private final NativeConsoleCommandSender consoleSender;

    public BukkitServerAdapter(BlazingServer server, NativePluginManager pluginManager) {
        this.server = server;
        this.pluginManager = pluginManager;
        this.consoleSender = new NativeConsoleCommandSender();
    }

    @Override
    public String getName() {
        return server.getServerName();
    }

    @Override
    public String getVersion() {
        return "BlazingMC 1.0.0";
    }

    @Override
    public String getBukkitVersion() {
        return "1.20.4-R0.1-SNAPSHOT";
    }

    @Override
    public int getPort() {
        return server.getConfig().getPort();
    }

    @Override
    public String getMotd() {
        return server.getMotd();
    }

    @Override
    public int getMaxPlayers() {
        return server.getMaxPlayers();
    }

    @Override
    public World getWorld(String name) {
        return null;
    }

    @Override
    public World getWorld(UUID uid) {
        return null;
    }

    @Override
    public List<World> getWorlds() {
        return List.of();
    }

    @Override
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    @Override
    public BukkitScheduler getScheduler() {
        return pluginManager.getScheduler();
    }

    @Override
    public CommandMap getCommandMap() {
        return pluginManager.getCommandMap();
    }

    @Override
    public ConsoleCommandSender getConsoleSender() {
        return consoleSender;
    }

    @Override
    public void shutdown() {
        server.stop();
    }

    @Override
    public void reload() {
    }
}
