package com.blazingmc.server.chat;

import com.blazingmc.chat.ChatColor;
import com.blazingmc.chat.ConsoleLogger;
import com.blazingmc.protocol.handler.ChatManagerInterface;
import com.blazingmc.protocol.handler.PlayerInterface;
import com.blazingmc.server.BlazingServer;
import com.blazingmc.server.player.PlayerManager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

public class ChatManager implements ChatManagerInterface {
    private final PlayerManager playerManager;
    private final boolean profanityFilter;
    private final int chatDistance;
    private final boolean chatFormatting;
    
    public ChatManager(PlayerManager playerManager) {
        this.playerManager = playerManager;
        this.profanityFilter = false;
        this.chatDistance = 0;
        this.chatFormatting = true;
    }
    
    public void handleChatMessage(PlayerInterface sender, String message) {
        if (sender == null || message == null || message.isEmpty()) {
            return;
        }
        
        message = message.trim();
        
        if (message.startsWith("/")) {
            handleCommand(sender, message);
            return;
        }
        
        ConsoleLogger.chat(sender.getUsername(), message);
        
        String formattedMessage = formatChatMessage(sender, message);
        broadcastChatMessage(formattedMessage, sender);
    }
    
    public void handleCommand(PlayerInterface sender, String command) {
        if (sender == null || command == null || command.isEmpty()) {
            return;
        }
        
        String[] parts = command.substring(1).split("\\s+");
        String cmd = parts[0].toLowerCase();
        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);
        
        ConsoleLogger.command(sender.getUsername(), command);
        
