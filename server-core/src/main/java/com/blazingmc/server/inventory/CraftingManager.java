package com.blazingmc.server.inventory;

import com.blazingmc.chat.ConsoleLogger;
import com.blazingmc.protocol.handler.PlayerInterface;
import org.bukkit.Material;

import java.util.*;

public class CraftingManager {
    private final List<ShapedRecipe> shapedRecipes;
    private final List<ShapelessRecipe> shapelessRecipes;
    
    public CraftingManager() {
        this.shapedRecipes = new ArrayList<>();
        this.shapelessRecipes = new ArrayList<>();
        registerDefaultRecipes();
        ConsoleLogger.info("Registered " + shapedRecipes.size() + " shaped and " + 
                          shapelessRecipes.size() + " shapeless recipes");
    }
    
    private void registerDefaultRecipes() {
        registerShaped(new Material[]{Material.OAK_PLANKS, Material.OAK_PLANKS, Material.OAK_PLANKS,
            Material.STICK, Material.AIR, Material.STICK,
            Material.STICK, Material.AIR, Material.STICK}, Material.WOODEN_PICKAXE, 1);
        
        registerShaped(new Material[]{Material.OAK_PLANKS, Material.OAK_PLANKS,
            Material.OAK_PLANKS, Material.STICK, Material.STICK, Material.AIR}, Material.WOODEN_AXE, 1);
        
        registerShaped(new Material[]{Material.OAK_PLANKS, Material.AIR,
            Material.OAK_PLANKS, Material.STICK, Material.AIR, Material.STICK,
            Material.STICK, Material.AIR, Material.AIR}, Material.WOODEN_SWORD, 1);
        
        registerShaped(new Material[]{Material.OAK_PLANKS, Material.OAK_PLANKS, Material.OAK_PLANKS,
            Material.OAK_PLANKS, Material.OAK_PLANKS, Material.OAK_PLANKS,
            Material.OAK_PLANKS, Material.OAK_PLANKS, Material.OAK_PLANKS}, Material.CHEST, 1);
        
        registerShaped(new Material[]{Material.OAK_PLANKS, Material.OAK_PLANKS, Material.OAK_PLANKS,
            Material.OAK_PLANKS, Material.AIR, Material.OAK_PLANKS}, Material.CRAFTING_TABLE, 1);
        
        registerShaped(new Material[]{Material.OAK_PLANKS, Material.OAK_PLANKS,
            Material.STICK, Material.AIR, Material.STICK, Material.AIR}, Material.WOODEN_SHOVEL, 1);
        
        registerShaped(new Material[]{Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE,
            Material.COBBLESTONE, Material.AIR, Material.COBBLESTONE,
            Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE}, Material.FURNACE, 1);
        
        registerShaped(new Material[]{Material.IRON_ORE, Material.IRON_ORE, Material.IRON_ORE,
            Material.IRON_ORE, Material.AIR, Material.IRON_ORE,
            Material.IRON_ORE, Material.IRON_ORE, Material.IRON_ORE}, Material.IRON_SWORD, 1);
        
        registerShaped(new Material[]{Material.DIAMOND_ORE, Material.DIAMOND_ORE, Material.DIAMOND_ORE,
            Material.DIAMOND_ORE, Material.AIR, Material.DIAMOND_ORE,
            Material.DIAMOND_ORE, Material.DIAMOND_ORE, Material.DIAMOND_ORE}, Material.DIAMOND_SWORD, 1);
        
        registerShaped(new Material[]{Material.COAL, Material.STICK}, Material.TORCH, 4);
        registerShaped(new Material[]{Material.CHARCOAL, Material.STICK}, Material.TORCH, 4);
        
        registerShaped(new Material[]{Material.OAK_PLANKS, Material.OAK_PLANKS,
            Material.OAK_PLANKS, Material.OAK_PLANKS}, Material.OAK_SIGN, 3);
        
        registerShaped(new Material[]{Material.OAK_PLANKS, Material.OAK_PLANKS, Material.OAK_PLANKS,
            Material.OAK_PLANKS, Material.OAK_PLANKS, Material.OAK_PLANKS}, Material.OAK_DOOR, 3);
        
        registerShaped(new Material[]{Material.OAK_PLANKS, Material.AIR, Material.OAK_PLANKS,
            Material.OAK_PLANKS, Material.OAK_PLANKS, Material.OAK_PLANKS}, Material.OAK_FENCE, 4);
        
        registerShapeless(new Material[]{Material.OAK_PLANKS, Material.OAK_PLANKS}, Material.WOODEN_BUTTON, 1);
        
        registerShapeless(new Material[]{Material.COBBLESTONE, Material.COBBLESTONE}, Material.STONE_BUTTON, 1);
    }
    
    public void registerShaped(Material[] ingredients, Material result, int amount) {
        shapedRecipes.add(new ShapedRecipe(ingredients, result, amount));
    }
    
    public void registerShapeless(Material[] ingredients, Material result, int amount) {
        shapelessRecipes.add(new ShapelessRecipe(ingredients, result, amount));
    }
    
    public ItemStack checkCrafting(ItemStack[] matrix) {
        if (matrix == null || matrix.length < 9) return null;
        
        for (ShapedRecipe recipe : shapedRecipes) {
            if (matchesShaped(recipe, matrix)) {
                return new ItemStack(recipe.result(), recipe.amount());
            }
        }
        
        for (ShapelessRecipe recipe : shapelessRecipes) {
            if (matchesShapeless(recipe, matrix)) {
                return new ItemStack(recipe.result(), recipe.amount());
            }
        }
        
        return null;
    }
    
    private boolean matchesShaped(ShapedRecipe recipe, ItemStack[] matrix) {
        Material[] ingredients = recipe.ingredients();
        
        for (int i = 0; i < 9 && i < ingredients.length; i++) {
            Material required = ingredients[i];
            Material provided = matrix[i] != null ? matrix[i].getType() : Material.AIR;
            
            if (required != provided) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean matchesShapeless(ShapelessRecipe recipe, ItemStack[] matrix) {
        Material[] required = recipe.ingredients();
        
        Map<Material, Integer> requiredCount = new HashMap<>();
        for (Material m : required) {
            requiredCount.merge(m, 1, Integer::sum);
        }
        
        Map<Material, Integer> providedCount = new HashMap<>();
        for (ItemStack item : matrix) {
            if (item != null && item.getType() != Material.AIR) {
                providedCount.merge(item.getType(), 1, Integer::sum);
            }
        }
        
        for (Map.Entry<Material, Integer> entry : requiredCount.entrySet()) {
            int have = providedCount.getOrDefault(entry.getKey(), 0);
            if (have < entry.getValue()) {
                return false;
            }
        }
        
        return true;
    }
    
    public List<ShapedRecipe> getShapedRecipes() {
        return Collections.unmodifiableList(shapedRecipes);
    }
    
    public List<ShapelessRecipe> getShapelessRecipes() {
        return Collections.unmodifiableList(shapelessRecipes);
    }
    
    public record ShapedRecipe(Material[] ingredients, Material result, int amount) {}
    public record ShapelessRecipe(Material[] ingredients, Material result, int amount) {}
}
