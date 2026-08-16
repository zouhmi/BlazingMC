package com.blazingmc.protocol.handler;

public interface BlockManagerInterface {
    void handleBlockBreak(PlayerInterface player, int x, int y, int z, int status);
    void handleBlockPlacement(PlayerInterface player, int hand, int x, int y, int z, 
                             int face, float cursorX, float cursorY, float cursorZ);
}