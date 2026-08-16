package org.bukkit.inventory;

public class Enchantment {
    private final int id;
    private final String name;
    private final int maxLevel;
    private final int startLevel;
    private final boolean treasure;
    private final boolean cursed;
    
    public Enchantment(int id, String name, int maxLevel, int startLevel, boolean treasure, boolean cursed) {
        this.id = id;
        this.name = name;
        this.maxLevel = maxLevel;
        this.startLevel = startLevel;
        this.treasure = treasure;
        this.cursed = cursed;
    }
    
    public int getId() { return id; }
    public String getName() { return name; }
    public int getMaxLevel() { return maxLevel; }
    public int getStartLevel() { return startLevel; }
    public boolean isTreasure() { return treasure; }
    public boolean isCursed() { return cursed; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Enchantment other = (Enchantment) obj;
        return id == other.id;
    }
    
    @Override
    public int hashCode() {
        return id;
    }
    
    public static final Enchantment PROTECTION_ENVIRONMENTAL = new Enchantment(0, "PROTECTION_ENVIRONMENTAL", 4, 1, false, false);
    public static final Enchantment PROTECTION_FIRE = new Enchantment(1, "PROTECTION_FIRE", 4, 1, false, false);
    public static final Enchantment PROTECTION_FALL = new Enchantment(2, "PROTECTION_FALL", 4, 1, false, false);
    public static final Enchantment PROTECTION_EXPLOSIONS = new Enchantment(3, "PROTECTION_EXPLOSIONS", 4, 1, false, false);
    public static final Enchantment PROTECTION_PROJECTILE = new Enchantment(4, "PROTECTION_PROJECTILE", 4, 1, false, false);
    public static final Enchantment DAMAGE_ALL = new Enchantment(5, "DAMAGE_ALL", 5, 1, false, false);
    public static final Enchantment DAMAGE_UNDEAD = new Enchantment(6, "DAMAGE_UNDEAD", 5, 1, false, false);
    public static final Enchantment DAMAGE_ARthropods = new Enchantment(7, "DAMAGE_ARthropods", 5, 1, false, false);
    public static final Enchantment KNOCKBACK = new Enchantment(8, "KNOCKBACK", 2, 1, false, false);
    public static final Enchantment FIRE_ASPECT = new Enchantment(9, "FIRE_ASPECT", 2, 1, false, false);
    public static final Enchantment LOOT_BONUS_MOBS = new Enchantment(10, "LOOT_BONUS_MOBS", 3, 1, false, false);
    public static final Enchantment DURABILITY = new Enchantment(11, "DURABILITY", 3, 1, false, false);
    public static final Enchantment DIG_SPEED = new Enchantment(12, "DIG_SPEED", 5, 1, false, false);
    public static final Enchantment SILK_TOUCH = new Enchantment(13, "SILK_TOUCH", 1, 1, false, false);
    public static final Enchantment LOOT_BONUS_BLOCKS = new Enchantment(14, "LOOT_BONUS_BLOCKS", 3, 1, false, false);
    public static final Enchantment ARROW_INFINITE = new Enchantment(15, "ARROW_INFINITE", 1, 1, false, false);
    public static final Enchantment ARROW_DAMAGE = new Enchantment(16, "ARROW_DAMAGE", 5, 1, false, false);
    public static final Enchantment ARROW_FIRE = new Enchantment(17, "ARROW_FIRE", 1, 1, false, false);
    public static final Enchantment ARROW_KNOCKBACK = new Enchantment(18, "ARROW_KNOCKBACK", 2, 1, false, false);
    public static final Enchantment LUCK = new Enchantment(19, "LUCK", 3, 1, false, false);
    public static final Enchantment LURE = new Enchantment(20, "LURE", 3, 1, false, false);
}