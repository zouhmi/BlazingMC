package com.blazingmc.protocol.handler;

public interface AntiCheatManagerInterface {
    boolean validateMovement(PlayerInterface player, double newX, double newY, double newZ);
    boolean validateRotation(PlayerInterface player, float newYaw, float newPitch);
    boolean validateFlight(PlayerInterface player, double newY, boolean onGround);
    boolean validateAttackReach(PlayerInterface player, double targetX, double targetY, double targetZ);
    boolean validateClickSpeed(PlayerInterface player);
    boolean validateBlockReach(PlayerInterface player, int x, int y, int z);
    boolean validateInteractReach(PlayerInterface player, double targetX, double targetY, double targetZ);
    boolean validateBlockPlaceSpeed(PlayerInterface player);
    boolean validateBlockBreakSpeed(PlayerInterface player, int x, int y, int z);
    boolean validateHealthUpdate(PlayerInterface player, float newHealth);
    boolean validateItemPickup(PlayerInterface player, double itemX, double itemY, double itemZ);
    boolean validateEntityInteraction(PlayerInterface player, double entityX, double entityY, double entityZ);
    boolean validateFishingRod(PlayerInterface player, double targetX, double targetY, double targetZ);
    boolean validateProjectileShoot(PlayerInterface player, double targetX, double targetY, double targetZ);
    void onViolation(PlayerInterface player, String type);
    void revertPosition(PlayerInterface player);
}