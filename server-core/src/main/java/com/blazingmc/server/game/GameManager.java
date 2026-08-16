package com.blazingmc.server.game;

import com.blazingmc.chat.ConsoleLogger;

public class GameManager {
    private long worldTime;
    private long fullTime;
    private int weatherDuration;
    private boolean thundering;
    private boolean raining;
    private int timeOfDay;
    private int gameRule_dayCycle;
    private boolean gameRule_doDaylightCycle;
    private boolean gameRule_doWeatherCycle;
    private boolean gameRule_doMobSpawning;
    private boolean gameRule_doTileDrops;
    private boolean gameRule_doFireTick;
    private int gameRule_spawnRadius;
    
    public GameManager() {
        this.worldTime = 0;
        this.fullTime = 0;
        this.weatherDuration = 0;
        this.thundering = false;
        this.raining = false;
        this.timeOfDay = 0;
        this.gameRule_dayCycle = 24000;
        this.gameRule_doDaylightCycle = true;
        this.gameRule_doWeatherCycle = true;
        this.gameRule_doMobSpawning = true;
        this.gameRule_doTileDrops = true;
        this.gameRule_doFireTick = true;
        this.gameRule_spawnRadius = 10;
    }
    
    public void tick() {
        if (gameRule_doDaylightCycle) {
            worldTime++;
            fullTime++;
            timeOfDay = (int) (worldTime % gameRule_dayCycle);
        }
        
        if (gameRule_doWeatherCycle && weatherDuration > 0) {
            weatherDuration--;
            if (weatherDuration <= 0) {
                raining = false;
                thundering = false;
                ConsoleLogger.info("Weather changed to clear");
            }
        }
        
        if (timeOfDay == 0) {
            ConsoleLogger.info("Time: Dawn");
        } else if (timeOfDay == 6000) {
            ConsoleLogger.info("Time: Noon");
        } else if (timeOfDay == 12000) {
            ConsoleLogger.info("Time: Dusk");
        } else if (timeOfDay == 13000) {
            ConsoleLogger.info("Time: Night");
        }
    }
    
    public void setTime(long time) {
        this.worldTime = time;
        this.timeOfDay = (int) (time % gameRule_dayCycle);
    }
    
    public void setWeather(int duration, boolean raining, boolean thundering) {
        this.weatherDuration = duration;
        this.raining = raining;
        this.thundering = thundering;
    }
    
    public long getWorldTime() { return worldTime; }
    public long getFullTime() { return fullTime; }
    public int getWeatherDuration() { return weatherDuration; }
    public boolean isThundering() { return thundering; }
    public boolean isRaining() { return raining; }
    public int getTimeOfDay() { return timeOfDay; }
    
    public boolean isDay() {
        return timeOfDay >= 0 && timeOfDay < 13000;
    }
    
    public boolean isNight() {
        return timeOfDay >= 13000;
    }
    
    public float getSunAngle() {
        float angle = (float) timeOfDay / gameRule_dayCycle;
        return angle * 360;
    }
}