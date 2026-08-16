package org.bukkit.permissions;

public class PermissionAttachmentInfo {
    private final Permissible permissible;
    private final String permission;
    private final boolean value;
    private final boolean explicit;
    
    public PermissionAttachmentInfo(Permissible permissible, String permission, boolean value, boolean explicit) {
        this.permissible = permissible;
        this.permission = permission;
        this.value = value;
        this.explicit = explicit;
    }
    
    public Permissible getPermissible() { return permissible; }
    public String getPermission() { return permission; }
    public boolean getValue() { return value; }
    public boolean isExplicit() { return explicit; }
}