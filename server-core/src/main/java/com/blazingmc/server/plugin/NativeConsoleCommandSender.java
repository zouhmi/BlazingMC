package com.blazingmc.server.plugin;

import com.blazingmc.chat.ConsoleLogger;
import org.bukkit.command.ConsoleCommandSender;

public class NativeConsoleCommandSender implements ConsoleCommandSender {
    @Override
    public void sendMessage(String message) {
        ConsoleLogger.info(message == null ? "" : message);
    }

    @Override
    public void sendMessage(String[] messages) {
        if (messages != null) {
            for (String message : messages) {
                sendMessage(message);
            }
        }
    }

    @Override
    public String getName() {
        return "CONSOLE";
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }

    @Override
    public boolean isOp() {
        return true;
    }

    @Override
    public void setOp(boolean value) {
    }
}
