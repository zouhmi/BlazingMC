package com.blazingmc.protocol.handler;

import io.netty.channel.ChannelHandlerContext;

import javax.crypto.Cipher;
import java.util.UUID;

public interface ServerInterface {
    void onPlayerJoin(ChannelHandlerContext ctx, UUID uuid, String username, Cipher encryptCipher, Cipher decryptCipher);
    void onPlayerDisconnect(UUID uuid);
    int getMaxPlayers();
    int getOnlinePlayerCount();
    String getServerName();
    String getMotd();
    PlayerManagerInterface getPlayerManager();
    ChatManagerInterface getChatManager();
    WorldInterface getWorld();
    BlockManagerInterface getBlockManager();
    AntiCheatManagerInterface getAntiCheatManager();
    CombatManagerInterface getCombatManager();
    ContainerManagerInterface getContainerManager();
}