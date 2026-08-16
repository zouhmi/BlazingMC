package com.blazingmc.chat;

import java.util.ArrayList;
import java.util.List;

public class TextComponent {
    private String text;
    private ChatColor color;
    private boolean bold;
    private boolean italic;
    private boolean underlined;
    private boolean strikethrough;
    private boolean obfuscated;
    private List<TextComponent> extra;
    
    public TextComponent(String text) {
        this.text = text;
        this.color = null;
        this.bold = false;
        this.italic = false;
        this.underlined = false;
        this.strikethrough = false;
        this.obfuscated = false;
        this.extra = new ArrayList<>();
    }
    
    public TextComponent(String text, ChatColor color) {
        this(text);
        this.color = color;
    }
    
    public TextComponent setText(String text) {
        this.text = text;
        return this;
    }
    
    public TextComponent setColor(ChatColor color) {
        this.color = color;
        return this;
    }
    
    public TextComponent setBold(boolean bold) {
        this.bold = bold;
        return this;
    }
    
    public TextComponent setItalic(boolean italic) {
        this.italic = italic;
        return this;
    }
    
    public TextComponent setUnderlined(boolean underlined) {
        this.underlined = underlined;
        return this;
    }
    
    public TextComponent setStrikethrough(boolean strikethrough) {
        this.strikethrough = strikethrough;
        return this;
    }
    
    public TextComponent setObfuscated(boolean obfuscated) {
        this.obfuscated = obfuscated;
        return this;
    }
    
    public TextComponent addExtra(TextComponent component) {
        extra.add(component);
        return this;
    }
    
    public TextComponent addExtra(String text) {
        extra.add(new TextComponent(text));
        return this;
    }
    
    public String getText() { return text; }
    public ChatColor getColor() { return color; }
    public boolean isBold() { return bold; }
    public boolean isItalic() { return italic; }
    public boolean isUnderlined() { return underlined; }
    public boolean isStrikethrough() { return strikethrough; }
    public boolean isObfuscated() { return obfuscated; }
    public List<TextComponent> getExtra() { return extra; }
    
    public String toLegacyText() {
        StringBuilder sb = new StringBuilder();
        
        if (color != null) {
            sb.append(color.toString());
        }
        
        if (bold) {
            sb.append(ChatColor.BOLD.toString());
        }
        
        if (italic) {
            sb.append(ChatColor.ITALIC.toString());
        }
        
        if (underlined) {
            sb.append(ChatColor.UNDERLINE.toString());
        }
        
        if (strikethrough) {
            sb.append(ChatColor.STRIKETHROUGH.toString());
        }
        
        if (obfuscated) {
            sb.append(ChatColor.MAGIC.toString());
        }
        
        sb.append(text);
        
        for (TextComponent extra : extra) {
            sb.append(extra.toLegacyText());
        }
        
        return sb.toString();
    }
    
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"text\":\"");
        sb.append(escapeJson(text));
        sb.append("\"");
        
        if (color != null) {
            sb.append(",\"color\":\"");
            sb.append(color.name().toLowerCase());
            sb.append("\"");
        }
        
        if (bold) {
            sb.append(",\"bold\":true");
        }
        
        if (italic) {
            sb.append(",\"italic\":true");
        }
        
        if (underlined) {
            sb.append(",\"underlined\":true");
        }
        
        if (strikethrough) {
            sb.append(",\"strikethrough\":true");
        }
        
        if (obfuscated) {
            sb.append(",\"obfuscated\":true");
        }
        
        if (!extra.isEmpty()) {
            sb.append(",\"extra\":[");
            for (int i = 0; i < extra.size(); i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(extra.get(i).toJson());
            }
            sb.append("]");
        }
        
        sb.append("}");
        return sb.toString();
    }
    
    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    
    public static TextComponent fromLegacy(String legacyText) {
        TextComponent component = new TextComponent("");
        TextComponent current = component;
        
        int i = 0;
        while (i < legacyText.length()) {
            if (legacyText.charAt(i) == '\u00A7' && i + 1 < legacyText.length()) {
                char code = Character.toLowerCase(legacyText.charAt(i + 1));
                
                if (code == 'r') {
                    current = new TextComponent("");
                    component.addExtra(current);
                } else {
                    for (ChatColor color : ChatColor.values()) {
                        if (color.getCode() == code) {
                            if (color.name().length() == 1) {
                                current.setColor(color);
                            } else {
                                switch (color) {
                                    case BOLD:
                                        current.setBold(true);
                                        break;
                                    case ITALIC:
                                        current.setItalic(true);
                                        break;
                                    case UNDERLINE:
                                        current.setUnderlined(true);
                                        break;
                                    case STRIKETHROUGH:
                                        current.setStrikethrough(true);
                                        break;
                                    case MAGIC:
                                        current.setObfuscated(true);
                                        break;
                                    default:
                                        break;
                                }
                            }
                            break;
                        }
                    }
                }
                i += 2;
            } else {
                StringBuilder text = new StringBuilder();
                while (i < legacyText.length() && legacyText.charAt(i) != '\u00A7') {
                    text.append(legacyText.charAt(i));
                    i++;
                }
                current.setText(current.getText() + text.toString());
            }
        }
        
        return component;
    }
}