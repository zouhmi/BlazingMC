package org.bukkit.configuration;

import java.util.Map;
import java.util.Set;

public interface Configuration {
    void addDefault(String path, Object value);
    void addDefaults(Configuration configuration);
    void addDefaults(Map<String, Object> defaults);
    Object get(String path);
    Object get(String path, Object defaultValue);
    void set(String path, Object value);
    ConfigurationSection createSection(String path);
    ConfigurationSection createSection(String path, Map<?, ?> map);
    Set<String> getKeys(boolean deep);
    Map<String, Object> getValues(boolean deep);
    ConfigurationSection getConfigurationSection(String path);
    boolean contains(String path);
    boolean contains(String path, boolean ignoreDefault);
    java.util.List<String> getStringList(String path);
    String getString(String path);
    String getString(String path, String defaultValue);
    boolean isString(String path);
    int getInt(String path);
    int getInt(String path, int defaultValue);
    boolean isInt(String path);
    boolean getBoolean(String path);
    boolean getBoolean(String path, boolean defaultValue);
    boolean isBoolean(String path);
    double getDouble(String path);
    double getDouble(String path, double defaultValue);
    boolean isDouble(String path);
    long getLong(String path);
    long getLong(String path, long defaultValue);
    boolean isLong(String path);
    java.util.List<?> getList(String path);
    java.util.List<?> getList(String path, java.util.List<?> defaultValue);
    boolean isList(String path);
}