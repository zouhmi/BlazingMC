package com.blazingmc.server.tick;

import com.blazingmc.chat.ConsoleLogger;
import com.blazingmc.server.BlazingServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TickScheduler {
    private static final Logger logger = LoggerFactory.getLogger(TickScheduler.class);
    private static final long TICK_DURATION_NANOS = 50_000_000L;
    
    private final ScheduledExecutorService executor;
    private volatile boolean running;
    private long tickCount;
    private long lastTickTime;
    private BlazingServer server;
    private long tickStartTime;
    private double tps;
    
    public TickScheduler() {
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Tick-Scheduler");
            t.setDaemon(true);
            return t;
        });
        this.running = false;
        this.tickCount = 0;
        this.lastTickTime = 0;
        this.tps = 20.0;
    }
    
    public void setServer(BlazingServer server) {
        this.server = server;
    }
    
    public void start() {
        if (running) return;
        running = true;
        tickCount = 0;
        lastTickTime = System.nanoTime();
        tickStartTime = System.currentTimeMillis();
        
        executor.scheduleAtFixedRate(this::tick, 0, TICK_DURATION_NANOS, TimeUnit.NANOSECONDS);
        logger.info("Tick scheduler started at 20 TPS");
    }
    
    public void stop() {
        running = false;
        executor.shutdown();
        logger.info("Tick scheduler stopped after {} ticks", tickCount);
    }
    
    private void tick() {
        if (!running) return;
        
        long startTime = System.nanoTime();
        lastTickTime = startTime;
        tickCount++;
        
        try {
            if (server != null) {
                server.getPlayerManager().tickAll();
                server.getWorld().tick();
                server.getGameManager().tick();
                server.getSpawnManager().tick();
                server.getRedstoneManager().tick();
                server.getProjectileManager().tick();
                server.getFurnaceManager().tick();
            }
        } catch (Exception e) {
            ConsoleLogger.error("Error during tick " + tickCount, e);
        }
        
        long elapsed = System.nanoTime() - startTime;
        double tickMs = elapsed / 1_000_000.0;
        
        if (elapsed > TICK_DURATION_NANOS) {
            ConsoleLogger.warn("Tick " + tickCount + " took " + String.format("%.2f", tickMs) + "ms (target: 50ms)");
        }
        
        updateTPS(tickMs);
    }
    
    private void updateTPS(double tickMs) {
        double targetTps = 20.0;
        if (tickMs > 50) {
            tps = Math.max(0, targetTps - ((tickMs - 50) / 50.0));
        } else {
            tps = targetTps;
        }
    }
    
    public long getTickCount() { return tickCount; }
    public long getLastTickTime() { return lastTickTime; }
    public boolean isRunning() { return running; }
    public double getTPS() { return tps; }
    public long getTickStartTime() { return tickStartTime; }
}