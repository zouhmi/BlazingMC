package com.blazingmc.world.entity;

public enum EntityType {
    PLAYER("player", true),
    ZOMBIE("zombie", true),
    SKELETON("skeleton", true),
    CREEPER("creeper", true),
    SPIDER("spider", true),
    PIG("pig", true),
    COW("cow", true),
    SHEEP("sheep", true),
    CHICKEN("chicken", true),
    WOLF("wolf", true),
    CAT("cat", true),
    VILLAGER("villager", true),
    
    ARROW("arrow", false),
    SNOWBALL("snowball", false),
    EGG("egg", false),
    FIREBALL("fireball", false),
    ITEM("item", false),
    EXPERIENCE_ORB("experience_orb", false),
    LIGHTNING("lightning", false);
    
    private final String name;
    private final boolean living;
    
    EntityType(String name, boolean living) {
        this.name = name;
        this.living = living;
    }
    
    public String getName() { return name; }
    public boolean isLiving() { return living; }
    
    public static EntityType fromName(String name) {
        for (var type : values()) {
            if (type.name.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
    
    public boolean isHostile() {
        return switch (this) {
            case ZOMBIE, SKELETON, CREEPER, SPIDER -> true;
            default -> false;
        };
    }
    
    public boolean isPassive() {
        return switch (this) {
            case PIG, COW, SHEEP, CHICKEN, VILLAGER, WOLF, CAT -> true;
            default -> false;
        };
    }
}