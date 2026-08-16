package com.blazingmc.protocol.handler;

import java.util.UUID;

public interface PlayerInterface {
    UUID getUuid();
    String getUsername();
    String getDisplayName();
    double getX();
    double getY();
    double getZ();
    float getYaw();
    float getPitch();
    boolean isOnGround();
    int getEntityId();
    int getGameMode();
    void setGameMode(int gameMode);
    int getMainHand();
    void setMainHand(int slot);
    void teleport(double x, double y, double z, float yaw, float pitch);
    void sendPacket(int packetId, byte[] data);
    void sendChatMessage(String message);
    void disconnect(String reason);
    
    void setPosition(double x, double y, double z);
    void setRotation(float yaw, float pitch);
    void setOnGround(boolean onGround);
    
    long getLastMovementTime();
    void setLastMovementTime(long time);
    
    double getMovementSpeed();
    void setMovementSpeed(double speed);
    
    boolean isSprinting();
    void setSprinting(boolean sprinting);
    
    boolean isCreativeMode();
    boolean isAllowFlying();
    
    double getLastValidX();
    double getLastValidY();
    double getLastValidZ();
    void setLastValidPosition(double x, double y, double z);
}