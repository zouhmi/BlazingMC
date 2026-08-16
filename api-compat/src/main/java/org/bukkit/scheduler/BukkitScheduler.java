package org.bukkit.scheduler;

import org.bukkit.plugin.Plugin;

public interface BukkitScheduler {
    int scheduleSyncDelayedTask(Plugin plugin, Runnable task, long delay);
    int scheduleSyncDelayedTask(Plugin plugin, Runnable task);
    int scheduleSyncRepeatingTask(Plugin plugin, Runnable task, long delay, long period);
    int scheduleAsyncDelayedTask(Plugin plugin, Runnable task, long delay);
    int scheduleAsyncDelayedTask(Plugin plugin, Runnable task);
    int scheduleAsyncRepeatingTask(Plugin plugin, Runnable task, long delay, long period);
    void cancelTask(int taskId);
    void cancelTasks(Plugin plugin);
    boolean isCurrentlyRunning(int taskId);
    boolean isQueued(int taskId);
}