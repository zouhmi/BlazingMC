package com.blazingmc.chat;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChatColorTest {
    
    @Test
    void testColorCodeValues() {
        assertEquals('0', ChatColor.BLACK.getCode());
        assertEquals('1', ChatColor.DARK_BLUE.getCode());
        assertEquals('2', ChatColor.DARK_GREEN.getCode());
        assertEquals('3', ChatColor.DARK_AQUA.getCode());
        assertEquals('4', ChatColor.DARK_RED.getCode());
        assertEquals('5', ChatColor.DARK_PURPLE.getCode());
        assertEquals('6', ChatColor.GOLD.getCode());
        assertEquals('7', ChatColor.GRAY.getCode());
        assertEquals('8', ChatColor.DARK_GRAY.getCode());
        assertEquals('9', ChatColor.BLUE.getCode());
        assertEquals('a', ChatColor.GREEN.getCode());
        assertEquals('b', ChatColor.AQUA.getCode());
        assertEquals('c', ChatColor.RED.getCode());
        assertEquals('d', ChatColor.LIGHT_PURPLE.getCode());
        assertEquals('e', ChatColor.YELLOW.getCode());
        assertEquals('f', ChatColor.WHITE.getCode());
    }
    
    @Test
    void testFormattingCodes() {
        assertEquals('k', ChatColor.MAGIC.getCode());
        assertEquals('l', ChatColor.BOLD.getCode());
        assertEquals('m', ChatColor.STRIKETHROUGH.getCode());
        assertEquals('n', ChatColor.UNDERLINE.getCode());
        assertEquals('o', ChatColor.ITALIC.getCode());
        assertEquals('r', ChatColor.RESET.getCode());
    }
    
    @Test
    void testToString() {
        assertEquals("\u00A70", ChatColor.BLACK.toString());
        assertEquals("\u00A7a", ChatColor.GREEN.toString());
        assertEquals("\u00A7l", ChatColor.BOLD.toString());
        assertEquals("\u00A7r", ChatColor.RESET.toString());
    }
    
    @Test
    void testStripColor() {
        assertEquals("Hello World", ChatColor.stripColor("\u00A7aHello \u00A7cWorld"));
        assertEquals("Test", ChatColor.stripColor("\u00A7l\u00A76Test"));
        assertNull(ChatColor.stripColor(null));
    }
    
    @Test
    void testTranslateAlternateColorCodes() {
        assertEquals("\u00A7aHello", ChatColor.translateAlternateColorCodes("&aHello"));
        assertEquals("\u00A7c\u00A7lError", ChatColor.translateAlternateColorCodes("&c&lError"));
        assertNull(ChatColor.translateAlternateColorCodes(null));
    }
    
    @Test
    void testToAnsi() {
        String result = ChatColor.toAnsi("\u00A7aHello");
        assertNotNull(result);
        assertTrue(result.contains("\u001B[92m"));
        assertTrue(result.contains("Hello"));
    }
    
    @Test
    void testToAnsiNull() {
        assertNull(ChatColor.toAnsi(null));
    }
    
    @Test
    void testGetMinecraftColor() {
        assertEquals("\u00A70", ChatColor.getMinecraftColor('0'));
        assertEquals("\u00A7a", ChatColor.getMinecraftColor('a'));
        assertEquals("", ChatColor.getMinecraftColor('x'));
    }
    
    @Test
    void testGetClosestColor() {
        String result = ChatColor.getClosestColor(0, 170, 0);
        assertEquals("\u00A72", result);
        
        result = ChatColor.getClosestColor(255, 255, 255);
        assertEquals("\u00A7f", result);
        
        result = ChatColor.getClosestColor(0, 0, 0);
        assertEquals("\u00A70", result);
    }
    
    @Test
    void testAnsiCodes() {
        for (ChatColor color : ChatColor.values()) {
            assertNotNull(color.getAnsiCode());
        }
    }
    
    @Test
    void testColorEnumCount() {
        assertEquals(22, ChatColor.values().length);
    }
}
