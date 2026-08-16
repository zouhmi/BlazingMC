package org.bukkit.configuration.file;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FileConfiguration implements Configuration, ConfigurationSection {
    private final Map<String, Object> values;
    private final Map<String, Object> defaults;
    private final String prefix;
    private final FileConfigurationOptions options;

    public FileConfiguration() {
        this(new LinkedHashMap<>(), new LinkedHashMap<>(), "");
    }

    private FileConfiguration(Map<String, Object> values, Map<String, Object> defaults, String prefix) {
        this.values = values;
        this.defaults = defaults;
        this.prefix = prefix;
        this.options = new FileConfigurationOptions(this);
    }

    public FileConfigurationOptions options() {
        return options;
    }

    @Override
    public void addDefault(String path, Object value) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("path cannot be blank");
        }
        put(defaults, path, copyValue(value));
    }

    @Override
    public void addDefaults(Configuration configuration) {
        if (configuration == null) {
            return;
        }
        addDefaults(configuration.getValues(true));
    }

    @Override
    public void addDefaults(Map<String, Object> newDefaults) {
        if (newDefaults == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : newDefaults.entrySet()) {
            addDefault(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Object get(String path) {
        Object value = find(values, path);
        return value != null ? copyValue(value) : copyValue(find(defaults, path));
    }

    @Override
    public Object get(String path, Object defaultValue) {
        Object value = get(path);
        return value == null ? defaultValue : value;
    }

    @Override
    public void set(String path, Object value) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("path cannot be blank");
        }
        if (value == null) {
            remove(values, path);
        } else {
            put(values, path, copyValue(value));
        }
    }

    @Override
    public ConfigurationSection createSection(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("path cannot be blank");
        }
        set(path, new LinkedHashMap<>());
        return getConfigurationSection(path);
    }

    @Override
    public ConfigurationSection createSection(String path, Map<?, ?> map) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("path cannot be blank");
        }
        Map<String, Object> section = new LinkedHashMap<>();
        if (map != null) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() != null) {
                    section.put(String.valueOf(entry.getKey()), copyValue(entry.getValue()));
                }
            }
        }
        set(path, section);
        return getConfigurationSection(path);
    }

    @Override
    public Set<String> getKeys(boolean deep) {
        Object section = getSectionValue(values);
        if (!(section instanceof Map<?, ?> map)) {
            return Collections.emptySet();
        }
        Set<String> result = new LinkedHashSet<>();
        collectKeys(map, "", deep, result);
        return result;
    }

    @Override
    public Map<String, Object> getValues(boolean deep) {
        Object section = getSectionValue(values);
        if (!(section instanceof Map<?, ?> map)) {
            return Collections.emptyMap();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        collectValues(map, "", deep, result);
        return result;
    }

    @Override
    public ConfigurationSection getConfigurationSection(String path) {
        Object value = get(path);
        if (!(value instanceof Map<?, ?>)) {
            return null;
        }
        return new FileConfiguration(values, defaults, fullPath(path));
    }

    @Override
    public boolean contains(String path) {
        return find(values, path) != null || find(defaults, path) != null;
    }

    @Override
    public boolean contains(String path, boolean ignoreDefault) {
        return find(values, path) != null || (!ignoreDefault && find(defaults, path) != null);
    }

    @Override
    public List<String> getStringList(String path) {
        Object value = get(path);
        if (value instanceof Collection<?> collection) {
            List<String> result = new ArrayList<>(collection.size());
            for (Object item : collection) {
                if (item != null) {
                    result.add(String.valueOf(item));
                }
            }
            return result;
        }
        return new ArrayList<>();
    }

    @Override
    public String getString(String path) {
        return getString(path, null);
    }

    @Override
    public String getString(String path, String defaultValue) {
        Object value = get(path);
        return value == null ? defaultValue : String.valueOf(value);
    }

    @Override
    public boolean isString(String path) {
        return get(path) instanceof String;
    }

    @Override
    public int getInt(String path) {
        return getInt(path, 0);
    }

    @Override
    public int getInt(String path, int defaultValue) {
        Object value = get(path);
        return value instanceof Number ? ((Number) value).intValue() : defaultValue;
    }

    @Override
    public boolean isInt(String path) {
        return get(path) instanceof Integer;
    }

    @Override
    public boolean getBoolean(String path) {
        return getBoolean(path, false);
    }

    @Override
    public boolean getBoolean(String path, boolean defaultValue) {
        Object value = get(path);
        return value instanceof Boolean ? (Boolean) value : defaultValue;
    }

    @Override
    public boolean isBoolean(String path) {
        return get(path) instanceof Boolean;
    }

    @Override
    public double getDouble(String path) {
        return getDouble(path, 0.0D);
    }

    @Override
    public double getDouble(String path, double defaultValue) {
        Object value = get(path);
        return value instanceof Number ? ((Number) value).doubleValue() : defaultValue;
    }

    @Override
    public boolean isDouble(String path) {
        return get(path) instanceof Double;
    }

    @Override
    public long getLong(String path) {
        return getLong(path, 0L);
    }

    @Override
    public long getLong(String path, long defaultValue) {
        Object value = get(path);
        return value instanceof Number ? ((Number) value).longValue() : defaultValue;
    }

    @Override
    public boolean isLong(String path) {
        return get(path) instanceof Long;
    }

    @Override
    public List<?> getList(String path) {
        return getList(path, Collections.emptyList());
    }

    @Override
    public List<?> getList(String path, List<?> defaultValue) {
        Object value = get(path);
        return value instanceof List<?> list ? new ArrayList<>(list) : defaultValue;
    }

    @Override
    public boolean isList(String path) {
        return get(path) instanceof List<?>;
    }

    private Object getSectionValue(Map<String, Object> root) {
        if (prefix.isEmpty()) {
            return root;
        }
        return find(root, prefix);
    }

    private String fullPath(String path) {
        if (path == null || path.isBlank()) {
            return prefix;
        }
        return prefix.isEmpty() ? path : prefix + "." + path;
    }

    private Object find(Map<String, Object> root, String path) {
        String resolved = fullPath(path);
        if (resolved.isEmpty()) {
            return root;
        }
        Object current = root;
        for (String part : resolved.split("\\.")) {
            if (!(current instanceof Map<?, ?> map)) {
                return null;
            }
            current = map.get(part);
        }
        return current;
    }

    private void put(Map<String, Object> root, String path, Object value) {
        String resolved = fullPath(path);
        String[] parts = resolved.split("\\.");
        Map<String, Object> current = root;
        for (int i = 0; i < parts.length - 1; i++) {
            Object child = current.get(parts[i]);
            if (!(child instanceof Map<?, ?>)) {
                child = new LinkedHashMap<String, Object>();
                current.put(parts[i], child);
            }
            current = castMap(child);
        }
        current.put(parts[parts.length - 1], value);
    }

    private void remove(Map<String, Object> root, String path) {
        String resolved = fullPath(path);
        String[] parts = resolved.split("\\.");
        Map<String, Object> current = root;
        for (int i = 0; i < parts.length - 1; i++) {
            Object child = current.get(parts[i]);
            if (!(child instanceof Map<?, ?>)) {
                return;
            }
            current = castMap(child);
        }
        current.remove(parts[parts.length - 1]);
    }

    private void collectKeys(Map<?, ?> map, String path, boolean deep, Set<String> result) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = String.valueOf(entry.getKey());
            String current = path.isEmpty() ? key : path + "." + key;
            result.add(current);
            if (deep && entry.getValue() instanceof Map<?, ?> child) {
                collectKeys(child, current, true, result);
            }
        }
    }

    private void collectValues(Map<?, ?> map, String path, boolean deep, Map<String, Object> result) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = String.valueOf(entry.getKey());
            String current = path.isEmpty() ? key : path + "." + key;
            if (deep && entry.getValue() instanceof Map<?, ?> child) {
                collectValues(child, current, true, result);
            } else {
                result.put(current, copyValue(entry.getValue()));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object value) {
        return (Map<String, Object>) value;
    }

    private Object copyValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> copy = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() != null) {
                    copy.put(String.valueOf(entry.getKey()), copyValue(entry.getValue()));
                }
            }
            return copy;
        }
        if (value instanceof List<?> list) {
            List<Object> copy = new ArrayList<>(list.size());
            for (Object item : list) {
                copy.add(copyValue(item));
            }
            return copy;
        }
        return value;
    }
}
