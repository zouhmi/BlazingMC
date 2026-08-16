package org.bukkit.configuration.file;

public class FileConfigurationOptions {
    private final FileConfiguration configuration;
    private boolean copyDefaults;
    private boolean parseComments;
    private boolean copyHeader;
    private char pathSeparator;

    public FileConfigurationOptions(FileConfiguration configuration) {
        this.configuration = configuration;
        this.pathSeparator = '.';
    }

    public FileConfiguration configuration() { return configuration; }
    public FileConfigurationOptions copyDefaults(boolean value) { copyDefaults = value; return this; }
    public boolean copyDefaults() { return copyDefaults; }
    public FileConfigurationOptions parseComments(boolean value) { parseComments = value; return this; }
    public boolean parseComments() { return parseComments; }
    public FileConfigurationOptions copyHeader(boolean value) { copyHeader = value; return this; }
    public boolean copyHeader() { return copyHeader; }
    public FileConfigurationOptions pathSeparator(char value) { pathSeparator = value; return this; }
    public char pathSeparator() { return pathSeparator; }
}
