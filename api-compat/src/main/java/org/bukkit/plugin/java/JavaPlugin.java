package org.bukkit.plugin.java;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

public abstract class JavaPlugin implements Plugin {
    private boolean enabled = false;
    private boolean naggable = true;
    private File dataFolder;
    private PluginDescriptionFile description;
    private File file;
    private ClassLoader classLoader;
    private Server server;
    private Logger logger;
    
    public JavaPlugin() {
        this.dataFolder = new File("plugins" + File.separator + getDescription().getName());
        this.logger = Logger.getLogger(getDescription().getName());
    }
    
    public abstract void onEnable();
    public abstract void onDisable();
    
    public void onLoad() {}
    public void onDisableConfigErrors() {}
    
    @Override
    public File getDataFolder() {
        return dataFolder;
    }
    
    @Override
    public PluginDescriptionFile getDescription() {
        return description;
    }
    
    @Override
    public FileConfiguration getConfig() {
        return null;
    }
    
    @Override
    public InputStream getResource(String filename) {
        return getClass().getClassLoader().getResourceAsStream(filename);
    }
    
    @Override
    public void saveConfig() {
    }
    
    @Override
    public void saveDefaultConfig() {
    }
    
    @Override
    public void saveResource(String resourcePath, boolean replace) {
    }
    
    @Override
    public void reloadConfig() {
    }
    
    @Override
    public File getFile() {
        return file;
    }
    
    @Override
    public Server getServer() {
        return server;
    }
    
    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }
    
    @Override
    public void setNaggable(boolean canNag) {
        this.naggable = canNag;
    }
    
    @Override
    public boolean isNaggable() {
        return naggable;
    }
    
    @Override
    public void onCommandExecutionAborted(CommandSender sender, Command command, String label, String[] args) {
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }
}