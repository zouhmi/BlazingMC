package org.bukkit.inventory;

public enum InventoryType {
    CHEST(27, "Chest"),
    ENDER_CHEST(27, "Ender Chest"),
    PLAYER(36, "Player"),
    CRAFTING(4, "Crafting"),
    FURNACE(3, "Furnace"),
    BREWING(4, "Brewing Stand"),
    ANVIL(3, "Anvil"),
    BEACON(1, "Beacon"),
    HOPPER(5, "Hopper"),
    DISPENSER(9, "Dispenser"),
    DROPPER(9, "Dropper"),
    ENCHANTING(2, "Enchanting"),
    GRINDSTONE(2, "Grindstone"),
    LECTERN(1, "Lectern"),
    LOOM(4, "Loom"),
    MERCHANT(3, "Merchant"),
    SHULKER_BOX(27, "Shulker Box"),
    SMITHING(3, "Smithing Table"),
    SMOKER(1, "Smoker"),
    BLAST_FURNACE(1, "Blast Furnace"),
    CAMPFIRE(4, "Campfire"),
    CARTOGRAPHY(3, "Cartography Table"),
    CHISELED_BOOKSHELF(6, "Chiseled Bookshelf"),
    CRATING(9, "Crating"),
    JUKEBOX(1, "Jukebox"),
    MINECART_CHEST(27, "Minecart with Chest"),
    MINECART_HOPPER(5, "Minecart with Hopper");
    
    private final int defaultSize;
    private final String title;
    
    InventoryType(int defaultSize, String title) {
        this.defaultSize = defaultSize;
        this.title = title;
    }
    
    public int getDefaultSize() { return defaultSize; }
    public String getDefaultTitle() { return title; }
}