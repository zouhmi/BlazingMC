package com.blazingmc.server.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameManagerTest {
    
    private GameManager gameManager;
    
    @BeforeEach
    void setUp() {
        gameManager = new GameManager();
    }
    
    @Test
    void testInitialValues() {
        assertEquals(0, gameManager.getWorldTime());
        assertEquals(0, gameManager.getFullTime());
        assertEquals(0, gameManager.getWeatherDuration());
        assertFalse(gameManager.isThundering());
        assertFalse(gameManager.isRaining());
        assertEquals(0, gameManager.getTimeOfDay());
    }
    
    @Test
    void testTick() {
        gameManager.tick();
        
        assertEquals(1, gameManager.getWorldTime());
        assertEquals(1, gameManager.getFullTime());
        assertEquals(1, gameManager.getTimeOfDay());
    }
    
    @Test
    void testTickMultiple() {
        for (int i = 0; i < 100; i++) {
            gameManager.tick();
        }
        
        assertEquals(100, gameManager.getWorldTime());
        assertEquals(100, gameManager.getFullTime());
        assertEquals(100, gameManager.getTimeOfDay());
    }
    
    @Test
    void testWorldTimeWraps() {
        for (int i = 0; i < 24001; i++) {
            gameManager.tick();
        }
        
        assertEquals(24001, gameManager.getFullTime());
        assertEquals(1, gameManager.getTimeOfDay());
    }
    
    @Test
    void testSetTime() {
        gameManager.setTime(1000);
        
        assertEquals(1000, gameManager.getWorldTime());
        assertEquals(1000, gameManager.getTimeOfDay());
    }
    
    @Test
    void testSetTimeLarge() {
        gameManager.setTime(100000);
        
        assertEquals(100000, gameManager.getWorldTime());
        assertEquals(4000, gameManager.getTimeOfDay());
    }
    
    @Test
    void testIsDay() {
        gameManager.setTime(0);
        assertTrue(gameManager.isDay());
        assertFalse(gameManager.isNight());
        
        gameManager.setTime(6000);
        assertTrue(gameManager.isDay());
        assertFalse(gameManager.isNight());
        
        gameManager.setTime(12000);
        assertTrue(gameManager.isDay());
        assertFalse(gameManager.isNight());
    }
    
    @Test
    void testIsNight() {
        gameManager.setTime(13000);
        assertTrue(gameManager.isNight());
        assertFalse(gameManager.isDay());
        
        gameManager.setTime(18000);
        assertTrue(gameManager.isNight());
        assertFalse(gameManager.isDay());
        
        gameManager.setTime(23999);
        assertTrue(gameManager.isNight());
        assertFalse(gameManager.isDay());
    }
    
    @Test
    void testSetWeather() {
        gameManager.setWeather(6000, true, true);
        
        assertEquals(6000, gameManager.getWeatherDuration());
        assertTrue(gameManager.isRaining());
        assertTrue(gameManager.isThundering());
    }
    
    @Test
    void testWeatherDecreases() {
        gameManager.setWeather(5, true, false);
        
        gameManager.tick();
        assertEquals(4, gameManager.getWeatherDuration());
        assertTrue(gameManager.isRaining());
        
        gameManager.tick();
        assertEquals(3, gameManager.getWeatherDuration());
        
        gameManager.tick();
        assertEquals(2, gameManager.getWeatherDuration());
        
        gameManager.tick();
        assertEquals(1, gameManager.getWeatherDuration());
        
        gameManager.tick();
        assertEquals(0, gameManager.getWeatherDuration());
        assertFalse(gameManager.isRaining());
    }
    
    @Test
    void testSunAngle() {
        gameManager.setTime(0);
        assertEquals(0.0f, gameManager.getSunAngle(), 0.01f);
        
        gameManager.setTime(6000);
        assertEquals(90.0f, gameManager.getSunAngle(), 0.01f);
        
        gameManager.setTime(12000);
        assertEquals(180.0f, gameManager.getSunAngle(), 0.01f);
        
        gameManager.setTime(18000);
        assertEquals(270.0f, gameManager.getSunAngle(), 0.01f);
    }
}
