package com.blazingmc.server.player;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class ArmorManager {
    private static final Map<Material, Double> ARMOR_DURABILITY = new HashMap<>();
    private static final Map<Material, Double> ARMOR_PROTECTION = new HashMap<>();
    private static final Map<Material, Double> ARMOR_TOUGHNESS = new HashMap<>();
    
    static {
        ARMOR_DURABILITY.put(Material.LEATHER_HELMET, 55.0);
        ARMOR_DURABILITY.put(Material.LEATHER_CHESTPLATE, 80.0);
        ARMOR_DURABILITY.put(Material.LEATHER_LEGGINGS, 75.0);
        ARMOR_DURABILITY.put(Material.LEATHER_BOOTS, 65.0);
        
        ARMOR_DURABILITY.put(Material.CHAINMAIL_HELMET, 165.0);
        ARMOR_DURABILITY.put(Material.CHAINMAIL_CHESTPLATE, 240.0);
        ARMOR_DURABILITY.put(Material.CHAINMAIL_LEGGINGS, 225.0);
        ARMOR_DURABILITY.put(Material.CHAINMAIL_BOOTS, 195.0);
        
        ARMOR_DURABILITY.put(Material.IRON_HELMET, 165.0);
        ARMOR_DURABILITY.put(Material.IRON_CHESTPLATE, 240.0);
        ARMOR_DURABILITY.put(Material.IRON_LEGGINGS, 225.0);
        ARMOR_DURABILITY.put(Material.IRON_BOOTS, 195.0);
        
        ARMOR_DURABILITY.put(Material.DIAMOND_HELMET, 363.0);
        ARMOR_DURABILITY.put(Material.DIAMOND_CHESTPLATE, 528.0);
        ARMOR_DURABILITY.put(Material.DIAMOND_LEGGINGS, 495.0);
        ARMOR_DURABILITY.put(Material.DIAMOND_BOOTS, 429.0);
        
        ARMOR_DURABILITY.put(Material.NETHERITE_HELMET, 407.0);
        ARMOR_DURABILITY.put(Material.NETHERITE_CHESTPLATE, 592.0);
        ARMOR_DURABILITY.put(Material.NETHERITE_LEGGINGS, 555.0);
        ARMOR_DURABILITY.put(Material.NETHERITE_BOOTS, 481.0);
        
        ARMOR_DURABILITY.put(Material.GOLDEN_HELMET, 77.0);
        ARMOR_DURABILITY.put(Material.GOLDEN_CHESTPLATE, 112.0);
        ARMOR_DURABILITY.put(Material.GOLDEN_LEGGINGS, 105.0);
        ARMOR_DURABILITY.put(Material.GOLDEN_BOOTS, 91.0);
        
        ARMOR_PROTECTION.put(Material.LEATHER_HELMET, 1.0);
        ARMOR_PROTECTION.put(Material.LEATHER_CHESTPLATE, 3.0);
        ARMOR_PROTECTION.put(Material.LEATHER_LEGGINGS, 2.0);
        ARMOR_PROTECTION.put(Material.LEATHER_BOOTS, 1.0);
        
        ARMOR_PROTECTION.put(Material.CHAINMAIL_HELMET, 2.0);
        ARMOR_PROTECTION.put(Material.CHAINMAIL_CHESTPLATE, 5.0);
        ARMOR_PROTECTION.put(Material.CHAINMAIL_LEGGINGS, 4.0);
        ARMOR_PROTECTION.put(Material.CHAINMAIL_BOOTS, 1.0);
        
        ARMOR_PROTECTION.put(Material.IRON_HELMET, 2.0);
        ARMOR_PROTECTION.put(Material.IRON_CHESTPLATE, 6.0);
        ARMOR_PROTECTION.put(Material.IRON_LEGGINGS, 5.0);
        ARMOR_PROTECTION.put(Material.IRON_BOOTS, 2.0);
        
        ARMOR_PROTECTION.put(Material.DIAMOND_HELMET, 3.0);
        ARMOR_PROTECTION.put(Material.DIAMOND_CHESTPLATE, 8.0);
        ARMOR_PROTECTION.put(Material.DIAMOND_LEGGINGS, 6.0);
        ARMOR_PROTECTION.put(Material.DIAMOND_BOOTS, 3.0);
        
        ARMOR_PROTECTION.put(Material.NETHERITE_HELMET, 3.0);
        ARMOR_PROTECTION.put(Material.NETHERITE_CHESTPLATE, 8.0);
        ARMOR_PROTECTION.put(Material.NETHERITE_LEGGINGS, 6.0);
        ARMOR_PROTECTION.put(Material.NETHERITE_BOOTS, 3.0);
        
        ARMOR_PROTECTION.put(Material.GOLDEN_HELMET, 1.0);
        ARMOR_PROTECTION.put(Material.GOLDEN_CHESTPLATE, 2.0);
        ARMOR_PROTECTION.put(Material.GOLDEN_LEGGINGS, 1.0);
        ARMOR_PROTECTION.put(Material.GOLDEN_BOOTS, 1.0);
        
        ARMOR_TOUGHNESS.put(Material.DIAMOND_HELMET, 2.0);
        ARMOR_TOUGHNESS.put(Material.DIAMOND_CHESTPLATE, 2.0);
        ARMOR_TOUGHNESS.put(Material.DIAMOND_LEGGINGS, 2.0);
        ARMOR_TOUGHNESS.put(Material.DIAMOND_BOOTS, 2.0);
        
        ARMOR_TOUGHNESS.put(Material.NETHERITE_HELMET, 3.0);
        ARMOR_TOUGHNESS.put(Material.NETHERITE_CHESTPLATE, 3.0);
        ARMOR_TOUGHNESS.put(Material.NETHERITE_LEGGINGS, 3.0);
        ARMOR_TOUGHNESS.put(Material.NETHERITE_BOOTS, 3.0);
    }
    
    public static boolean isHelmet(Material material) {
        return material != null && material.name().endsWith("_HELMET");
    }
    
    public static boolean isChestplate(Material material) {
        return material != null && material.name().endsWith("_CHESTPLATE");
    }
    
    public static boolean isLeggings(Material material) {
        return material != null && material.name().endsWith("_LEGGINGS");
    }
    
    public static boolean isBoots(Material material) {
        return material != null && material.name().endsWith("_BOOTS");
    }
    
    public static boolean isArmor(Material material) {
        return material != null && (isHelmet(material) || isChestplate(material) || isLeggings(material) || isBoots(material));
    }
    
    public static double getArmorDurability(Material material) {
        return ARMOR_DURABILITY.getOrDefault(material, 0.0);
    }
    
    public static double getArmorProtection(Material material) {
        return ARMOR_PROTECTION.getOrDefault(material, 0.0);
    }
    
    public static double getArmorToughness(Material material) {
        return ARMOR_TOUGHNESS.getOrDefault(material, 0.0);
    }
    
    public static double calculateDamageReduction(double rawDamage, double armorPoints, double toughness) {
        if (rawDamage <= 0) {
            return 0;
        }
        
        double armor = Math.max(0, armorPoints);
        double armorToughness = Math.max(0, toughness);
        double reduction = Math.min(20, Math.max(armor / 5.0,
            armor - rawDamage / (2.0 + armorToughness))) / 25.0;
        return rawDamage * (1.0 - reduction);
    }
    
    public static double getTotalProtection(Material helmet, Material chestplate, Material leggings, Material boots) {
        return getArmorProtection(helmet) + getArmorProtection(chestplate) + 
               getArmorProtection(leggings) + getArmorProtection(boots);
    }
    
    public static double getTotalToughness(Material helmet, Material chestplate, Material leggings, Material boots) {
        return getArmorToughness(helmet) + getArmorToughness(chestplate) + 
               getArmorToughness(leggings) + getArmorToughness(boots);
    }
    
    public static double applyArmorProtection(double rawDamage, Material helmet, Material chestplate, 
                                             Material leggings, Material boots) {
        double armorPoints = getTotalProtection(helmet, chestplate, leggings, boots);
        double toughness = getTotalToughness(helmet, chestplate, leggings, boots);
        return calculateDamageReduction(rawDamage, armorPoints, toughness);
    }
}
