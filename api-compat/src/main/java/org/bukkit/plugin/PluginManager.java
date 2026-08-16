package org.bukkit.plugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.EventExecutor;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Set;

public interface PluginManager {
    void registerEvents(Listener listener, JavaPlugin plugin);
    void registerEvent(Class<? extends Event> event, Listener listener, EventPriority priority, EventExecutor executor, JavaPlugin plugin);
    void registerEvent(Class<? extends Event> event, Listener listener, EventPriority priority, EventExecutor executor, JavaPlugin plugin, boolean ignoreCancelled);
    void callEvent(Event event);
    boolean isPluginEnabled(String name);
    boolean isPluginEnabled(JavaPlugin plugin);
    JavaPlugin getPlugin(String name);
    JavaPlugin[] getPlugins();
    void disablePlugins();
    void disablePlugin(JavaPlugin plugin);
    void enablePlugin(JavaPlugin plugin);
    boolean registerCommand(String label, CommandExecutor executor, String permission, JavaPlugin plugin);
    boolean registerCommand(String label, CommandExecutor executor, String description, String usageMessage, String permission, JavaPlugin plugin);
    Set<Permission> getPermissions();
    boolean addPermission(Permission perm);
    boolean removePermission(String name);
    boolean removePermission(Permission perm);
    Permission getPermission(String name);
    void recalculatePermissionDefaults(Permission perm);
    void subscribeToPermission(String name, Permissible permissible);
    void unsubscribeFromPermission(String name, Permissible permissible);
    Set<Permissible> getPermissionSubscriptions(String name);
    void subscribeToDefaultPerms(boolean op, Permissible permissible);
    void unsubscribeFromDefaultPerms(boolean op, Permissible permissible);
    Set<Permissible> getDefaultPermSubscriptions(boolean op);
    List<PermissionAttachmentInfo> getEffectivePermissions(Permissible permissible);
    boolean addPermission(Permissible permissible, String name);
    boolean removePermission(Permissible permissible, String name);
    PermissionAttachment addAttachment(JavaPlugin plugin, String name, boolean value);
    PermissionAttachment addAttachment(JavaPlugin plugin);
    PermissionAttachment addAttachment(JavaPlugin plugin, String name, boolean value, int ticks);
    PermissionAttachment addAttachment(JavaPlugin plugin, int ticks);
    void removeAttachment(PermissionAttachment attachment);
    void recalculatePermissions(Permissible permissible);
    void clearPlugins();
}