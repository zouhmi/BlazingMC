package org.bukkit;

import org.bukkit.command.CommandMap;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Objects;

public final class Bukkit {
    private static Server server;

    private Bukkit() {
    }

    public static void setServer(Server value) {
        server = value;
    }

    public static Server getServer() {
        return server;
    }

    public static PluginManager getPluginManager() {
        return requireServer().getPluginManager();
    }

    public static BukkitScheduler getScheduler() {
        return requireServer().getScheduler();
    }

    public static CommandMap getCommandMap() {
        return requireServer().getCommandMap();
    }

    private static Server requireServer() {
        return Objects.requireNonNull(server, "Bukkit server is not initialized");
    }
}
