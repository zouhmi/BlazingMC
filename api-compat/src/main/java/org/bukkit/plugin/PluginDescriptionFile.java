package org.bukkit.plugin;

import java.util.List;
import java.util.Map;

public class PluginDescriptionFile {
    private String name;
    private String main;
    private String version;
    private String description;
    private String author;
    private List<String> authors;
    private String website;
    private String prefix;
    private boolean apiVersion;
    private Map<String, Map<String, Object>> commands;
    private List<String> depend;
    private List<String> softDepend;
    private List<String> loadBefore;
    private String database;
    private List<String> libraries;
    
    public PluginDescriptionFile(String name, String version, String main) {
        this.name = name;
        this.version = version;
        this.main = main;
    }
    
    public String getName() { return name; }
    public String getMain() { return main; }
    public String getVersion() { return version; }
    public String getDescription() { return description; }
    public String getAuthor() { return author; }
    public List<String> getAuthors() { return authors; }
    public String getWebsite() { return website; }
    public String getPrefix() { return prefix; }
    public boolean isApiVersion() { return apiVersion; }
    public Map<String, Map<String, Object>> getCommands() { return commands; }
    public List<String> getDepend() { return depend; }
    public List<String> getSoftDepend() { return softDepend; }
    public List<String> getLoadBefore() { return loadBefore; }
    public String getDatabase() { return database; }
    public List<String> getLibraries() { return libraries; }
}