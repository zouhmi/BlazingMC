package org.bukkit.configuration.file;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class YamlConfiguration extends FileConfiguration {
    public static YamlConfiguration loadConfiguration(File file) {
        YamlConfiguration configuration = new YamlConfiguration();
        if (file == null || !file.isFile()) {
            return configuration;
        }
        try {
            for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int separator = trimmed.indexOf(':');
                if (separator <= 0) {
                    continue;
                }
                String path = trimmed.substring(0, separator).trim();
                String value = trimmed.substring(separator + 1).trim();
                configuration.set(path, parse(value));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load configuration: " + file, exception);
        }
        return configuration;
    }

    public void save(File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("file cannot be null");
        }
        File parent = file.getParentFile();
        if (parent != null) {
            Files.createDirectories(parent.toPath());
        }
        StringBuilder content = new StringBuilder();
        for (var entry : getValues(true).entrySet()) {
            content.append(entry.getKey()).append(": ").append(format(entry.getValue())).append('\n');
        }
        Files.writeString(file.toPath(), content.toString(), StandardCharsets.UTF_8);
    }

    private static Object parse(String value) {
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(value);
        }
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1).replace("\\\"", "\"");
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
        }
        return value;
    }

    private static String format(Object value) {
        if (value instanceof String string) {
            return "\"" + string.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        }
        return String.valueOf(value);
    }
}
