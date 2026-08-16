package org.bukkit.command;

import java.util.HashMap;
import java.util.Map;

public class CommandMap {
    private final Map<String, Command> knownCommands;
    
    public CommandMap() {
        this.knownCommands = new HashMap<>();
    }
    
    public void register(String label, Command command) {
        knownCommands.put(label.toLowerCase(), command);
    }
    
    public Command getCommand(String name) {
        return knownCommands.get(name.toLowerCase());
    }
    
    public boolean dispatch(CommandSender sender, String commandLine) {
        String[] args = commandLine.split("\\s+");
        if (args.length == 0) return false;
        
        String commandLabel = args[0].toLowerCase();
        Command command = knownCommands.get(commandLabel);
        
        if (command == null) return false;
        
        String[] commandArgs = new String[args.length - 1];
        System.arraycopy(args, 1, commandArgs, 0, commandArgs.length);
        
        return command.execute(sender, commandLabel, commandArgs);
    }
    
    public Map<String, Command> getKnownCommands() {
        return knownCommands;
    }
}