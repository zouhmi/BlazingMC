package com.blazingmc.protocol.handler;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface PlayerManagerInterface {
    PlayerInterface getPlayer(UUID uuid);
    PlayerInterface getPlayer(String username);
    boolean isPlayerOnline(UUID uuid);
    boolean isPlayerOnline(String username);
    int getOnlinePlayerCount();
    int getNextEntityId();
    Map<UUID, PlayerInterface> getPlayers();
    Collection<PlayerInterface> getOnlinePlayers();
}