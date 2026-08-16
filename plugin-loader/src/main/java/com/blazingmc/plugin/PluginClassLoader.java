package com.blazingmc.plugin;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PluginClassLoader extends URLClassLoader {
    private final Map<String, Class<?>> classes;
    private final PluginDescriptionFile description;
    private final File dataFolder;
    private final Plugin plugin;
    
    public PluginClassLoader(PluginDescriptionFile description, File file, File dataFolder, ClassLoader parent) throws java.net.MalformedURLException {
        super(new URL[]{file.toURI().toURL()}, parent);
        this.classes = new ConcurrentHashMap<>();
        this.description = description;
        this.dataFolder = dataFolder;
        this.plugin = null;
    }
    
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> result = classes.get(name);
        if (result != null) {
            return result;
        }
        
        try {
            result = super.findClass(name);
            classes.put(name, result);
            return result;
        } catch (ClassNotFoundException e) {
            throw e;
        }
    }
    
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> result = classes.get(name);
        if (result != null) {
            return result;
        }
        
        try {
            result = super.loadClass(name, resolve);
            classes.put(name, result);
            return result;
        } catch (ClassNotFoundException e) {
            throw e;
        }
    }
    
    public void addURL(URL url) {
        super.addURL(url);
    }
    
    public PluginDescriptionFile getDescription() {
        return description;
    }
    
    public File getDataFolder() {
        return dataFolder;
    }
    
    public Plugin getPlugin() {
        return plugin;
    }
}