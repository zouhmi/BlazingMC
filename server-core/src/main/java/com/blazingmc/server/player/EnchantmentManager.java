package com.blazingmc.server.player;

import com.blazingmc.server.inventory.Inventory;
import com.blazingmc.server.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.Enchantment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EnchantmentManager {
    private final Map<UUID, List<Offer>> offers = new ConcurrentHashMap<>();

    public List<Offer> generateOffers(ItemStack item, int bookshelfPower, long seed) {
        if (item == null || item.isEmpty()) {
            return List.of();
        }

        List<Enchantment> available = getAvailableEnchantments(item.getType());
        if (available.isEmpty()) {
            return List.of();
        }

        Random random = new Random(seed ^ item.getType().ordinal() * 31L ^ bookshelfPower * 131L);
        List<Offer> generated = new ArrayList<>();
        int power = Math.max(1, Math.min(15, bookshelfPower));
        for (int index = 0; index < 3; index++) {
            Enchantment enchantment = available.get(random.nextInt(available.size()));
            int level = 1 + random.nextInt(Math.min(enchantment.getMaxLevel(), Math.max(1, power / 5 + 1)));
            int levelCost = Math.max(1, (index + 1) * 3 + level + random.nextInt(Math.max(1, power / 3 + 1)));
            int lapisCost = index + 1;
            generated.add(new Offer(enchantment, level, levelCost, lapisCost));
        }
        return Collections.unmodifiableList(generated);
    }

    public List<Offer> open(Player player, ItemStack item, int bookshelfPower) {
        List<Offer> generated = generateOffers(item, bookshelfPower, player.getUuid().getMostSignificantBits());
        offers.put(player.getUuid(), generated);
        return generated;
    }

    public boolean enchant(Player player, ItemStack item, int offerIndex) {
        List<Offer> playerOffers = offers.get(player.getUuid());
        if (playerOffers == null || offerIndex < 0 || offerIndex >= playerOffers.size() || item == null || item.isEmpty()) {
            return false;
        }

        Offer offer = playerOffers.get(offerIndex);
        ExperienceManager experience = player.getExperienceManager();
        Inventory inventory = player.getInventory();
        if (!experience.hasLevel(offer.levelCost()) || inventory.count(Material.LAPIS_LAZULI) < offer.lapisCost()) {
            return false;
        }

        if (!experience.removeLevels(offer.levelCost())) {
            return false;
        }
        inventory.removeItem(new ItemStack(Material.LAPIS_LAZULI, offer.lapisCost()));
        item.addEnchantment(offer.enchantment(), offer.level());
        offers.remove(player.getUuid());
        return true;
    }

    public List<Offer> getOffers(Player player) {
        return offers.getOrDefault(player.getUuid(), List.of());
    }

    public void close(Player player) {
        offers.remove(player.getUuid());
    }

    private List<Enchantment> getAvailableEnchantments(Material material) {
        String name = material.name();
        List<Enchantment> result = new ArrayList<>();
        if (name.endsWith("_SWORD")) {
            result.add(Enchantment.DAMAGE_ALL);
            result.add(Enchantment.KNOCKBACK);
            result.add(Enchantment.FIRE_ASPECT);
            result.add(Enchantment.LOOT_BONUS_MOBS);
        } else if (name.endsWith("_PICKAXE") || name.endsWith("_AXE") || name.endsWith("_SHOVEL")) {
            result.add(Enchantment.DIG_SPEED);
            result.add(Enchantment.SILK_TOUCH);
            result.add(Enchantment.LOOT_BONUS_BLOCKS);
        } else if (name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") ||
                   name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS")) {
            result.add(Enchantment.PROTECTION_ENVIRONMENTAL);
            result.add(Enchantment.PROTECTION_FIRE);
            result.add(Enchantment.PROTECTION_PROJECTILE);
        } else if (material == Material.BOW) {
            result.add(Enchantment.ARROW_DAMAGE);
            result.add(Enchantment.ARROW_FIRE);
            result.add(Enchantment.ARROW_INFINITE);
            result.add(Enchantment.ARROW_KNOCKBACK);
        }
        result.add(Enchantment.DURABILITY);
        return result;
    }

    public record Offer(Enchantment enchantment, int level, int levelCost, int lapisCost) { }
}
