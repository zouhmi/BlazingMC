package com.blazingmc.plugin;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventExecutor;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;

public class NativePluginManager implements PluginManager, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(NativePluginManager.class);
    private final Path pluginDirectory;
    private Server server;
    private final CommandMap commandMap;
    private final NativeScheduler scheduler;
    private final Map<String, JavaPlugin> plugins;
    private final Map<String, PluginClassLoader> classLoaders;
    private final Map<Class<? extends Event>, List<RegisteredListener>> listeners;
    private final Map<String, Permission> permissions;
    private final List<String> failures;

    public NativePluginManager(Path pluginDirectory, Server server) {
        this.pluginDirectory = pluginDirectory;
        this.server = server;
        this.commandMap = new CommandMap();
        this.scheduler = new NativeScheduler();
        this.plugins = new LinkedHashMap<>();
        this.classLoaders = new HashMap<>();
        this.listeners = new HashMap<>();
        this.permissions = new LinkedHashMap<>();
        this.failures = new ArrayList<>();
        if (server != null) {
            Bukkit.setServer(server);
        }
    }

    public synchronized void setServer(Server server) {
        this.server = server;
        if (server != null) {
            Bukkit.setServer(server);
        }
        for (JavaPlugin plugin : plugins.values()) {
            plugin.setServer(server);
        }
    }

    public synchronized void loadPlugins() {
        try {
            Files.createDirectories(pluginDirectory);
            List<Path> jars = Files.list(pluginDirectory)
                .filter(path -> path.getFileName().toString().endsWith(".jar"))
                .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase()))
                .toList();
            Set<String> names = new HashSet<>();
            for (Path jar : jars) {
                try {
                    PluginDescriptionFile description = readDescription(jar);
                    if (!names.add(description.getName().toLowerCase())) {
                        failures.add(jar.getFileName() + ": duplicate plugin name " + description.getName());
                        logger.warn("Skipping duplicate plugin {} from {}", description.getName(), jar);
                        continue;
                    }
                    loadPlugin(jar, description);
                } catch (Throwable exception) {
                    String failure = jar.getFileName() + ": " + failureMessage(exception);
                    failures.add(failure);
                    logger.error("Unable to load plugin {}", jar.getFileName(), exception);
                }
            }
        } catch (IOException exception) {
            failures.add(pluginDirectory + ": " + exception.getMessage());
            logger.error("Unable to scan plugin directory {}", pluginDirectory, exception);
        }
    }

    private void loadPlugin(Path jar, PluginDescriptionFile description) throws Exception {
        Set<String> requiredDependencies = new LinkedHashSet<>(description.getDepend());
        requiredDependencies.addAll(description.getRequiredServerDependencies());
        for (String dependency : requiredDependencies) {
            if (!plugins.containsKey(dependency.toLowerCase())) {
                throw new IllegalStateException("Missing required dependency " + dependency);
            }
        }
        Path dataFolder = pluginDirectory.resolve(description.getName());
        Files.createDirectories(dataFolder);
        PluginClassLoader loader = new PluginClassLoader(description, jar.toFile(), dataFolder.toFile(), getClass().getClassLoader());
        JavaPlugin plugin = (JavaPlugin) loader.loadPlugin();
        plugin.setServer(server);
        plugin.setCommandMap(commandMap);
        classLoaders.put(description.getName().toLowerCase(), loader);
        registerMetadataCommands(plugin, description);
        plugin.onLoad();
        plugin.setEnabled(true);
        plugins.put(description.getName().toLowerCase(), plugin);
        logger.info("Enabled plugin {} {}", description.getName(), description.getVersion());
    }

    private PluginDescriptionFile readDescription(Path jar) throws IOException {
        try (JarFile jarFile = new JarFile(jar.toFile())) {
            var entry = jarFile.getJarEntry("plugin.yml");
            if (entry == null) {
                entry = jarFile.getJarEntry("paper-plugin.yml");
            }
            if (entry == null) {
                throw new IOException("plugin.yml or paper-plugin.yml is missing");
            }
            try (InputStream input = jarFile.getInputStream(entry)) {
                Object loaded = new Yaml().load(input);
                if (!(loaded instanceof Map<?, ?> raw)) {
                    throw new IOException("plugin metadata is not a mapping");
                }
                Map<String, Object> values = new LinkedHashMap<>();
                for (Map.Entry<?, ?> item : raw.entrySet()) {
                    if (item.getKey() != null) {
                        values.put(String.valueOf(item.getKey()), item.getValue());
                    }
                }
                return new PluginDescriptionFile(values);
            }
        }
    }

    private void registerMetadataCommands(JavaPlugin plugin, PluginDescriptionFile description) {
        for (Map.Entry<String, Map<String, Object>> entry : description.getCommands().entrySet()) {
            String label = entry.getKey().toLowerCase();
            Map<String, Object> metadata = entry.getValue();
            String commandDescription = String.valueOf(metadata.getOrDefault("description", ""));
            String usage = String.valueOf(metadata.getOrDefault("usage", ""));
            String permission = metadata.get("permission") == null ? null : String.valueOf(metadata.get("permission"));
            Command command = new Command(label, commandDescription, usage, permission);
            command.setPlugin(plugin);
            commandMap.register(label, command);
            Object aliases = metadata.get("aliases");
            if (aliases instanceof Iterable<?> iterable) {
                for (Object alias : iterable) {
                    if (alias != null) {
                        commandMap.register(String.valueOf(alias), command);
                    }
                }
            }
        }
    }

    @Override
    public synchronized void registerEvents(Listener listener, JavaPlugin plugin) {
        if (listener == null || plugin == null) {
            throw new IllegalArgumentException("listener and plugin are required");
        }
        for (Method method : listener.getClass().getMethods()) {
            EventHandler handler = method.getAnnotation(EventHandler.class);
            if (handler == null || method.getParameterCount() != 1 || !Event.class.isAssignableFrom(method.getParameterTypes()[0]) ||
                !Modifier.isPublic(method.getModifiers()) || Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            @SuppressWarnings("unchecked") Class<? extends Event> eventType = (Class<? extends Event>) method.getParameterTypes()[0];
            registerEvent(eventType, listener, handler.priority(), (target, event) -> {
                try {
                    method.invoke(target, event);
                } catch (ReflectiveOperationException exception) {
                    throw new IllegalStateException(exception);
                }
            }, plugin, handler.ignoreCancelled());
        }
    }

    @Override
    public synchronized void registerEvent(Class<? extends Event> event, Listener listener, EventPriority priority, EventExecutor executor, JavaPlugin plugin) {
        registerEvent(event, listener, priority, executor, plugin, false);
    }

    @Override
    public synchronized void registerEvent(Class<? extends Event> event, Listener listener, EventPriority priority, EventExecutor executor, JavaPlugin plugin, boolean ignoreCancelled) {
        listeners.computeIfAbsent(event, ignored -> new ArrayList<>()).add(new RegisteredListener(listener, executor, priority, plugin, ignoreCancelled));
        listeners.get(event).sort(Comparator.comparing(RegisteredListener::priority));
    }

    public synchronized void callEvent(Event event) {
        if (event == null) {
            return;
        }
        listeners.forEach((type, registered) -> {
            if (!type.isAssignableFrom(event.getClass())) {
                return;
            }
            for (RegisteredListener listener : List.copyOf(registered)) {
                if (!listener.plugin().isEnabled()) {
                    continue;
                }
                try {
                    listener.executor().execute(listener.listener(), event);
                } catch (Exception exception) {
                    logger.error("Plugin {} failed while handling {}", listener.plugin().getDescription().getName(), event.getEventName(), exception);
                }
            }
        });
    }

    @Override
    public boolean isPluginEnabled(String name) {
        JavaPlugin plugin = getPlugin(name);
        return plugin != null && plugin.isEnabled();
    }

    @Override
    public boolean isPluginEnabled(JavaPlugin plugin) {
        return plugin != null && plugin.isEnabled();
    }

    @Override
    public JavaPlugin getPlugin(String name) {
        return name == null ? null : plugins.get(name.toLowerCase());
    }

    @Override
    public JavaPlugin[] getPlugins() {
        return plugins.values().toArray(JavaPlugin[]::new);
    }

    @Override
    public void disablePlugins() {
        plugins.values().forEach(plugin -> disablePlugin(plugin));
    }

    @Override
    public synchronized void disablePlugin(JavaPlugin plugin) {
        if (plugin != null && plugin.isEnabled()) {
            scheduler.cancelTasks(plugin);
            plugin.setEnabled(false);
        }
    }

    @Override
    public void enablePlugin(JavaPlugin plugin) {
        if (plugin != null && !plugin.isEnabled()) {
            plugin.setEnabled(true);
        }
    }

    @Override
    public boolean registerCommand(String label, CommandExecutor executor, String permission, JavaPlugin plugin) {
        return registerCommand(label, executor, "", "", permission, plugin);
    }

    @Override
    public boolean registerCommand(String label, CommandExecutor executor, String description, String usageMessage, String permission, JavaPlugin plugin) {
        if (label == null || label.isBlank() || executor == null || plugin == null) {
            return false;
        }
        Command command = commandMap.getCommand(label);
        if (command == null) {
            command = new Command(label, description, usageMessage, permission);
            command.setPlugin(plugin);
            commandMap.register(label, command);
        }
        command.setExecutor(executor);
        return true;
    }

    @Override
    public Set<Permission> getPermissions() { return Set.copyOf(permissions.values()); }
    @Override
    public boolean addPermission(Permission permission) { return permission != null && permissions.putIfAbsent(permission.getName(), permission) == null; }
    @Override
    public boolean removePermission(String name) { return name != null && permissions.remove(name) != null; }
    @Override
    public boolean removePermission(Permission permission) { return permission != null && removePermission(permission.getName()); }
    @Override
    public Permission getPermission(String name) { return name == null ? null : permissions.get(name); }
    @Override
    public void recalculatePermissionDefaults(Permission permission) { }
    @Override
    public void subscribeToPermission(String name, Permissible permissible) { }
    @Override
    public void unsubscribeFromPermission(String name, Permissible permissible) { }
    @Override
    public Set<Permissible> getPermissionSubscriptions(String name) { return Set.of(); }
    @Override
    public void subscribeToDefaultPerms(boolean op, Permissible permissible) { }
    @Override
    public void unsubscribeFromDefaultPerms(boolean op, Permissible permissible) { }
    @Override
    public Set<Permissible> getDefaultPermSubscriptions(boolean op) { return Set.of(); }
    @Override
    public List<PermissionAttachmentInfo> getEffectivePermissions(Permissible permissible) { return permissible == null ? List.of() : List.copyOf(permissible.getEffectivePermissions()); }
    @Override
    public boolean addPermission(Permissible permissible, String name) { return permissible != null && permissible.hasPermission(name); }
    @Override
    public boolean removePermission(Permissible permissible, String name) { return false; }
    @Override
    public PermissionAttachment addAttachment(JavaPlugin plugin, String name, boolean value) { return null; }
    @Override
    public PermissionAttachment addAttachment(JavaPlugin plugin) { return null; }
    @Override
    public PermissionAttachment addAttachment(JavaPlugin plugin, String name, boolean value, int ticks) { return null; }
    @Override
    public PermissionAttachment addAttachment(JavaPlugin plugin, int ticks) { return null; }
    @Override
    public void removeAttachment(PermissionAttachment attachment) { if (attachment != null) attachment.getPermissible().removeAttachment(attachment); }
    @Override
    public void recalculatePermissions(Permissible permissible) { if (permissible != null) permissible.recalculatePermissions(); }
    @Override
    public void clearPlugins() { disablePlugins(); plugins.clear(); classLoaders.values().forEach(loader -> { try { loader.close(); } catch (IOException ignored) { } }); classLoaders.clear(); }

    public CommandMap getCommandMap() { return commandMap; }
    public NativeScheduler getScheduler() { return scheduler; }
    public List<String> getFailures() { return List.copyOf(failures); }

    @Override
    public void close() { clearPlugins(); scheduler.close(); }

    private String failureMessage(Throwable exception) {
        Throwable cause = exception;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getClass().getSimpleName() + ": " + cause.getMessage();
    }

    private record RegisteredListener(Listener listener, EventExecutor executor, EventPriority priority, JavaPlugin plugin, boolean ignoreCancelled) { }
}
