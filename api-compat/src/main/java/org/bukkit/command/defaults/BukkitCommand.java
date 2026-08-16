package org.bukkit.command.defaults;

import org.bukkit.command.Command;

public abstract class BukkitCommand extends Command {
    protected BukkitCommand(String name) {
        super(name);
    }
}