        switch (cmd) {
            case "help" -> handleHelpCommand(sender);
            case "tp" -> handleTeleportCommand(sender, args);
            case "gamemode" -> handleGamemodeCommand(sender, args);
            case "msg" -> handlePrivateMessageCommand(sender, args);
            case "say" -> handleSayCommand(sender, args);
            case "list" -> handleListCommand(sender);
            case "seed" -> handleSeedCommand(sender);
            case "time" -> handleTimeCommand(sender, args);
            case "weather" -> handleWeatherCommand(sender, args);
            case "difficulty" -> handleDifficultyCommand(sender, args);
            case "give" -> handleGiveCommand(sender, args);
            case "kill" -> handleKillCommand(sender, args);
            case "clear" -> handleClearCommand(sender, args);
            default -> sendSystemMessage(sender, ChatColor.RED + "Unknown command: " + cmd + ". Type /help for a list of commands.");
        }
    }
    
    private void handleHelpCommand(PlayerInterface sender) {
        sendSystemMessage(sender, ChatColor.GOLD + "=== BlazingMC Commands ===");
        sendSystemMessage(sender, ChatColor.YELLOW + "/help" + ChatColor.GRAY + " - Show this help message");
        sendSystemMessage(sender, ChatColor.YELLOW + "/tp <player>" + ChatColor.GRAY + " - Teleport to a player");
        sendSystemMessage(sender, ChatColor.YELLOW + "/gamemode <mode>" + ChatColor.GRAY + " - Change game mode");
        sendSystemMessage(sender, ChatColor.YELLOW + "/msg <player> <message>" + ChatColor.GRAY + " - Send private message");
        sendSystemMessage(sender, ChatColor.YELLOW + "/say <message>" + ChatColor.GRAY + " - Broadcast message");
        sendSystemMessage(sender, ChatColor.YELLOW + "/list" + ChatColor.GRAY + " - List online players");
        sendSystemMessage(sender, ChatColor.YELLOW + "/seed" + ChatColor.GRAY + " - Show world seed");
        sendSystemMessage(sender, ChatColor.YELLOW + "/time <set|add> <value>" + ChatColor.GRAY + " - Change time");
        sendSystemMessage(sender, ChatColor.YELLOW + "/weather <clear|rain|thunder>" + ChatColor.GRAY + " - Change weather");
        sendSystemMessage(sender, ChatColor.YELLOW + "/difficulty <peaceful|easy|normal|hard>" + ChatColor.GRAY + " - Change difficulty");
        sendSystemMessage(sender, ChatColor.YELLOW + "/give <item> [amount]" + ChatColor.GRAY + " - Give item to player");
        sendSystemMessage(sender, ChatColor.YELLOW + "/kill [player]" + ChatColor.GRAY + " - Kill player or self");
        sendSystemMessage(sender, ChatColor.YELLOW + "/clear" + ChatColor.GRAY + " - Clear inventory");
    }
    
    private void handleTeleportCommand(PlayerInterface sender, String[] args) {
        if (args.length < 1) {
            sendSystemMessage(sender, ChatColor.RED + "Usage: /tp <player>");
            return;
        }
        
        PlayerInterface target = playerManager.getPlayer(args[0]);
        if (target == null) {
            sendSystemMessage(sender, ChatColor.RED + "Player not found: " + args[0]);
            return;
        }
        
        sender.teleport(target.getX(), target.getY(), target.getZ(), target.getYaw(), target.getPitch());
        sendSystemMessage(sender, ChatColor.GREEN + "Teleported to " + target.getUsername());
    }
    
    private void handleGamemodeCommand(PlayerInterface sender, String[] args) {
        if (args.length < 1) {
            sendSystemMessage(sender, ChatColor.RED + "Usage: /gamemode <survival|creative|adventure|spectator>");
            return;
        }
        
        int gameMode = switch (args[0].toLowerCase()) {
            case "survival", "s" -> 0;
            case "creative", "c" -> 1;
            case "adventure", "a" -> 2;
            case "spectator", "sp" -> 3;
            default -> {
                sendSystemMessage(sender, ChatColor.RED + "Unknown game mode: " + args[0]);
                yield -1;
            }
        };
        
        if (gameMode >= 0) {
            sender.setGameMode(gameMode);
            sendSystemMessage(sender, ChatColor.GREEN + "Game mode set to " + args[0]);
        }
    }
    
    private void handlePrivateMessageCommand(PlayerInterface sender, String[] args) {
        if (args.length < 2) {
            sendSystemMessage(sender, ChatColor.RED + "Usage: /msg <player> <message>");
            return;
        }
        
        PlayerInterface target = playerManager.getPlayer(args[0]);
        if (target == null) {
            sendSystemMessage(sender, ChatColor.RED + "Player not found: " + args[0]);
            return;
        }
        
        StringBuilder message = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) message.append(" ");
            message.append(args[i]);
        }
        
        sendSystemMessage(sender, ChatColor.GRAY + "To " + target.getUsername() + ": " + message);
        sendSystemMessage(target, ChatColor.GRAY + "From " + sender.getUsername() + ": " + message);
    }
    
    private void handleSayCommand(PlayerInterface sender, String[] args) {
        if (args.length < 1) {
            sendSystemMessage(sender, ChatColor.RED + "Usage: /say <message>");
            return;
        }
        
        StringBuilder message = new StringBuilder();
        for (String arg : args) {
            if (message.length() > 0) message.append(" ");
            message.append(arg);
        }
        
        String formattedMessage = ChatColor.GRAY + "[" + sender.getUsername() + "] " + ChatColor.WHITE + message;
        broadcastChatMessage(formattedMessage, null);
    }
    
    private void handleListCommand(PlayerInterface sender) {
        int count = playerManager.getOnlinePlayerCount();
        StringBuilder playerList = new StringBuilder();
        
        for (PlayerInterface player : playerManager.getPlayers().values()) {
            if (playerList.length() > 0) playerList.append(ChatColor.GRAY + ", ");
            playerList.append(ChatColor.GREEN + player.getUsername());
        }
        
        sendSystemMessage(sender, ChatColor.GOLD + "Online players (" + count + "): " + playerList);
    }
    
    private void handleSeedCommand(PlayerInterface sender) {
        sendSystemMessage(sender, ChatColor.GREEN + "World seed: " + BlazingServer.getInstance().getWorld().getSeed());
    }
    
    private void handleTimeCommand(PlayerInterface sender, String[] args) {
        if (args.length < 2) {
            sendSystemMessage(sender, ChatColor.RED + "Usage: /time <set|add> <value>");
            return;
        }
        
        String action = args[0].toLowerCase();
        try {
            long value = Long.parseLong(args[1]);
            
            switch (action) {
                case "set" -> {
                    BlazingServer.getInstance().getWorld().setTime(value);
                    broadcastChatMessage(ChatColor.GRAY + "[Server] Time set to " + value, null);
                }
                case "add" -> {
                    long newTime = BlazingServer.getInstance().getWorld().getTime() + value;
                    BlazingServer.getInstance().getWorld().setTime(newTime);
                    broadcastChatMessage(ChatColor.GRAY + "[Server] Time advanced by " + value, null);
                }
                default -> sendSystemMessage(sender, ChatColor.RED + "Unknown action: " + action);
            }
        } catch (NumberFormatException e) {
            sendSystemMessage(sender, ChatColor.RED + "Invalid number: " + args[1]);
        }
    }
    
    private void handleWeatherCommand(PlayerInterface sender, String[] args) {
        if (args.length < 1) {
            sendSystemMessage(sender, ChatColor.RED + "Usage: /weather <clear|rain|thunder>");
            return;
        }
        
        String weather = args[0].toLowerCase();
        int duration = args.length > 1 ? Integer.parseInt(args[1]) : 6000;
        
        switch (weather) {
            case "clear" -> {
                BlazingServer.getInstance().getWorld().setWeatherDuration(0);
                broadcastChatMessage(ChatColor.GRAY + "[Server] Weather set to clear", null);
            }
            case "rain" -> {
                BlazingServer.getInstance().getWorld().setWeatherDuration(duration);
                broadcastChatMessage(ChatColor.GRAY + "[Server] Weather set to rain", null);
            }
            case "thunder" -> {
                BlazingServer.getInstance().getWorld().setWeatherDuration(duration);
                broadcastChatMessage(ChatColor.GRAY + "[Server] Weather set to thunder", null);
            }
            default -> sendSystemMessage(sender, ChatColor.RED + "Unknown weather: " + weather);
        }
    }
    
    private void handleDifficultyCommand(PlayerInterface sender, String[] args) {
        if (args.length < 1) {
            sendSystemMessage(sender, ChatColor.RED + "Usage: /difficulty <peaceful|easy|normal|hard>");
            return;
        }
        
        String difficulty = args[0].toLowerCase();
        sendSystemMessage(sender, ChatColor.GREEN + "Difficulty set to " + difficulty + " (not yet implemented)");
    }
    
    private void handleGiveCommand(PlayerInterface sender, String[] args) {
        if (args.length < 1) {
            sendSystemMessage(sender, ChatColor.RED + "Usage: /give <item> [amount]");
            return;
        }
        
        String itemName = args[0].toUpperCase();
        int amount = args.length > 1 ? Integer.parseInt(args[1]) : 1;
        
        try {
            org.bukkit.Material material = org.bukkit.Material.valueOf(itemName);
            if (material == org.bukkit.Material.AIR) {
                sendSystemMessage(sender, ChatColor.RED + "Cannot give air");
                return;
            }
            
            com.blazingmc.server.inventory.ItemStack item = new com.blazingmc.server.inventory.ItemStack(material, amount);
            
            if (sender instanceof com.blazingmc.server.player.Player) {
                com.blazingmc.server.player.Player player = (com.blazingmc.server.player.Player) sender;
                player.getInventory().addItem(item);
                sendSystemMessage(sender, ChatColor.GREEN + "Given " + amount + " " + itemName);
            } else {
                sendSystemMessage(sender, ChatColor.RED + "Can only give items to players");
            }
        } catch (IllegalArgumentException e) {
            sendSystemMessage(sender, ChatColor.RED + "Unknown item: " + itemName);
        }
    }
    
    private void handleKillCommand(PlayerInterface sender, String[] args) {
        PlayerInterface target = sender;
        
        if (args.length >= 1) {
            target = playerManager.getPlayer(args[0]);
            if (target == null) {
                sendSystemMessage(sender, ChatColor.RED + "Player not found: " + args[0]);
                return;
            }
        }
        
        target.disconnect("Killed by " + sender.getUsername());
        broadcastChatMessage(ChatColor.RED + sender.getUsername() + " was killed", null);
    }
    
    private void handleClearCommand(PlayerInterface sender, String[] args) {
        if (sender instanceof com.blazingmc.server.player.Player) {
            com.blazingmc.server.player.Player player = (com.blazingmc.server.player.Player) sender;
            player.getInventory().clear();
            sendSystemMessage(sender, ChatColor.GREEN + "Inventory cleared");
        }
    }
    
    private String formatChatMessage(PlayerInterface sender, String message) {
        return ChatColor.WHITE + "<" + ChatColor.GREEN + sender.getUsername() + ChatColor.WHITE + "> " + ChatColor.translateAlternateColorCodes(message);
    }
    
    public void broadcastChatMessage(String message, PlayerInterface exclude) {
        ByteBuffer buffer = ByteBuffer.allocate(message.length() * 3 + 64).order(ByteOrder.BIG_ENDIAN);
        
        buffer.put((byte) 0x60);
        
        byte[] uuidBytes = UUID.randomUUID().toString().getBytes();
        buffer.putInt(uuidBytes.length);
        buffer.put(uuidBytes);
        
        byte[] nameBytes = "Server".getBytes();
        buffer.putInt(nameBytes.length);
        buffer.put(nameBytes);
        
        byte[] msgBytes = message.getBytes();
        buffer.putInt(msgBytes.length);
        buffer.put(msgBytes);
        
        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        
        for (PlayerInterface player : playerManager.getPlayers().values()) {
            if (player != exclude) {
                player.sendPacket(0x60, data);
            }
        }
    }
    
    public void sendSystemMessage(PlayerInterface player, String message) {
        ByteBuffer buffer = ByteBuffer.allocate(message.length() * 3 + 32).order(ByteOrder.BIG_ENDIAN);
        
        String json = "{\"text\":\"" + message.replace("\"", "\\\"") + "\"}";
        byte[] jsonBytes = json.getBytes();
        
        buffer.putInt(jsonBytes.length);
        buffer.put(jsonBytes);
        buffer.put((byte) 0);
        
        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        
        player.sendPacket(0x64, data);
    }
    
    public void broadcastSystemMessage(String message) {
        for (PlayerInterface player : playerManager.getPlayers().values()) {
            sendSystemMessage(player, message);
        }
    }
    
    public boolean isProfanityFilterEnabled() {
        return profanityFilter;
    }
    
    public int getChatDistance() {
        return chatDistance;
    }
    
    public boolean isChatFormattingEnabled() {
        return chatFormatting;
    }
}