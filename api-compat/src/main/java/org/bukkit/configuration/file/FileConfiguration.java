package org.bukkit.configuration.file;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Set;

public class FileConfiguration implements Configuration {
    @Override
    public void addDefault(String path, Object value) {
    }
    
    @Override
    public void addDefaults(Configuration configuration) {
    }
    
    @Override
    public void addDefaults(java.util.Map<String, Object> defaults) {
    }
    
    @Override
    public Object get(String path) {
        return null;
    }
    
    @Override
    public Object get(String path, Object defaultValue) {
        return defaultValue;
    }
    
    @Override
    public void set(String path, Object value) {
    }
    
    @Override
    public ConfigurationSection createSection(String path) {
        return null;
    }
    
    @Override
    public ConfigurationSection createSection(String path, java.util.Map<?, ?> map) {
        return null;
    }
    
    @Override
    public Set<String> getKeys(boolean deep) {
        return null;
    }
    
    @Override
    public java.util.Map<String, Object> getValues(boolean deep) {
        return null;
    }
    
    @Override
    public ConfigurationSection getConfigurationSection(String path) {
        return null;
    }
    
    @Override
    public boolean contains(String path) {
        return false;
    }
    
    @Override
    public boolean contains(String path, boolean ignoreDefault) {
        return false;
    }
    
    @Override
    public java.util.List<String> getStringList(String path) {
        return null;
    }
    
    @Override
    public String getString(String path) {
        return null;
    }
    
    @Override
    public String getString(String path, String defaultValue) {
        return defaultValue;
    }
    
    @Override
    public boolean isString(String path) {
        return false;
    }
    
    @Override
    public int getInt(String path) {
        return 0;
    }
    
    @Override
    public int getInt(String path, int defaultValue) {
        return defaultValue;
    }
    
    @Override
    public boolean isInt(String path) {
        return false;
    }
    
    @Override
    public boolean getBoolean(String path) {
        return false;
    }
    
    @Override
    public boolean getBoolean(String path, boolean defaultValue) {
        return defaultValue;
    }
    
    @Override
    public boolean isBoolean(String path) {
        return false;
    }
    
    @Override
    public double getDouble(String path) {
        return 0.0;
    }
    
    @Override
    public double getDouble(String path, double defaultValue) {
        return defaultValue;
    }
    
    @Override
    public boolean isDouble(String path) {
        return false;
    }
    
    @Override
    public long getLong(String path) {
        return 0L;
    }
    
    @Override
    public long getLong(String path, long defaultValue) {
        return defaultValue;
    }
    
    @Override
    public boolean isLong(String path) {
        return false;
    }
    
    @Override
    public java.util.List<?> getList(String path) {
        return null;
    }
    
    @Override
    public java.util.List<?> getList(String path, java.util.List<?> defaultValue) {
        return defaultValue;
    }
    
    @Override
    public boolean isList(String path) {
        return false;
    }
}