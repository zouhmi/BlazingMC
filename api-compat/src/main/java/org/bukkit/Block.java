package org.bukkit;

public interface Block {
    int getX();
    int getY();
    int getZ();
    World getWorld();
    Location getLocation();
    BlockState getState();
    Material getType();
    void setType(Material type);
    byte getData();
    int getLightLevel();
    int getLightFromSky();
    int getLightFromBlocks();
    boolean isEmpty();
    boolean isLiquid();
    boolean isSolid();
    Block getRelative(BlockFace face);
    Block getRelative(int modX, int modY, int modZ);
}