package org.bukkit.permissions;

import org.bukkit.plugin.Plugin;

import java.util.Map;

public class PermissionAttachment {
    private final Plugin plugin;
    private final Permissible permissible;
    private final Map<String, Boolean> permissions;
    
    public PermissionAttachment(Plugin plugin, Permissible permissible) {
        this.plugin = plugin;
        this.permissible = permissible;
        this.permissions = new java.util.HashMap<>();
    }
    
    public Plugin getPlugin() { return plugin; }
    public Permissible getPermissible() { return permissible; }
    public Map<String, Boolean> getPermissions() { return permissions; }
    
    public void setPermission(String name, boolean value) {
        permissions.put(name, value);
        permissible.recalculatePermissions();
    }
    
    public void unsetPermission(String name) {
        permissions.remove(name);
        permissible.recalculatePermissions();
    }
    
    public void remove() {
        permissible.removeAttachment(this);
    }
}