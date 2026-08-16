package com.blazingmc.server.inventory;

import com.blazingmc.chat.ConsoleLogger;
import com.blazingmc.protocol.handler.ContainerManagerInterface;
import com.blazingmc.protocol.handler.PlayerInterface;
import com.blazingmc.server.BlazingServer;
import com.blazingmc.server.player.Player;
import org.bukkit.Material;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

public class ContainerManager implements ContainerManagerInterface {
    private final BlazingServer server;
    private final Map<UUID, OpenContainer> openContainers;
    private int nextWindowId = 1;
    
    public ContainerManager(BlazingServer server) {
        this.server = server;
        this.openContainers = new HashMap<>();
    }
    
    public void openChest(PlayerInterface player, int x, int y, int z) {
        if (player == null) return;
        
        Inventory chestInventory = getOrCreateChestInventory(x, y, z);
        int windowId = nextWindowId++;
        
        OpenContainer container = new OpenContainer(windowId, chestInventory, x, y, z);
        openContainers.put(player.getUuid(), container);
        
        sendOpenWindow(player, windowId, "minecraft:chest", chestInventory.getTitle(), chestInventory.getSize());
        sendWindowItems(player, windowId, chestInventory);
        
        ConsoleLogger.debug(player.getUsername() + " opened chest at " + x + "," + y + "," + z);
    }
    
    public void openEnderChest(PlayerInterface player, int x, int y, int z) {
        if (player == null) return;
        
        Inventory enderChest = getOrCreateEnderChestInventory(player);
        int windowId = nextWindowId++;
        
        OpenContainer container = new OpenContainer(windowId, enderChest, x, y, z);
        openContainers.put(player.getUuid(), container);
        
        sendOpenWindow(player, windowId, "minecraft:ender_chest", "Ender Chest", enderChest.getSize());
        sendWindowItems(player, windowId, enderChest);
        
        ConsoleLogger.debug(player.getUsername() + " opened ender chest");
    }
    
    public void openFurnace(PlayerInterface player, int x, int y, int z) {
        if (player == null) return;
        
        Inventory furnaceInventory = getOrCreateFurnaceInventory(x, y, z);
        int windowId = nextWindowId++;
        
        OpenContainer container = new OpenContainer(windowId, furnaceInventory, x, y, z);
        openContainers.put(player.getUuid(), container);
        
        sendOpenWindow(player, windowId, "minecraft:furnace", "Furnace", furnaceInventory.getSize());
        sendWindowItems(player, windowId, furnaceInventory);
        
        ConsoleLogger.debug(player.getUsername() + " opened furnace at " + x + "," + y + "," + z);
    }
    
    public void openEnchanting(PlayerInterface player, int x, int y, int z) {
        if (player == null) return;
        Inventory enchantingInventory = new Inventory(2, "Enchanting", Inventory.InventoryType.ENCHANTING);
        int windowId = nextWindowId++;
        OpenContainer container = new OpenContainer(windowId, enchantingInventory, x, y, z);
        openContainers.put(player.getUuid(), container);
        sendOpenWindow(player, windowId, "minecraft:enchanting_table", "Enchanting", enchantingInventory.getSize());
        sendWindowItems(player, windowId, enchantingInventory);
        ConsoleLogger.debug(player.getUsername() + " opened enchanting table");
    }

    public void openCraftingTable(PlayerInterface player, int x, int y, int z) {
        if (player == null) return;
        
        Inventory craftingInventory = new Inventory(36, "Crafting Table", Inventory.InventoryType.CRAFTING);
        int windowId = nextWindowId++;
        
        OpenContainer container = new OpenContainer(windowId, craftingInventory, x, y, z);
        openContainers.put(player.getUuid(), container);
        
        sendOpenWindow(player, windowId, "minecraft:crafting_table", "Crafting Table", 36);
        sendWindowItems(player, windowId, craftingInventory);
        
        ConsoleLogger.debug(player.getUsername() + " opened crafting table at " + x + "," + y + "," + z);
    }
    
    @Override
    public void handleClickContainer(PlayerInterface player, int windowId, int slot, int button, int stateId) {
        if (player == null) return;
        
        OpenContainer container = openContainers.get(player.getUuid());
        if (container == null || container.windowId() != windowId) {
            ConsoleLogger.debug("Invalid container click from " + player.getUsername());
            return;
        }
        
        Inventory inventory = container.inventory();
        if (slot < 0 || slot >= inventory.getSize()) {
            return;
        }
        
        ItemStack clicked = inventory.getItem(slot);
        
        if (button == 0) {
            handleLeftClick(player, container, slot, stateId);
        } else if (button == 1) {
            handleRightClick(player, container, slot, stateId);
        }
        
        sendSetSlot(player, windowId, slot, inventory.getItem(slot));
    }
    
    private void handleLeftClick(PlayerInterface player, OpenContainer container, int slot, int stateId) {
        Inventory inventory = container.inventory();
        ItemStack clicked = inventory.getItem(slot);
        
        if (clicked.isEmpty()) {
            return;
        }
        
        int amount = clicked.getAmount();
        int half = amount / 2;
        
        if (half > 0) {
            clicked.setAmount(half);
        } else {
            inventory.setItem(slot, new ItemStack(Material.AIR));
        }
    }
    
