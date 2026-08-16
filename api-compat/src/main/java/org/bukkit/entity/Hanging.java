package org.bukkit.entity;

import org.bukkit.BlockFace;

public interface Hanging extends Entity {
    boolean canBePlacedOn();
    void setDirection(BlockFace face);
}