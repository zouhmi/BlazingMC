package com.blazingmc.protocol.handler;

public interface ContainerManagerInterface {
    void handleClickContainer(PlayerInterface player, int windowId, int slot, int button, int stateId);
    void handleCloseContainer(PlayerInterface player, int windowId);
    void handleSetCreativeSlot(PlayerInterface player, int slot, int itemId, int count);
    void handleWindowButtonClick(PlayerInterface player, int windowId, int buttonId);
}
