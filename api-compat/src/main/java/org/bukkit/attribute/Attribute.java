package org.bukkit.attribute;

public enum Attribute {
    GENERIC_MAX_HEALTH("generic.max_health", 20.0, 1024.0, 0.0),
    GENERIC_FOLLOW_RANGE("generic.follow_range", 32.0, 2048.0, 0.0),
    GENERIC_KNOCKBACK_RESISTANCE("generic.knockback_resistance", 0.0, 1.0, 0.0),
    GENERIC_MOVEMENT_SPEED("generic.movement_speed", 0.7, 1024.0, 0.0),
    GENERIC_FLYING_SPEED("generic.flying_speed", 0.4, 1024.0, 0.0),
    GENERIC_ATTACK_DAMAGE("generic.attack_damage", 2.0, 2048.0, 0.0),
    GENERIC_ATTACK_SPEED("generic.attack_speed", 4.0, 1024.0, 0.0),
    GENERIC_ARMOR("generic.armor", 0.0, 30.0, 0.0),
    GENERIC_ARMOR_TOUGHNESS("generic.armor_toughness", 0.0, 20.0, 0.0),
    GENERIC_LUCK("generic.luck", 0.0, 1024.0, -1024.0),
    ZOMBIE_SPAWN_REINFORCEMENTS("zombie.spawn_reinforcements", 0.0, 1.0, 0.0);
    
    private final String internalName;
    private final double defaultValue;
    private final double maximumValue;
    private final double minimumValue;
    
    Attribute(String internalName, double defaultValue, double maximumValue, double minimumValue) {
        this.internalName = internalName;
        this.defaultValue = defaultValue;
        this.maximumValue = maximumValue;
        this.minimumValue = minimumValue;
    }
    
    public String getInternalName() { return internalName; }
    public double getDefaultValue() { return defaultValue; }
    public double getMaximumValue() { return maximumValue; }
    public double getMinimumValue() { return minimumValue; }
}