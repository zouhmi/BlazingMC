package org.bukkit.event;

import org.bukkit.plugin.Plugin;

public interface EventExecutor {
    void execute(Listener listener, Event event);
}