package com.blazingmc.server.inventory;

import org.bukkit.Material;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FurnaceManager {
    private static final int COOK_TIME_TICKS = 200;
    private final Map<Position, FurnaceState> furnaces = new ConcurrentHashMap<>();

    public Inventory getInventory(int x, int y, int z) {
        return getOrCreate(x, y, z).inventory();
    }

    public FurnaceState getOrCreate(int x, int y, int z) {
        return furnaces.computeIfAbsent(new Position(x, y, z), key ->
            new FurnaceState(new Inventory(3, "Furnace", Inventory.InventoryType.FURNACE)));
    }

    public void tick() {
        for (FurnaceState furnace : furnaces.values()) {
            tick(furnace);
        }
    }

    private void tick(FurnaceState furnace) {
        Inventory inventory = furnace.inventory();
        ItemStack input = inventory.getItem(0);
        ItemStack fuel = inventory.getItem(1);
        ItemStack output = inventory.getItem(2);
        Material result = getSmeltingResult(input == null ? null : input.getType());
        boolean canOutput = result != null && (output == null || output.isEmpty() ||
            output.isSimilar(new ItemStack(result)) && output.getAmount() < 64);

        if (furnace.burnTime() > 0) {
            furnace.setBurnTime(furnace.burnTime() - 1);
        }

        if (furnace.burnTime() == 0 && canOutput && fuel != null && !fuel.isEmpty()) {
            int fuelTime = getFuelTime(fuel.getType());
            if (fuelTime > 0) {
                furnace.setBurnTime(fuelTime);
                furnace.setBurnTimeTotal(fuelTime);
                Material fuelType = fuel.getType();
                fuel.removeAmount(1);
                if (fuelType == Material.LAVA_BUCKET) {
                    inventory.setItem(1, new ItemStack(Material.BUCKET));
                } else if (fuel.isEmpty()) {
                    inventory.setItem(1, new ItemStack(Material.AIR));
                }
            }
        }

        if (furnace.burnTime() > 0 && canOutput) {
            furnace.setCookTime(furnace.cookTime() + 1);
            if (furnace.cookTime() >= COOK_TIME_TICKS) {
                smelt(inventory, result);
                furnace.setCookTime(0);
            }
        } else if (furnace.cookTime() > 0) {
            furnace.setCookTime(Math.max(0, furnace.cookTime() - 2));
        }
    }

    private void smelt(Inventory inventory, Material result) {
        ItemStack input = inventory.getItem(0);
        if (input == null || input.isEmpty()) {
            return;
        }

        input.removeAmount(1);
        if (input.isEmpty()) {
            inventory.setItem(0, new ItemStack(Material.AIR));
        }

        ItemStack output = inventory.getItem(2);
        if (output == null || output.isEmpty()) {
            inventory.setItem(2, new ItemStack(result));
        } else {
            output.addAmount(1);
        }
    }

    public Material getSmeltingResult(Material input) {
        if (input == null) {
            return null;
        }
        return switch (input) {
            case IRON_ORE, RAW_IRON -> Material.IRON_INGOT;
            case GOLD_ORE, RAW_GOLD -> Material.GOLD_INGOT;
            case SAND -> Material.GLASS;
            case COBBLESTONE -> Material.STONE;
            case OAK_LOG, SPRUCE_LOG, BIRCH_LOG, JUNGLE_LOG, ACACIA_LOG, DARK_OAK_LOG -> Material.CHARCOAL;
            default -> null;
        };
    }

    public int getFuelTime(Material fuel) {
        if (fuel == null) {
            return 0;
        }
        return switch (fuel) {
            case COAL, CHARCOAL -> 1600;
            case OAK_PLANKS, SPRUCE_PLANKS, BIRCH_PLANKS, JUNGLE_PLANKS, ACACIA_PLANKS, DARK_OAK_PLANKS -> 300;
            case STICK -> 100;
            case LAVA_BUCKET -> 20000;
            default -> 0;
        };
    }

    public int getCookTimeTicks() {
        return COOK_TIME_TICKS;
    }

    public record Position(int x, int y, int z) { }

    public static final class FurnaceState {
        private final Inventory inventory;
        private int burnTime;
        private int burnTimeTotal;
        private int cookTime;

        private FurnaceState(Inventory inventory) {
            this.inventory = inventory;
        }

        public Inventory inventory() { return inventory; }
        public int burnTime() { return burnTime; }
        public int burnTimeTotal() { return burnTimeTotal; }
        public int cookTime() { return cookTime; }
        private void setBurnTime(int burnTime) { this.burnTime = burnTime; }
        private void setBurnTimeTotal(int burnTimeTotal) { this.burnTimeTotal = burnTimeTotal; }
        private void setCookTime(int cookTime) { this.cookTime = cookTime; }
    }
}
