package org.bukkit.command;

public interface CommandSender {
    void sendMessage(String message);
    void sendMessage(String[] messages);
    String getName();
    boolean hasPermission(String permission);
    boolean isOp();
    void setOp(boolean value);
}