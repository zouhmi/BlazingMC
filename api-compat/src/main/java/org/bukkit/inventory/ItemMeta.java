package org.bukkit.inventory;

import java.util.List;

public interface ItemMeta {
    boolean hasDisplayName();
    String getDisplayName();
    void setDisplayName(String name);
    boolean hasLocalizedName();
    String getLocalizedName();
    void setLocalizedName(String name);
    boolean hasLore();
    List<String> getLore();
    void setLore(List<String> lore);
    boolean hasEnchants();
    boolean hasEnchant(Enchantment enchantment);
    int getEnchantLevel(Enchantment enchantment);
    void addEnchant(Enchantment enchantment, int level, boolean ignoreLevelRestriction);
    void removeEnchant(Enchantment enchantment);
    boolean hasConflictingEnchant(Enchantment enchantment);
    ItemMeta clone();
}