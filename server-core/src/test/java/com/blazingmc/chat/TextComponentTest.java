package com.blazingmc.chat;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TextComponentTest {
    
    @Test
    void testBasicCreation() {
        TextComponent tc = new TextComponent("Hello");
        
        assertEquals("Hello", tc.getText());
        assertNull(tc.getColor());
        assertFalse(tc.isBold());
        assertFalse(tc.isItalic());
        assertFalse(tc.isUnderlined());
        assertFalse(tc.isStrikethrough());
        assertFalse(tc.isObfuscated());
        assertTrue(tc.getExtra().isEmpty());
    }
    
    @Test
    void testCreationWithColor() {
        TextComponent tc = new TextComponent("Hello", ChatColor.RED);
        
        assertEquals("Hello", tc.getText());
        assertEquals(ChatColor.RED, tc.getColor());
    }
    
    @Test
    void testFluentSetters() {
        TextComponent tc = new TextComponent("Test");
        
        TextComponent result = tc.setText("Changed")
                                 .setColor(ChatColor.GREEN)
                                 .setBold(true)
                                 .setItalic(true)
                                 .setUnderlined(true)
                                 .setStrikethrough(true)
                                 .setObfuscated(true);
        
        assertEquals("Changed", result.getText());
        assertEquals(ChatColor.GREEN, result.getColor());
        assertTrue(result.isBold());
        assertTrue(result.isItalic());
        assertTrue(result.isUnderlined());
        assertTrue(result.isStrikethrough());
        assertTrue(result.isObfuscated());
        assertSame(tc, result);
    }
    
    @Test
    void testAddExtraString() {
        TextComponent tc = new TextComponent("Hello");
        tc.addExtra(" World");
        
        assertEquals(1, tc.getExtra().size());
        assertEquals(" World", tc.getExtra().get(0).getText());
    }
    
    @Test
    void testAddExtraComponent() {
        TextComponent tc = new TextComponent("Hello");
        TextComponent extra = new TextComponent(" World", ChatColor.GREEN);
        tc.addExtra(extra);
        
        assertEquals(1, tc.getExtra().size());
        assertSame(extra, tc.getExtra().get(0));
    }
    
    @Test
    void testToLegacyText() {
        TextComponent tc = new TextComponent("Hello");
        assertEquals("Hello", tc.toLegacyText());
    }
    
    @Test
    void testToLegacyTextColor() {
        TextComponent tc = new TextComponent("Hello", ChatColor.RED);
        String result = tc.toLegacyText();
        
        assertTrue(result.startsWith("\u00A7c"));
        assertTrue(result.endsWith("Hello"));
    }
    
    @Test
    void testToLegacyTextBold() {
        TextComponent tc = new TextComponent("Hello");
        tc.setBold(true);
        String result = tc.toLegacyText();
        
        assertTrue(result.contains("\u00A7l"));
        assertTrue(result.contains("Hello"));
    }
    
    @Test
    void testToLegacyTextExtra() {
        TextComponent tc = new TextComponent("Hello");
        tc.addExtra(" World");
        
        String result = tc.toLegacyText();
        assertEquals("Hello World", result);
    }
    
    @Test
    void testToJson() {
        TextComponent tc = new TextComponent("Hello");
        String json = tc.toJson();
        
        assertTrue(json.startsWith("{\"text\":\""));
        assertTrue(json.contains("Hello"));
        assertTrue(json.endsWith("}"));
    }
    
    @Test
    void testToJsonColor() {
        TextComponent tc = new TextComponent("Hello", ChatColor.RED);
        String json = tc.toJson();
        
        assertTrue(json.contains("\"color\":\"red\""));
    }
    
    @Test
    void testToJsonBold() {
        TextComponent tc = new TextComponent("Hello");
        tc.setBold(true);
        String json = tc.toJson();
        
        assertTrue(json.contains("\"bold\":true"));
    }
    
    @Test
    void testToJsonExtra() {
        TextComponent tc = new TextComponent("Hello");
        tc.addExtra(" World");
        String json = tc.toJson();
        
        assertTrue(json.contains("\"extra\":["));
        assertTrue(json.contains("\"text\":\" World\""));
    }
    
    @Test
    void testToJsonEscape() {
        TextComponent tc = new TextComponent("He said \"hi\"");
        String json = tc.toJson();
        
        assertTrue(json.contains("He said \\\"hi\\\""));
    }
    
    @Test
    void testFromLegacy() {
        TextComponent tc = TextComponent.fromLegacy("Hello");
        
        assertNotNull(tc);
    }
    
    @Test
    void testFromLegacyWithColor() {
        TextComponent tc = TextComponent.fromLegacy("\u00A7aHello");
        
        assertNotNull(tc);
    }
    
    @Test
    void testFromLegacyWithReset() {
        TextComponent tc = TextComponent.fromLegacy("\u00A7aHello\u00A7rWorld");
        
        assertNotNull(tc);
    }
    
    @Test
    void testFromLegacyWithBold() {
        TextComponent tc = TextComponent.fromLegacy("\u00A7lBold");
        
        assertNotNull(tc);
    }
    
    @Test
    void testMultipleExtras() {
        TextComponent tc = new TextComponent("A");
        tc.addExtra("B");
        tc.addExtra("C");
        tc.addExtra("D");
        
        assertEquals(3, tc.getExtra().size());
        
        String result = tc.toLegacyText();
        assertEquals("ABCD", result);
    }
    
    @Test
    void testNestedExtra() {
        TextComponent tc = new TextComponent("Main");
        TextComponent nested = new TextComponent(" Nested");
        nested.setColor(ChatColor.GREEN);
        tc.addExtra(nested);
        
        assertEquals(1, tc.getExtra().size());
        assertEquals(ChatColor.GREEN, tc.getExtra().get(0).getColor());
    }
}
