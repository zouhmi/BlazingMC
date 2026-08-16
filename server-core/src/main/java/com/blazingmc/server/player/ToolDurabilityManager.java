package com.blazingmc.server.player;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class ToolDurabilityManager {
    private static final Map<Material, Double> TOOL_DURABILITY = new HashMap<>();
    private static final Map<Material, Double> TOOL_DAMAGE = new HashMap<>();
    private static final Map<Material, Double> MINING_SPEED = new HashMap<>();
    
    static {
        TOOL_DURABILITY.put(Material.WOODEN_SWORD, 59.0);
        TOOL_DURABILITY.put(Material.WOODEN_PICKAXE, 59.0);
        TOOL_DURABILITY.put(Material.WOODEN_AXE, 59.0);
        TOOL_DURABILITY.put(Material.WOODEN_SHOVEL, 59.0);
        
        TOOL_DURABILITY.put(Material.STONE_SWORD, 131.0);
        TOOL_DURABILITY.put(Material.STONE_PICKAXE, 131.0);
        TOOL_DURABILITY.put(Material.STONE_AXE, 131.0);
        TOOL_DURABILITY.put(Material.STONE_SHOVEL, 131.0);
        
        TOOL_DURABILITY.put(Material.IRON_SWORD, 250.0);
        TOOL_DURABILITY.put(Material.IRON_PICKAXE, 250.0);
        TOOL_DURABILITY.put(Material.IRON_AXE, 250.0);
        TOOL_DURABILITY.put(Material.IRON_SHOVEL, 250.0);
        
        TOOL_DURABILITY.put(Material.DIAMOND_SWORD, 1561.0);
        TOOL_DURABILITY.put(Material.DIAMOND_PICKAXE, 1561.0);
        TOOL_DURABILITY.put(Material.DIAMOND_AXE, 1561.0);
        TOOL_DURABILITY.put(Material.DIAMOND_SHOVEL, 1561.0);
        
        TOOL_DURABILITY.put(Material.NETHERITE_SWORD, 2031.0);
        TOOL_DURABILITY.put(Material.NETHERITE_PICKAXE, 2031.0);
        TOOL_DURABILITY.put(Material.NETHERITE_AXE, 2031.0);
        TOOL_DURABILITY.put(Material.NETHERITE_SHOVEL, 2031.0);
        
        TOOL_DURABILITY.put(Material.GOLDEN_SWORD, 32.0);
        TOOL_DURABILITY.put(Material.GOLDEN_PICKAXE, 32.0);
        TOOL_DURABILITY.put(Material.GOLDEN_AXE, 32.0);
        TOOL_DURABILITY.put(Material.GOLDEN_SHOVEL, 32.0);
        
        TOOL_DAMAGE.put(Material.WOODEN_SWORD, 4.0);
        TOOL_DAMAGE.put(Material.STONE_SWORD, 5.0);
        TOOL_DAMAGE.put(Material.IRON_SWORD, 6.0);
        TOOL_DAMAGE.put(Material.DIAMOND_SWORD, 7.0);
        TOOL_DAMAGE.put(Material.NETHERITE_SWORD, 8.0);
        TOOL_DAMAGE.put(Material.GOLDEN_SWORD, 4.0);
        
        TOOL_DAMAGE.put(Material.WOODEN_AXE, 7.0);
        TOOL_DAMAGE.put(Material.STONE_AXE, 9.0);
        TOOL_DAMAGE.put(Material.IRON_AXE, 9.0);
        TOOL_DAMAGE.put(Material.DIAMOND_AXE, 9.0);
        TOOL_DAMAGE.put(Material.NETHERITE_AXE, 10.0);
        TOOL_DAMAGE.put(Material.GOLDEN_AXE, 7.0);
        
        TOOL_DAMAGE.put(Material.WOODEN_PICKAXE, 2.0);
        TOOL_DAMAGE.put(Material.STONE_PICKAXE, 3.0);
        TOOL_DAMAGE.put(Material.IRON_PICKAXE, 4.0);
        TOOL_DAMAGE.put(Material.DIAMOND_PICKAXE, 5.0);
        TOOL_DAMAGE.put(Material.NETHERITE_PICKAXE, 6.0);
        TOOL_DAMAGE.put(Material.GOLDEN_PICKAXE, 2.0);
        TOOL_DAMAGE.put(Material.WOODEN_SHOVEL, 2.0);
        TOOL_DAMAGE.put(Material.STONE_SHOVEL, 3.0);
        TOOL_DAMAGE.put(Material.IRON_SHOVEL, 4.0);
        TOOL_DAMAGE.put(Material.DIAMOND_SHOVEL, 5.0);
        TOOL_DAMAGE.put(Material.NETHERITE_SHOVEL, 6.0);
        TOOL_DAMAGE.put(Material.GOLDEN_SHOVEL, 2.0);
        
        MINING_SPEED.put(Material.WOODEN_PICKAXE, 2.0);
        MINING_SPEED.put(Material.STONE_PICKAXE, 4.0);
        MINING_SPEED.put(Material.IRON_PICKAXE, 6.0);
        MINING_SPEED.put(Material.DIAMOND_PICKAXE, 8.0);
        MINING_SPEED.put(Material.NETHERITE_PICKAXE, 9.0);
        MINING_SPEED.put(Material.GOLDEN_PICKAXE, 12.0);
        
        MINING_SPEED.put(Material.WOODEN_AXE, 2.0);
        MINING_SPEED.put(Material.STONE_AXE, 4.0);
        MINING_SPEED.put(Material.IRON_AXE, 6.0);
        MINING_SPEED.put(Material.DIAMOND_AXE, 8.0);
        MINING_SPEED.put(Material.NETHERITE_AXE, 9.0);
        MINING_SPEED.put(Material.GOLDEN_AXE, 12.0);
        
        MINING_SPEED.put(Material.WOODEN_SHOVEL, 2.0);
        MINING_SPEED.put(Material.STONE_SHOVEL, 4.0);
        MINING_SPEED.put(Material.IRON_SHOVEL, 6.0);
        MINING_SPEED.put(Material.DIAMOND_SHOVEL, 8.0);
        MINING_SPEED.put(Material.NETHERITE_SHOVEL, 9.0);
        MINING_SPEED.put(Material.GOLDEN_SHOVEL, 12.0);
    }
    
    public static boolean isTool(Material material) {
        return material != null && TOOL_DURABILITY.containsKey(material);
    }
    
    public static boolean isSword(Material material) {
        return material != null && material.name().endsWith("_SWORD") && TOOL_DURABILITY.containsKey(material);
    }
    
    public static boolean isPickaxe(Material material) {
        return material != null && material.name().endsWith("_PICKAXE") && TOOL_DURABILITY.containsKey(material);
    }
    
    public static boolean isAxe(Material material) {
        return material != null && material.name().endsWith("_AXE") && TOOL_DURABILITY.containsKey(material);
    }
    
    public static boolean isShovel(Material material) {
        return material != null && material.name().endsWith("_SHOVEL") && TOOL_DURABILITY.containsKey(material);
    }
    
    public static double getToolDurability(Material material) {
        return TOOL_DURABILITY.getOrDefault(material, 0.0);
    }
    
    public static double getToolDamage(Material material) {
        return TOOL_DAMAGE.getOrDefault(material, 1.0);
    }
    
    public static double getMiningSpeed(Material material) {
        return MINING_SPEED.getOrDefault(material, 1.0);
    }
    
    public static boolean canHarvestBlock(Material tool, Material block) {
        if (tool == null || block == null || block == Material.BEDROCK) {
            return false;
        }
        
        if (block == Material.OBSIDIAN) {
            return tool == Material.DIAMOND_PICKAXE || tool == Material.NETHERITE_PICKAXE;
        }
        
        if (isPickaxe(tool)) {
            return isOreBlock(block) || block == Material.STONE || 
                   block == Material.COBBLESTONE || block == Material.ANDESITE ||
                   block == Material.DIORITE || block == Material.GRANITE;
        }
        
        if (isAxe(tool)) {
            return isWoodBlock(block);
        }
        
        if (isShovel(tool)) {
            return block == Material.DIRT || block == Material.GRASS_BLOCK || 
                   block == Material.SAND || block == Material.GRAVEL ||
                   block == Material.COARSE_DIRT || block == Material.PODZOL;
        }
        
        return false;
    }
    
    private static boolean isOreBlock(Material material) {
        return material == Material.COAL_ORE || material == Material.IRON_ORE ||
               material == Material.GOLD_ORE || material == Material.DIAMOND_ORE ||
               material == Material.REDSTONE_ORE || material == Material.EMERALD_ORE ||
               material == Material.LAPIS_ORE;
    }
    
    private static boolean isWoodBlock(Material material) {
        return material == Material.OAK_PLANKS || material == Material.SPRUCE_PLANKS ||
               material == Material.BIRCH_PLANKS || material == Material.JUNGLE_PLANKS ||
               material == Material.ACACIA_PLANKS || material == Material.DARK_OAK_PLANKS ||
               material == Material.OAK_LOG || material == Material.SPRUCE_LOG ||
               material == Material.BIRCH_LOG || material == Material.JUNGLE_LOG ||
               material == Material.ACACIA_LOG || material == Material.DARK_OAK_LOG;
    }
    
    public static int calculateMiningTime(Material tool, Material block, boolean hasEfficiency) {
        if (tool == null || block == null) {
            return -1;
        }
        
        double speed = getMiningSpeed(tool);
        double hardness = getBlockHardness(block);
        
        if (!canHarvestBlock(tool, block)) {
            return -1;
        }
        
        if (hasEfficiency) {
            speed *= 2.0;
        }
        
        double timeTicks = (hardness * 1.5) / speed;
        return Math.max(1, (int) Math.ceil(timeTicks));
    }
    
    private static double getBlockHardness(Material material) {
        if (material == Material.BEDROCK) return -1.0;
        if (material == Material.OBSIDIAN) return 50.0;
        if (material == Material.DIAMOND_ORE) return 3.0;
        if (material == Material.IRON_ORE) return 3.0;
        if (material == Material.GOLD_ORE) return 3.0;
        if (material == Material.COAL_ORE) return 3.0;
        if (material == Material.REDSTONE_ORE) return 3.0;
        if (material == Material.EMERALD_ORE) return 3.0;
        if (material == Material.LAPIS_ORE) return 3.0;
        if (material == Material.STONE || material == Material.COBBLESTONE) return 1.5;
        if (material == Material.DIRT || material == Material.GRASS_BLOCK) return 0.5;
        if (material == Material.SAND || material == Material.GRAVEL) return 0.6;
        if (material == Material.OAK_PLANKS || material == Material.SPRUCE_PLANKS) return 2.0;
        if (material == Material.OAK_LOG || material == Material.SPRUCE_LOG) return 2.0;
        return 1.0;
    }
}
