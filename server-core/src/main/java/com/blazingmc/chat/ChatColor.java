package com.blazingmc.chat;

public enum ChatColor {
    BLACK('0', "\u001B[30m"),
    DARK_BLUE('1', "\u001B[34m"),
    DARK_GREEN('2', "\u001B[32m"),
    DARK_AQUA('3', "\u001B[36m"),
    DARK_RED('4', "\u001B[31m"),
    DARK_PURPLE('5', "\u001B[35m"),
    GOLD('6', "\u001B[33m"),
    GRAY('7', "\u001B[37m"),
    DARK_GRAY('8', "\u001B[90m"),
    BLUE('9', "\u001B[94m"),
    GREEN('a', "\u001B[92m"),
    AQUA('b', "\u001B[96m"),
    RED('c', "\u001B[91m"),
    LIGHT_PURPLE('d', "\u001B[95m"),
    YELLOW('e', "\u001B[93m"),
    WHITE('f', "\u001B[97m"),
    
    MAGIC('k', ""),
    BOLD('l', "\u001B[1m"),
    STRIKETHROUGH('m', "\u001B[9m"),
    UNDERLINE('n', "\u001B[4m"),
    ITALIC('o', "\u001B[3m"),
    RESET('r', "\u001B[0m");
    
    private static final char SECTION_SIGN = '\u00A7';
    private static final String CONSOLE_RESET = "\u001B[0m";
    
    private final char code;
    private final String ansiCode;
    
    ChatColor(char code, String ansiCode) {
        this.code = code;
        this.ansiCode = ansiCode;
    }
    
    public char getCode() {
        return code;
    }
    
    public String getAnsiCode() {
        return ansiCode;
    }
    
    public String toString() {
        return String.valueOf(SECTION_SIGN) + code;
    }
    
    public static String stripColor(String input) {
        if (input == null) return null;
        return input.replaceAll("(?i)" + SECTION_SIGN + "[0-9a-fk-or]", "");
    }
    
    public static String translateAlternateColorCodes(String text) {
        if (text == null) return null;
        return text.replace('&', SECTION_SIGN);
    }
    
    public static String toAnsi(String text) {
        if (text == null) return null;
        
        StringBuilder result = new StringBuilder();
        int length = text.length();
        
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            
            if (c == SECTION_SIGN && i + 1 < length) {
                char next = Character.toLowerCase(text.charAt(i + 1));
                
                for (ChatColor color : values()) {
                    if (color.code == next) {
                        result.append(color.ansiCode);
                        i++;
                        break;
                    }
                }
            } else {
                result.append(c);
            }
        }
        
        return result.toString() + CONSOLE_RESET;
    }
    
    public static String getMinecraftColor(char code) {
        for (ChatColor color : values()) {
            if (color.code == code) {
                return color.toString();
            }
        }
        return "";
    }
    
    public static String getClosestColor(int r, int g, int b) {
        double minDist = Double.MAX_VALUE;
        String closest = "";
        
        int[][] ansiColors = {
            {0, 0, 0},       
            {0, 0, 170},     
            {0, 170, 0},     
            {0, 170, 170},   
            {170, 0, 0},     
            {170, 0, 170},   
            {255, 170, 0},   
            {170, 170, 170}, 
            {85, 85, 85},    
            {85, 85, 255},   
            {85, 255, 85},   
            {85, 255, 255},  
            {255, 85, 85},   
            {255, 85, 255},  
            {255, 255, 85},  
            {255, 255, 255}  
        };
        
        String[] codes = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};
        
        for (int i = 0; i < ansiColors.length; i++) {
            double dist = Math.pow(r - ansiColors[i][0], 2) +
                         Math.pow(g - ansiColors[i][1], 2) +
                         Math.pow(b - ansiColors[i][2], 2);
            
            if (dist < minDist) {
                minDist = dist;
                closest = codes[i];
            }
        }
        
        return String.valueOf(SECTION_SIGN) + closest;
    }
}