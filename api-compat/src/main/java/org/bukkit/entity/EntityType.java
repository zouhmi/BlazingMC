package org.bukkit.entity;

import org.bukkit.Player;

public enum EntityType {
    PLAYER("player", 0, Human.class),
    CREEPER("creeper", 50, Monster.class),
    SKELETON("skeleton", 51, Monster.class),
    SPIDER("spider", 52, Monster.class),
    GIANT("giant", 53, Monster.class),
    ZOMBIE("zombie", 54, Monster.class),
    SLIME("slime", 55, Monster.class),
    GHAST("ghast", 56, Monster.class),
    ZOMBIFIED_PIGLIN("zombified_piglin", 57, Monster.class),
    PIG("pig", 90, Animal.class),
    COW("cow", 91, Animal.class),
    SHEEP("sheep", 92, Animal.class),
    CHICKEN("chicken", 93, Animal.class),
    SQUID("squid", 94, WaterAnimal.class),
    WOLF("wolf", 95, Tameable.class),
    MOOSHROOM("mooshroom", 96, Animal.class),
    OCELOT("ocelot", 98, Tameable.class),
    HORSE("horse", 100, AbstractHorse.class),
    RABBIT("rabbit", 101, Animal.class),
    VILLAGER("villager", 120, Ageable.class),
    IRON_GOLEM("iron_golem", 99, Creature.class),
    SNOWMAN("snowman", 97, Creature.class),
    ENDERMAN("enderman", 58, Monster.class),
    CAVE_SPIDER("cave_spider", 59, Monster.class),
    SILVERFISH("silverfish", 60, Monster.class),
    BLAZE("blaze", 61, Monster.class),
    MAGMA_CUBE("magma_cube", 62, Monster.class),
    ENDER_DRAGON("ender_dragon", 63, ComplexCreature.class),
    WITHER("wither", 64, Monster.class),
    BAT("bat", 65, Ambient.class),
    WITCH("witch", 66, Monster.class),
    ENDERMITE("endermite", 67, Monster.class),
    GUARDIAN("guardian", 68, Monster.class),
    SHULKER("shulker", 69, Monster.class),
    PIGLIN("piglin", 102, Monster.class),
    HOGLIN("hoglin", 103, Animal.class),
    ZOGLIN("zoglin", 104, Monster.class),
    STRIDER("strider", 105, Animal.class),
    FROG("frog", 106, Animal.class),
    TADPOLE("tadpole", 107, WaterAnimal.class),
    WARDEN("warden", 108, Monster.class),
    ALLAY("allay", 109, Creature.class),
    GOAT("goat", 110, Animal.class),
    SNIFFER("sniffer", 111, Animal.class),
    CAMEL("camel", 112, AbstractHorse.class),
    BREEZE("breeze", 113, Monster.class),
    ARMADILLO("armadillo", 114, Animal.class),
    
    ARROW("arrow", 60, AbstractProjectile.class),
    SNOWBALL("snowball", 61, AbstractProjectile.class),
    EGG("egg", 62, AbstractProjectile.class),
    FIREBALL("fireball", 63, AbstractProjectile.class),
    SMALL_FIREBALL("small_fireball", 64, AbstractProjectile.class),
    ENDER_PEARL("ender_pearl", 65, AbstractProjectile.class),
    WITHER_SKULL("wither_skull", 66, AbstractProjectile.class),
    SHULKER_BULLET("shulker_bullet", 67, AbstractProjectile.class),
    DRAGON_FIREBALL("dragon_fireball", 68, AbstractProjectile.class),
    FISHING_HOOK("fishing_bobber", 90, AbstractProjectile.class),
    LIGHTNING("lightning", 91, Weather.class),
    TRIDENT("trident", 115, AbstractProjectile.class),
    WIND_CHARGE("wind_charge", 116, AbstractProjectile.class),
    
    PAINTING("painting", 90, Hanging.class),
    ITEM_FRAME("item_frame", 91, Hanging.class),
    GLOW_ITEM_FRAME("glow_item_frame", 92, Hanging.class),
    LEASH_KNOT("leash_knot", 93, Hanging.class),
    ARMOR_STAND("armor_stand", 94, LivingEntity.class),
    INTERACTION("interaction", 95, Entity.class),
    DISPLAY("display", 96, Entity.class),
    BLOCK_DISPLAY("block_display", 97, Entity.class),
    ITEM_DISPLAY("item_display", 98, Entity.class),
    TEXT_DISPLAY("text_display", 99, Entity.class),
    
    MINECART("minecart", 10, Vehicle.class),
    MINECART_CHEST("chest_minecart", 11, Vehicle.class),
    MINECART_FURNACE("furnace_minecart", 12, Vehicle.class),
    MINECART_TNT("tnt_minecart", 13, Vehicle.class),
    MINECART_MOB_SPAWNER("mob_spawner_minecart", 14, Vehicle.class),
    MINECART_HOPPER("hopper_minecart", 15, Vehicle.class),
    MINECART_COMMAND("command_minecart", 16, Vehicle.class),
    BOAT("boat", 1, Vehicle.class),
    CHEST_BOAT("chest_boat", 2, Vehicle.class),
    
    FIREWORK("firework", 100, Projectile.class),
    FIREWORK_ROCKET("firework_rocket", 101, Projectile.class),
    POTION("potion", 117, AbstractProjectile.class),
    EXPERIENCE_BOTTLE("experience_bottle", 118, AbstractProjectile.class),
    
    ITEM("item", 2, Item.class),
    EXPERIENCE_ORB("experience_orb", 3, ExperienceOrb.class),
    AREA_EFFECT_CLOUD("area_effect_cloud", 30, Entity.class),
    EVOKER_FANGS("evoker_fangs", 31, Entity.class),
    END_CRYSTAL("end_crystal", 200, Entity.class),
    COMMAND_BLOCK_MINECART("command_block_minecart", 400, Vehicle.class),
    MARKER("marker", 401, Entity.class),
    SHULKER_BOX("shulker_box", 402, Entity.class),
    GLOW_SQUID("glow_squid", 403, WaterAnimal.class);
    
    private final String internalName;
    private final int typeId;
    private final Class<?> entityClass;
    
    EntityType(String internalName, int typeId, Class<?> entityClass) {
        this.internalName = internalName;
        this.typeId = typeId;
        this.entityClass = entityClass;
    }
    
    public String getInternalName() { return internalName; }
    public int getTypeId() { return typeId; }
    public Class<?> getEntityClass() { return entityClass; }
    
    public boolean isAlive() {
        return LivingEntity.class.isAssignableFrom(entityClass);
    }
    
    public boolean isSpawnable() {
        return entityClass != null && !entityClass.equals(Player.class);
    }
}