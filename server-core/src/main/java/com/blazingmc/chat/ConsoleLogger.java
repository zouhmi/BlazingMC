package com.blazingmc.chat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ConsoleLogger {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    public static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    private static final String WHITE = "\u001B[37m";
    private static final String GRAY = "\u001B[90m";
    private static final String BOLD = "\u001B[1m";
    
    public static void info(String message) {
        String time = LocalDateTime.now().format(TIME_FORMAT);
        System.out.println(GRAY + "[" + time + "]" + RESET + " " + GREEN + BOLD + "[INFO]" + RESET + " " + WHITE + message + RESET);
    }
    
    public static void warn(String message) {
        String time = LocalDateTime.now().format(TIME_FORMAT);
        System.out.println(GRAY + "[" + time + "]" + RESET + " " + YELLOW + BOLD + "[WARN]" + RESET + " " + YELLOW + message + RESET);
    }
    
    public static void error(String message) {
        String time = LocalDateTime.now().format(TIME_FORMAT);
        System.err.println(GRAY + "[" + time + "]" + RESET + " " + RED + BOLD + "[ERROR]" + RESET + " " + RED + message + RESET);
    }
    
    public static void error(String message, Throwable throwable) {
        String time = LocalDateTime.now().format(TIME_FORMAT);
        System.err.println(GRAY + "[" + time + "]" + RESET + " " + RED + BOLD + "[ERROR]" + RESET + " " + RED + message + RESET);
        throwable.printStackTrace(System.err);
    }
    
    public static void debug(String message) {
        String time = LocalDateTime.now().format(TIME_FORMAT);
        System.out.println(GRAY + "[" + time + "]" + RESET + " " + GRAY + BOLD + "[DEBUG]" + RESET + " " + GRAY + message + RESET);
    }
    
    public static void chat(String playerName, String message) {
        String time = LocalDateTime.now().format(TIME_FORMAT);
        System.out.println(GRAY + "[" + time + "]" + RESET + " " + CYAN + BOLD + "[CHAT]" + RESET + " " + WHITE + "<" + GREEN + playerName + WHITE + "> " + ChatColor.toAnsi(message) + RESET);
    }
    
    public static void command(String playerName, String command) {
        String time = LocalDateTime.now().format(TIME_FORMAT);
        System.out.println(GRAY + "[" + time + "]" + RESET + " " + PURPLE + BOLD + "[CMD]" + RESET + " " + WHITE + playerName + ": " + GRAY + "/" + command + RESET);
    }
    
    public static void join(String playerName) {
        String time = LocalDateTime.now().format(TIME_FORMAT);
        System.out.println(GRAY + "[" + time + "]" + RESET + " " + GREEN + BOLD + "[JOIN]" + RESET + " " + GREEN + playerName + WHITE + " joined the server" + RESET);
    }
    
    public static void quit(String playerName) {
        String time = LocalDateTime.now().format(TIME_FORMAT);
        System.out.println(GRAY + "[" + time + "]" + RESET + " " + RED + BOLD + "[QUIT]" + RESET + " " + RED + playerName + WHITE + " left the server" + RESET);
    }
    
    public static void tps(double tps) {
        String time = LocalDateTime.now().format(TIME_FORMAT);
        String tpsColor;
        if (tps >= 18.0) {
            tpsColor = GREEN;
        } else if (tps >= 15.0) {
            tpsColor = YELLOW;
        } else {
            tpsColor = RED;
        }
        System.out.println(GRAY + "[" + time + "]" + RESET + " " + BLUE + BOLD + "[TPS]" + RESET + " " + tpsColor + String.format("%.2f", tps) + RESET);
    }
    
    public static void printBanner() {
        System.out.println("");
        System.out.println(BOLD + CYAN + "  ____                       _ _               " + RESET);
        System.out.println(BOLD + CYAN + " | __ ) _ __ _____      _____| (_)_ __   __ _  " + RESET);
        System.out.println(BOLD + CYAN + " |  _ \\| '__/ _ \\ \\ /\\ / / _ \\ | | '_ \\ / _` | " + RESET);
        System.out.println(BOLD + CYAN + " | |_) | | | (_) \\ V  V /  __/ | | | | | (_| | " + RESET);
        System.out.println(BOLD + CYAN + " |____/|_|  \\___/ \\_/\\_/ \\___|_|_|_| |_|\\__, | " + RESET);
        System.out.println(BOLD + CYAN + "                                          |___/  " + RESET);
        System.out.println("");
        System.out.println(GRAY + "  by " + GREEN + "@Zouhmi" + GRAY + " | " + WHITE + "zouhmi.net" + RESET);
        System.out.println("");
    }
    
    public static void serverStart(String version, int port) {
        printBanner();
        info("Starting " + BOLD + "BlazingMC" + RESET + GREEN + " v" + version + RESET);
        info("Listening on port " + BOLD + port + RESET);
        info("Type " + YELLOW + "'stop'" + RESET + " to shutdown");
        System.out.println("");
    }
    
    public static void serverStop() {
        info("Shutting down server...");
    }
    
    public static void tick(long tickNumber, double tps) {
        String time = LocalDateTime.now().format(TIME_FORMAT);
        String tpsColor;
        if (tps >= 18.0) {
            tpsColor = GREEN;
        } else if (tps >= 15.0) {
            tpsColor = YELLOW;
        } else {
            tpsColor = RED;
        }
        System.out.print("\r" + GRAY + "[" + time + "]" + RESET + " " + BLUE + BOLD + "[TICK]" + RESET + " " + GRAY + "#" + tickNumber + RESET + " " + tpsColor + "TPS: " + String.format("%.2f", tps) + RESET + "    ");
    }
}