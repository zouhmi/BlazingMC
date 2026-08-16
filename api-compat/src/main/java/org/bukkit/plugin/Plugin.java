package org.bukkit.plugin;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public interface Plugin {
    String getName();
    File getDataFolder();
    PluginDescriptionFile getDescription();
    FileConfiguration getConfig();
    InputStream getResource(String filename);
    void saveConfig();
    void saveDefaultConfig();
    void saveResource(String resourcePath, boolean replace);
    void reloadConfig();
    File getFile();
    Server getServer();
    ClassLoader getClassLoader();
    void setNaggable(boolean canNag);
    boolean isNaggable();
    void onCommandExecutionAborted(CommandSender sender, Command command, String label, String[] args);
    boolean isEnabled();
    void onDisable();
    void onLoad();
    void onEnable();
    void onDisableConfigErrors();
}