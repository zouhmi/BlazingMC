package com.blazingmc.protocol.handler;

public interface WorldInterface {
    String getName();
    long getSeed();
    long getTime();
    void setTime(long time);
    long getFullTime();
    void setFullTime(long fullTime);
    int getWeatherDuration();
    void setWeatherDuration(int weatherDuration);
    int getHighestBlockY(int x, int z);
}