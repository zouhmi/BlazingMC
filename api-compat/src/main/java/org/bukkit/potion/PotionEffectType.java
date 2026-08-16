package org.bukkit.potion;

public class PotionEffectType {
    private final int id;
    private final String name;
    private final boolean instant;
    private final boolean beneficial;
    private final boolean harmful;
    
    public PotionEffectType(int id, String name, boolean instant, boolean beneficial, boolean harmful) {
        this.id = id;
        this.name = name;
        this.instant = instant;
        this.beneficial = beneficial;
        this.harmful = harmful;
    }
    
    public int getId() { return id; }
    public String getName() { return name; }
    public boolean isInstant() { return instant; }
    public boolean isBeneficial() { return beneficial; }
    public boolean isHarmful() { return harmful; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PotionEffectType other = (PotionEffectType) obj;
        return id == other.id;
    }
    
    @Override
    public int hashCode() {
        return id;
    }
    
    public static final PotionEffectType SPEED = new PotionEffectType(1, "SPEED", false, true, false);
    public static final PotionEffectType SLOW = new PotionEffectType(2, "SLOW", false, false, true);
    public static final PotionEffectType FAST_DIGGING = new PotionEffectType(3, "FAST_DIGGING", false, true, false);
    public static final PotionEffectType DIG_SLOWDOWN = new PotionEffectType(4, "DIG_SLOWDOWN", false, false, true);
    public static final PotionEffectType DAMAGE_BOOST = new PotionEffectType(5, "DAMAGE_BOOST", false, false, true);
    public static final PotionEffectType HEAL = new PotionEffectType(6, "HEAL", true, true, false);
    public static final PotionEffectType HARM = new PotionEffectType(7, "HARM", true, false, true);
    public static final PotionEffectType JUMP = new PotionEffectType(8, "JUMP", false, true, false);
    public static final PotionEffectType CONFUSION = new PotionEffectType(9, "CONFUSION", false, false, true);
    public static final PotionEffectType REGENERATION = new PotionEffectType(10, "REGENERATION", false, true, false);
    public static final PotionEffectType RESISTANCE = new PotionEffectType(11, "RESISTANCE", false, true, false);
    public static final PotionEffectType FIRE_RESISTANCE = new PotionEffectType(12, "FIRE_RESISTANCE", false, true, false);
    public static final PotionEffectType WATER_BREATHING = new PotionEffectType(13, "WATER_BREATHING", false, true, false);
    public static final PotionEffectType INVISIBILITY = new PotionEffectType(14, "INVISIBILITY", false, true, false);
    public static final PotionEffectType BLINDNESS = new PotionEffectType(15, "BLINDNESS", false, false, true);
    public static final PotionEffectType NIGHT_VISION = new PotionEffectType(16, "NIGHT_VISION", false, true, false);
    public static final PotionEffectType HUNGER = new PotionEffectType(17, "HUNGER", false, false, true);
    public static final PotionEffectType WEAKNESS = new PotionEffectType(18, "WEAKNESS", false, false, true);
    public static final PotionEffectType POISON = new PotionEffectType(19, "POISON", false, false, true);
    public static final PotionEffectType WITHER = new PotionEffectType(20, "WITHER", false, false, true);
    public static final PotionEffectType HEALTH_BOOST = new PotionEffectType(21, "HEALTH_BOOST", false, true, false);
    public static final PotionEffectType ABSORPTION = new PotionEffectType(22, "ABSORPTION", false, true, false);
    public static final PotionEffectType SATURATION = new PotionEffectType(23, "SATURATION", true, true, false);
}