    private void handleRightClick(PlayerInterface player, OpenContainer container, int slot, int stateId) {
        Inventory inventory = container.inventory();
        ItemStack clicked = inventory.getItem(slot);
        
        if (clicked.isEmpty()) {
            return;
        }
        
        if (clicked.getAmount() > 1) {
            clicked.removeAmount(1);
        }
    }
    
    @Override
    public void handleCloseContainer(PlayerInterface player, int windowId) {
        if (player == null) return;
        
        OpenContainer container = openContainers.remove(player.getUuid());
        if (container != null) {
            ConsoleLogger.debug(player.getUsername() + " closed container window " + windowId);
        }
    }
    
    @Override
    public void handleWindowButtonClick(PlayerInterface player, int windowId, int buttonId) {
        if (player == null) return;
        
        OpenContainer container = openContainers.get(player.getUuid());
        if (container == null || container.windowId() != windowId) {
            return;
        }
        
        if (container.inventory().getType() == Inventory.InventoryType.ENCHANTING &&
            player instanceof Player enchantPlayer && buttonId >= 0 && buttonId < 3) {
            ItemStack item = container.inventory().getItem(0);
            if (server.getEnchantmentManager().getOffers(enchantPlayer).isEmpty()) {
                server.getEnchantmentManager().open(enchantPlayer, item, 15);
            }
            if (server.getEnchantmentManager().enchant(enchantPlayer, item, buttonId)) {
                sendWindowItems(player, windowId, container.inventory());
            }
        }
    }
    
    @Override
    public void handleSetCreativeSlot(PlayerInterface player, int slot, int itemId, int count) {
        if (player == null) return;
        
        if (player instanceof Player p) {
            if (p.getGameMode() != 1) {
                ConsoleLogger.warn("Creative inventory action from non-creative player: " + player.getUsername());
                return;
            }
            
            Inventory inventory = p.getInventory();
            if (slot >= 0 && slot < inventory.getSize()) {
                if (itemId == 0) {
                    inventory.setItem(slot, new ItemStack(Material.AIR));
                } else {
                    Material material = getMaterialById(itemId);
                    if (material != null) {
                        inventory.setItem(slot, new ItemStack(material, count));
                    }
                }
            }
        }
    }
    
    private Inventory getOrCreateChestInventory(int x, int y, int z) {
        String key = "chest_" + x + "_" + y + "_" + z;
        return new Inventory(27, "Chest", Inventory.InventoryType.CHEST);
    }
    
    private Inventory getOrCreateEnderChestInventory(PlayerInterface player) {
        if (player instanceof Player p) {
            return p.getEnderChest();
        }
        return new Inventory(27, "Ender Chest", Inventory.InventoryType.ENDER_CHEST);
    }
    
    private Inventory getOrCreateFurnaceInventory(int x, int y, int z) {
        return server.getFurnaceManager().getInventory(x, y, z);
    }
    
    private void sendOpenWindow(PlayerInterface player, int windowId, String type, String title, int slotCount) {
        ByteBuffer buffer = ByteBuffer.allocate(128).order(ByteOrder.BIG_ENDIAN);
        
        writeVarInt(buffer, windowId);
        writeString(buffer, type);
        writeString(buffer, title);
        writeVarInt(buffer, slotCount);
        
        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        
        player.sendPacket(0x2E, data);
    }
    
    private void sendWindowItems(PlayerInterface player, int windowId, Inventory inventory) {
        ByteBuffer buffer = ByteBuffer.allocate(2048).order(ByteOrder.BIG_ENDIAN);
        
        writeVarInt(buffer, windowId);
        writeVarInt(buffer, inventory.getSize());
        
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null || item.isEmpty()) {
                buffer.put((byte) 0);
            } else {
                buffer.put((byte) 1);
                writeVarInt(buffer, item.getType().ordinal());
                buffer.put((byte) item.getAmount());
                buffer.putShort(item.getDurability());
            }
        }
        
        writeVarInt(buffer, 0);
        
        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        
        player.sendPacket(0x14, data);
    }
    
    private void sendSetSlot(PlayerInterface player, int windowId, int slot, ItemStack item) {
        ByteBuffer buffer = ByteBuffer.allocate(64).order(ByteOrder.BIG_ENDIAN);
        
        writeVarInt(buffer, windowId);
        writeVarInt(buffer, slot);
        
        if (item == null || item.isEmpty()) {
            buffer.put((byte) 0);
        } else {
            buffer.put((byte) 1);
            writeVarInt(buffer, item.getType().ordinal());
            buffer.put((byte) item.getAmount());
            buffer.putShort(item.getDurability());
        }
        
        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        
        player.sendPacket(0x15, data);
    }
    
    public void closeAllContainers() {
        openContainers.clear();
    }
    
    private void writeVarInt(ByteBuffer buffer, int value) {
        while ((value & ~0x7F) != 0) {
            buffer.put((byte) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
        buffer.put((byte) value);
    }
    
    private void writeString(ByteBuffer buffer, String str) {
        byte[] bytes = str.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        writeVarInt(buffer, bytes.length);
        buffer.put(bytes);
    }
    
    private Material getMaterialById(int id) {
        Material[] materials = Material.values();
        if (id >= 0 && id < materials.length) {
            return materials[id];
        }
        return null;
    }
    
    public record OpenContainer(int windowId, Inventory inventory, int x, int y, int z) {}
}
