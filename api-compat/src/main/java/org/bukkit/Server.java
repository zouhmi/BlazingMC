package org.bukkit;

import org.bukkit.command.CommandMap;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.List;
import java.util.UUID;

public interface Server {
    String getName();
    String getVersion();
    String getBukkitVersion();
    int getPort();
    String getMotd();
    int getMaxPlayers();
    World getWorld(String name);
    World getWorld(UUID uid);
    List<World> getWorlds();
    PluginManager getPluginManager();
    BukkitScheduler getScheduler();
    CommandMap getCommandMap();
    ConsoleCommandSender getConsoleSender();
    void shutdown();
    void reload();
}