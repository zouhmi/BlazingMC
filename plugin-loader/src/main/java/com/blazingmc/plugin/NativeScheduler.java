package com.blazingmc.plugin;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class NativeScheduler implements BukkitScheduler, AutoCloseable {
    private static final long TICK_MILLIS = 50L;
    private final ScheduledExecutorService syncExecutor;
    private final ScheduledExecutorService asyncExecutor;
    private final AtomicInteger nextId;
    private final Map<Integer, Task> tasks;

    public NativeScheduler() {
        syncExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "BlazingMC-Plugin-Scheduler");
            thread.setDaemon(true);
            return thread;
        });
        asyncExecutor = Executors.newScheduledThreadPool(2, r -> {
            Thread thread = new Thread(r, "BlazingMC-Plugin-Async");
            thread.setDaemon(true);
            return thread;
        });
        nextId = new AtomicInteger(1);
        tasks = new ConcurrentHashMap<>();
    }

    @Override
    public int scheduleSyncDelayedTask(Plugin plugin, Runnable task, long delay) {
        return schedule(plugin, task, delay, -1, syncExecutor);
    }

    @Override
    public int scheduleSyncDelayedTask(Plugin plugin, Runnable task) {
        return scheduleSyncDelayedTask(plugin, task, 0);
    }

    @Override
    public int scheduleSyncRepeatingTask(Plugin plugin, Runnable task, long delay, long period) {
        return schedule(plugin, task, delay, period, syncExecutor);
    }

    @Override
    public int scheduleAsyncDelayedTask(Plugin plugin, Runnable task, long delay) {
        return schedule(plugin, task, delay, -1, asyncExecutor);
    }

    @Override
    public int scheduleAsyncDelayedTask(Plugin plugin, Runnable task) {
        return scheduleAsyncDelayedTask(plugin, task, 0);
    }

    @Override
    public int scheduleAsyncRepeatingTask(Plugin plugin, Runnable task, long delay, long period) {
        return schedule(plugin, task, delay, period, asyncExecutor);
    }

    @Override
    public void cancelTask(int taskId) {
        Task task = tasks.remove(taskId);
        if (task != null) {
            task.future().cancel(false);
        }
    }

    @Override
    public void cancelTasks(Plugin plugin) {
        tasks.values().removeIf(task -> {
            if (task.plugin() == plugin) {
                task.future().cancel(false);
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean isCurrentlyRunning(int taskId) {
        Task task = tasks.get(taskId);
        return task != null && !task.future().isDone() && !task.future().isCancelled();
    }

    @Override
    public boolean isQueued(int taskId) {
        return isCurrentlyRunning(taskId);
    }

    private int schedule(Plugin plugin, Runnable task, long delay, long period, ScheduledExecutorService executor) {
        if (plugin == null || task == null) {
            throw new IllegalArgumentException("plugin and task are required");
        }
        if (delay < 0 || (period == 0) || period < -1) {
            throw new IllegalArgumentException("invalid scheduler delay or period");
        }
        int id = nextId.getAndIncrement();
        long delayMillis = delay * TICK_MILLIS;
        ScheduledFuture<?> future;
        if (period > 0) {
            future = executor.scheduleAtFixedRate(task, delayMillis, period * TICK_MILLIS, TimeUnit.MILLISECONDS);
        } else {
            future = executor.schedule(() -> {
                try {
                    task.run();
                } finally {
                    tasks.remove(id);
                }
            }, delayMillis, TimeUnit.MILLISECONDS);
        }
        tasks.put(id, new Task(plugin, future));
        return id;
    }

    @Override
    public void close() {
        tasks.values().forEach(task -> task.future().cancel(false));
        tasks.clear();
        syncExecutor.shutdownNow();
        asyncExecutor.shutdownNow();
    }

    private record Task(Plugin plugin, ScheduledFuture<?> future) {
    }
}
