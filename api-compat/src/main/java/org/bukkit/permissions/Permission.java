package org.bukkit.permissions;

import java.util.Set;

public class Permission {
    private final String name;
    private String description;
    private final PermissionDefault defaultValue;
    private final java.util.Map<String, Boolean> children;
    
    public Permission(String name) {
        this(name, "", PermissionDefault.OP, new java.util.HashMap<>());
    }
    
    public Permission(String name, String description) {
        this(name, description, PermissionDefault.OP, new java.util.HashMap<>());
    }
    
    public Permission(String name, String description, PermissionDefault defaultValue) {
        this(name, description, defaultValue, new java.util.HashMap<>());
    }
    
    public Permission(String name, String description, PermissionDefault defaultValue, java.util.Map<String, Boolean> children) {
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
        this.children = children;
    }
    
    public String getName() { return name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public PermissionDefault getDefault() { return defaultValue; }
    public java.util.Map<String, Boolean> getChildren() { return children; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Permission other = (Permission) obj;
        return name.equals(other.name);
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}