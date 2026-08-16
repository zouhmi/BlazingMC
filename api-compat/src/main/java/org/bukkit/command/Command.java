package org.bukkit.command;

import org.bukkit.plugin.Plugin;

public class Command {
    private final String name;
    private String description;
    private String usageMessage;
    private String permission;
    private Plugin plugin;
    
    public Command(String name) {
        this.name = name;
        this.description = "";
        this.usageMessage = "";
        this.permission = null;
    }
    
    public Command(String name, String description, String usageMessage, String permission) {
        this.name = name;
        this.description = description;
        this.usageMessage = usageMessage;
        this.permission = permission;
    }
    
    public String getName() { return name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getUsageMessage() { return usageMessage; }
    public void setUsageMessage(String usageMessage) { this.usageMessage = usageMessage; }
    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }
    public Plugin getPlugin() { return plugin; }
    public void setPlugin(Plugin plugin) { this.plugin = plugin; }
    
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        return false;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Command other = (Command) obj;
        return name.equals(other.name);
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}