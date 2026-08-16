package com.blazingmc.protocol.handler;

public interface ChatManagerInterface {
    void handleChatMessage(PlayerInterface sender, String message);
    void handleCommand(PlayerInterface sender, String command);
    void broadcastChatMessage(String message, PlayerInterface exclude);
    void sendSystemMessage(PlayerInterface player, String message);
    void broadcastSystemMessage(String message);
}