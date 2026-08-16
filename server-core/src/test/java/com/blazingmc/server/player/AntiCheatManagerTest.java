package com.blazingmc.server.player;

import com.blazingmc.protocol.handler.PlayerInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AntiCheatManagerTest {
    
    private AntiCheatManager antiCheatManager;
    private PlayerManager playerManager;
    
    @BeforeEach
    void setUp() {
        playerManager = mock(PlayerManager.class);
        antiCheatManager = new AntiCheatManager(playerManager);
    }
    
    private PlayerInterface mockPlayer() {
        PlayerInterface player = mock(PlayerInterface.class);
        when(player.getUsername()).thenReturn("TestPlayer");
        when(player.getLastValidX()).thenReturn(0.0);
        when(player.getLastValidY()).thenReturn(64.0);
        when(player.getLastValidZ()).thenReturn(0.0);
        return player;
    }
    
    @Test
    void testValidateMovementNormal() {
        PlayerInterface player = mockPlayer();
        when(player.getLastMovementTime()).thenReturn(System.currentTimeMillis() - 1000);
        when(player.getX()).thenReturn(0.0);
        when(player.getY()).thenReturn(64.0);
        when(player.getZ()).thenReturn(0.0);
        when(player.isOnGround()).thenReturn(true);
        
        boolean valid = antiCheatManager.validateMovement(player, 1.0, 64.0, 1.0);
        
        assertTrue(valid);
    }
    
    @Test
    void testValidateMovementNullPlayer() {
        boolean valid = antiCheatManager.validateMovement(null, 0, 0, 0);
        
        assertTrue(valid);
    }
    
    @Test
    void testValidateMovementFirstMove() {
        PlayerInterface player = mockPlayer();
        when(player.getLastMovementTime()).thenReturn(0L);
        
        boolean valid = antiCheatManager.validateMovement(player, 1.0, 64.0, 1.0);
        
        assertTrue(valid);
    }
    
    @Test
    void testValidateMovementTooFast() {
        PlayerInterface player = mockPlayer();
        when(player.getLastMovementTime()).thenReturn(System.currentTimeMillis() - 200);
        when(player.getX()).thenReturn(0.0);
        when(player.getY()).thenReturn(64.0);
        when(player.getZ()).thenReturn(0.0);
        when(player.isOnGround()).thenReturn(true);
        
        boolean valid = antiCheatManager.validateMovement(player, 100.0, 64.0, 100.0);
        
        assertFalse(valid);
    }
    
    @Test
    void testValidateMovementHorizontalDistance() {
        PlayerInterface player = mockPlayer();
        when(player.getLastMovementTime()).thenReturn(System.currentTimeMillis() - 100);
        when(player.getX()).thenReturn(0.0);
        when(player.getY()).thenReturn(64.0);
        when(player.getZ()).thenReturn(0.0);
        when(player.isOnGround()).thenReturn(true);
        
        boolean valid = antiCheatManager.validateMovement(player, 10.0, 64.0, 0.0);
        
        assertFalse(valid);
    }
    
    @Test
    void testValidateMovementVerticalDistance() {
        PlayerInterface player = mockPlayer();
        when(player.getLastMovementTime()).thenReturn(System.currentTimeMillis() - 100);
        when(player.getX()).thenReturn(0.0);
        when(player.getY()).thenReturn(64.0);
        when(player.getZ()).thenReturn(0.0);
        when(player.isOnGround()).thenReturn(true);
        
        boolean valid = antiCheatManager.validateMovement(player, 0.0, 70.0, 0.0);
        
        assertFalse(valid);
    }
    
    @Test
    void testValidateMovementSamePosition() {
        PlayerInterface player = mockPlayer();
        when(player.getLastMovementTime()).thenReturn(System.currentTimeMillis() - 1000);
        when(player.getX()).thenReturn(5.0);
        when(player.getY()).thenReturn(64.0);
        when(player.getZ()).thenReturn(5.0);
        when(player.isOnGround()).thenReturn(true);
        
        boolean valid = antiCheatManager.validateMovement(player, 5.0, 64.0, 5.0);
        
        assertTrue(valid);
    }
    
    @Test
    void testValidateMovementSmallStep() {
        PlayerInterface player = mockPlayer();
        when(player.getLastMovementTime()).thenReturn(System.currentTimeMillis() - 1000);
        when(player.getX()).thenReturn(0.0);
        when(player.getY()).thenReturn(64.0);
        when(player.getZ()).thenReturn(0.0);
        when(player.isOnGround()).thenReturn(true);
        
        boolean valid = antiCheatManager.validateMovement(player, 0.1, 64.0, 0.1);
        
        assertTrue(valid);
    }
    
    @Test
    void testValidateRotationNormal() {
        PlayerInterface player = mockPlayer();
        when(player.getYaw()).thenReturn(0.0f);
        when(player.getPitch()).thenReturn(0.0f);
        
        boolean valid = antiCheatManager.validateRotation(player, 45.0f, 30.0f);
        
        assertTrue(valid);
    }
    
    @Test
    void testValidateRotationNullPlayer() {
        boolean valid = antiCheatManager.validateRotation(null, 0, 0);
        
        assertTrue(valid);
    }
    
    @Test
    void testValidateRotationInvalidPitchTooHigh() {
        PlayerInterface player = mockPlayer();
        when(player.getYaw()).thenReturn(0.0f);
        when(player.getPitch()).thenReturn(0.0f);
        
        boolean valid = antiCheatManager.validateRotation(player, 0.0f, 91.0f);
        
        assertFalse(valid);
    }
    
    @Test
    void testValidateRotationInvalidPitchTooLow() {
        PlayerInterface player = mockPlayer();
        when(player.getYaw()).thenReturn(0.0f);
        when(player.getPitch()).thenReturn(0.0f);
        
        boolean valid = antiCheatManager.validateRotation(player, 0.0f, -91.0f);
        
        assertFalse(valid);
    }
    
    @Test
    void testValidateRotationMaxPitch() {
        PlayerInterface player = mockPlayer();
        when(player.getYaw()).thenReturn(0.0f);
        when(player.getPitch()).thenReturn(0.0f);
        
        boolean valid = antiCheatManager.validateRotation(player, 0.0f, 90.0f);
        
        assertTrue(valid);
    }
    
    @Test
    void testValidateRotationMinPitch() {
        PlayerInterface player = mockPlayer();
        when(player.getYaw()).thenReturn(0.0f);
        when(player.getPitch()).thenReturn(0.0f);
        
        boolean valid = antiCheatManager.validateRotation(player, 0.0f, -90.0f);
        
        assertTrue(valid);
    }
    
    @Test
    void testValidateRotationFullYaw() {
        PlayerInterface player = mockPlayer();
        when(player.getYaw()).thenReturn(0.0f);
        when(player.getPitch()).thenReturn(0.0f);
        
        boolean valid = antiCheatManager.validateRotation(player, 360.0f, 0.0f);
        
        assertTrue(valid);
    }
    
    @Test
    void testValidateFlightNormal() {
        PlayerInterface player = mockPlayer();
        when(player.isOnGround()).thenReturn(true);
        
        boolean valid = antiCheatManager.validateFlight(player, 64.0, true);
        
        assertTrue(valid);
    }
    
    @Test
    void testValidateFlightFlying() {
        PlayerInterface player = mockPlayer();
        when(player.isOnGround()).thenReturn(false);
        
        boolean valid = antiCheatManager.validateFlight(player, 70.0, false);
        
        assertFalse(valid);
    }
    
    @Test
    void testValidateFlightCreative() {
        PlayerInterface player = mock(PlayerInterface.class);
        when(player.getUsername()).thenReturn("TestPlayer");
        when(player.isCreativeMode()).thenReturn(true);
        
        boolean valid = antiCheatManager.validateFlight(player, 70.0, false);
        
        assertTrue(valid);
    }
    
    @Test
    void testValidateAttackReachClose() {
        PlayerInterface player = mockPlayer();
        when(player.getX()).thenReturn(0.0);
        when(player.getY()).thenReturn(64.0);
        when(player.getZ()).thenReturn(0.0);
        
        boolean valid = antiCheatManager.validateAttackReach(player, 2.0, 64.0, 2.0);
        
        assertTrue(valid);
    }
    
    @Test
    void testValidateAttackReachTooFar() {
        PlayerInterface player = mockPlayer();
        when(player.getX()).thenReturn(0.0);
        when(player.getY()).thenReturn(64.0);
        when(player.getZ()).thenReturn(0.0);
        
        boolean valid = antiCheatManager.validateAttackReach(player, 10.0, 64.0, 10.0);
        
        assertFalse(valid);
    }
    
    @Test
    void testValidateBlockReachClose() {
        PlayerInterface player = mockPlayer();
        when(player.getX()).thenReturn(0.0);
        when(player.getY()).thenReturn(64.0);
        when(player.getZ()).thenReturn(0.0);
        
        boolean valid = antiCheatManager.validateBlockReach(player, 2, 64, 2);
        
        assertTrue(valid);
    }
    
    @Test
    void testValidateBlockReachTooFar() {
        PlayerInterface player = mockPlayer();
        when(player.getX()).thenReturn(0.0);
        when(player.getY()).thenReturn(64.0);
        when(player.getZ()).thenReturn(0.0);
        
        boolean valid = antiCheatManager.validateBlockReach(player, 10, 64, 10);
        
        assertFalse(valid);
    }
    
    @Test
    void testValidateClickSpeedNormal() {
        PlayerInterface player = mockPlayer();
        
        boolean valid = antiCheatManager.validateClickSpeed(player);
        
        assertTrue(valid);
    }
    
    @Test
    void testOnViolation() {
        PlayerInterface player = mockPlayer();
        when(player.getUuid()).thenReturn(java.util.UUID.randomUUID());
        
        assertDoesNotThrow(() -> antiCheatManager.onViolation(player, "test"));
    }
    
    @Test
    void testRevertPosition() {
        PlayerInterface player = mockPlayer();
        
        assertDoesNotThrow(() -> antiCheatManager.revertPosition(player));
    }
}
