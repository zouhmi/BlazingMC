package org.bukkit.plugin.java;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public abstract class JavaPlugin implements Plugin {
    private boolean enabled;
    private boolean naggable;
    private final File dataFolder;
    private final PluginDescriptionFile description;
    private final File file;
    private final ClassLoader classLoader;
    private Server server;
    private final Logger logger;
    private FileConfiguration config;

    public JavaPlugin() {
        this.description = new PluginDescriptionFile(getClass().getSimpleName(), "unknown", getClass().getName());
        this.dataFolder = new File("plugins" + File.separator + description.getName());
        this.file = new File(description.getName() + ".jar");
        this.classLoader = getClass().getClassLoader();
        this.logger = Logger.getLogger(description.getName());
        this.naggable = true;
    }

    public abstract void onEnable();
    public abstract void onDisable();

    public void onLoad() {
    }

    public void onDisableConfigErrors() {
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public PluginDescriptionFile getDescription() {
        return description;
    }

    @Override
    public synchronized FileConfiguration getConfig() {
        if (config == null) {
            config = new FileConfiguration();
        }
        return config;
    }

    @Override
    public InputStream getResource(String filename) {
        if (filename == null || filename.isBlank()) {
            return null;
        }
        return classLoader.getResourceAsStream(filename);
    }

    @Override
    public synchronized void saveConfig() {
        try {
            Files.createDirectories(dataFolder.toPath());
            Path configPath = dataFolder.toPath().resolve("config.yml");
            StringBuilder content = new StringBuilder();
            for (Map.Entry<String, Object> entry : getConfig().getValues(true).entrySet()) {
                content.append(entry.getKey()).append(": ").append(formatValue(entry.getValue())).append('\n');
            }
            Files.writeString(configPath, content.toString(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            logger.warning("Unable to save plugin configuration: " + exception.getMessage());
        }
    }

    @Override
    public synchronized void saveDefaultConfig() {
        Path configPath = dataFolder.toPath().resolve("config.yml");
        if (Files.exists(configPath)) {
            return;
        }
        InputStream resource = getResource("config.yml");
        if (resource != null) {
            try (resource) {
                Files.createDirectories(dataFolder.toPath());
                Files.copy(resource, configPath);
                reloadConfig();
                return;
            } catch (IOException exception) {
                logger.warning("Unable to save default configuration: " + exception.getMessage());
            }
        }
        saveConfig();
    }

    @Override
    public void saveResource(String resourcePath, boolean replace) {
        if (resourcePath == null || resourcePath.isBlank()) {
            throw new IllegalArgumentException("resourcePath cannot be blank");
        }
        InputStream resource = getResource(resourcePath);
        if (resource == null) {
            throw new IllegalArgumentException("Resource not found: " + resourcePath);
        }
        Path target = dataFolder.toPath().resolve(resourcePath).normalize();
        if (!target.startsWith(dataFolder.toPath().normalize())) {
            throw new IllegalArgumentException("Resource path escapes plugin data folder");
        }
        if (Files.exists(target) && !replace) {
            return;
        }
        try (resource) {
            Files.createDirectories(target.getParent());
            Files.copy(resource, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to save resource: " + resourcePath, exception);
        }
    }

    @Override
    public synchronized void reloadConfig() {
        FileConfiguration reloaded = new FileConfiguration();
        Path configPath = dataFolder.toPath().resolve("config.yml");
        if (Files.exists(configPath)) {
            try {
                for (String line : Files.readAllLines(configPath, StandardCharsets.UTF_8)) {
                    int separator = line.indexOf(':');
                    if (separator <= 0) {
                        continue;
                    }
                    String key = line.substring(0, separator).trim();
                    String value = line.substring(separator + 1).trim();
                    reloaded.set(key, parseValue(value));
                }
            } catch (IOException exception) {
                logger.warning("Unable to reload plugin configuration: " + exception.getMessage());
            }
        }
        config = reloaded;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public Logger getLogger() {
        return logger;
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
        if (this.enabled == enabled) {
            return;
        }
        this.enabled = enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    private String formatValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String string) {
            return "\"" + string.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        }
        if (value instanceof List<?> list) {
            return list.toString();
        }
        return String.valueOf(value);
    }

    private Object parseValue(String value) {
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(value);
        }
        if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
            return value.substring(1, value.length() - 1).replace("\\\"", "\"").replace("\\\\", "\\");
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
        }
        return value;
    }
}
