package org.bukkit.plugin;

import java.util.ArrayList;
import java.util.Collections;
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
    private List<String> requiredServerDependencies;

    public PluginDescriptionFile(String name, String version, String main) {
        this.name = name;
        this.version = version;
        this.main = main;
        this.authors = new ArrayList<>();
        this.depend = new ArrayList<>();
        this.softDepend = new ArrayList<>();
        this.loadBefore = new ArrayList<>();
        this.libraries = new ArrayList<>();
        this.requiredServerDependencies = new ArrayList<>();
    }

    public PluginDescriptionFile(Map<String, Object> values) {
        this(
            stringValue(values.get("name"), "UnknownPlugin"),
            stringValue(values.get("version"), "unknown"),
            stringValue(values.get("main"), "")
        );
        description = stringValue(values.get("description"), "");
        author = stringValue(values.get("author"), "");
        website = stringValue(values.get("website"), "");
        prefix = stringValue(values.get("prefix"), name);
        apiVersion = values.get("api-version") != null;
        commands = mapValue(values.get("commands"));
        authors = stringList(values.get("authors"));
        if (authors.isEmpty() && !author.isBlank()) {
            authors = List.of(author);
        }
        depend = stringList(values.get("depend"));
        softDepend = stringList(values.get("softdepend"));
        loadBefore = stringList(values.get("loadbefore"));
        database = stringValue(values.get("database"), "");
        libraries = stringList(values.get("libraries"));
        requiredServerDependencies = requiredServerDependencies(values.get("dependencies"));
    }

    public String getName() { return name; }
    public String getMain() { return main; }
    public String getVersion() { return version; }
    public String getDescription() { return description; }
    public String getAuthor() { return author; }
    public List<String> getAuthors() { return Collections.unmodifiableList(authors); }
    public String getWebsite() { return website; }
    public String getPrefix() { return prefix; }
    public boolean isApiVersion() { return apiVersion; }
    public Map<String, Map<String, Object>> getCommands() { return commands == null ? Collections.emptyMap() : Collections.unmodifiableMap(commands); }
    public List<String> getDepend() { return Collections.unmodifiableList(depend); }
    public List<String> getSoftDepend() { return Collections.unmodifiableList(softDepend); }
    public List<String> getLoadBefore() { return Collections.unmodifiableList(loadBefore); }
    public String getDatabase() { return database; }
    public List<String> getLibraries() { return Collections.unmodifiableList(libraries); }
    public List<String> getRequiredServerDependencies() { return Collections.unmodifiableList(requiredServerDependencies); }

    private static String stringValue(Object value, String fallback) {
        return value == null ? fallback : String.valueOf(value);
    }

    private static List<String> stringList(Object value) {
        if (value instanceof Iterable<?> iterable) {
            List<String> result = new ArrayList<>();
            for (Object item : iterable) {
                if (item != null) {
                    result.add(String.valueOf(item));
                }
            }
            return result;
        }
        if (value == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(List.of(String.valueOf(value)));
    }

    private static List<String> requiredServerDependencies(Object value) {
        if (!(value instanceof Map<?, ?> dependencies)) {
            return new ArrayList<>();
        }
        Object server = dependencies.get("server");
        if (!(server instanceof Map<?, ?> serverDependencies)) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        for (Map.Entry<?, ?> entry : serverDependencies.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            if (entry.getValue() instanceof Map<?, ?> metadata && Boolean.FALSE.equals(metadata.get("required"))) {
                continue;
            }
            result.add(String.valueOf(entry.getKey()));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Map<String, Object>> mapValue(Object value) {
        if (!(value instanceof Map<?, ?> map)) {
            return Collections.emptyMap();
        }
        Map<String, Map<String, Object>> result = new java.util.LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() != null && entry.getValue() instanceof Map<?, ?> child) {
                result.put(String.valueOf(entry.getKey()), (Map<String, Object>) child);
            }
        }
        return result;
    }
}
