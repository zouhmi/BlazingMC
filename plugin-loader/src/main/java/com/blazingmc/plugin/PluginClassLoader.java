package com.blazingmc.plugin;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PluginClassLoader extends URLClassLoader {
    private final Map<String, Class<?>> classes;
    private final PluginDescriptionFile description;
    private final File dataFolder;
    private final File pluginFile;
    private Plugin plugin;

    public PluginClassLoader(PluginDescriptionFile description, File file, File dataFolder, ClassLoader parent) throws java.net.MalformedURLException {
        super(new URL[]{file.toURI().toURL()}, parent);
        this.classes = new ConcurrentHashMap<>();
        this.description = description;
        this.dataFolder = dataFolder;
        this.pluginFile = file;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> result = classes.get(name);
        if (result != null) {
            return result;
        }
        result = super.findClass(name);
        classes.put(name, result);
        return result;
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> result = classes.get(name);
        if (result != null) {
            return result;
        }
        result = super.loadClass(name, resolve);
        classes.put(name, result);
        return result;
    }

    public void addURL(URL url) {
        super.addURL(url);
    }

    public Plugin loadPlugin() throws ReflectiveOperationException {
        if (description.getMain().isBlank()) {
            throw new ClassNotFoundException("Plugin main class is missing for " + description.getName());
        }
        Class<?> mainClass = loadClass(description.getMain());
        if (!JavaPlugin.class.isAssignableFrom(mainClass)) {
            throw new ClassCastException(description.getMain() + " does not extend JavaPlugin");
        }
        JavaPlugin instance = (JavaPlugin) mainClass.getDeclaredConstructor().newInstance();
        instance.initialize(description, dataFolder, pluginFile, this);
        plugin = instance;
        return instance;
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
