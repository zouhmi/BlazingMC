package org.bukkit.permissions;

import org.bukkit.plugin.Plugin;

import java.util.Set;

public interface Permissible {
    String getName();
    boolean isOp();
    void setOp(boolean value);
    boolean hasPermission(String permission);
    boolean hasPermission(Permission perm);
    PermissionAttachment addAttachment(Plugin plugin, String name, boolean value);
    PermissionAttachment addAttachment(Plugin plugin);
    PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks);
    PermissionAttachment addAttachment(Plugin plugin, int ticks);
    void removeAttachment(PermissionAttachment attachment);
    void recalculatePermissions();
    Set<PermissionAttachmentInfo> getEffectivePermissions();
}