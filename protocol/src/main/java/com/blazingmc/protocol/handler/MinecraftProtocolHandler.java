package com.blazingmc.protocol.handler;

import com.blazingmc.protocol.ProtocolState;
import com.blazingmc.protocol.codec.PacketDecoder;
import com.blazingmc.protocol.codec.PacketEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import javax.crypto.Cipher;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;

public class MinecraftProtocolHandler extends SimpleChannelInboundHandler<PacketDecoder.PacketData> {
    private ProtocolState currentState = ProtocolState.HANDSHAKE;
    private final HandshakeHandler handshakeHandler;
    private final StatusHandler statusHandler;
    private LoginHandler loginHandler;
    private final KeepAliveHandler keepAliveHandler;
    private final ServerInterface server;
    private final boolean onlineMode;
    private boolean playerJoined;
    private int teleportId;
    
    public MinecraftProtocolHandler(ServerInterface server, boolean onlineMode) {
        this.server = server;
        this.onlineMode = onlineMode;
        this.handshakeHandler = new HandshakeHandler();
        this.statusHandler = new StatusHandler(
            server.getServerName(),
            server.getMotd(),
            765,
            "1.20.4",
            server.getMaxPlayers(),
            server.getOnlinePlayerCount()
        );
        this.keepAliveHandler = new KeepAliveHandler();
        this.playerJoined = false;
        this.teleportId = 0;
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PacketDecoder.PacketData msg) throws Exception {
        int packetId = msg.getPacketId();
        ByteBuf data = msg.getData();
        
        switch (currentState) {
            case HANDSHAKE:
                handleHandshake(ctx, packetId, data);
                break;
            case STATUS:
                handleStatus(ctx, packetId, data);
                break;
            case LOGIN:
                handleLogin(ctx, packetId, data);
                break;
            case CONFIGURATION:
                handleConfiguration(ctx, packetId, data);
                break;
            case PLAY:
                handlePlay(ctx, packetId, data);
                break;
        }
    }
    
    private void handleHandshake(ChannelHandlerContext ctx, int packetId, ByteBuf data) {
        if (packetId == 0x00) {
            handshakeHandler.handle(ctx, data);
            currentState = handshakeHandler.getNextState();
            System.out.println("Transitioning to state: " + currentState);
            
                if (currentState == ProtocolState.STATUS) {
                statusHandler.updateOnlinePlayers(server.getOnlinePlayerCount());
            }
        }
    }
    
    private void handleStatus(ChannelHandlerContext ctx, int packetId, ByteBuf data) {
        if (packetId == 0x00) {
                statusHandler.handleStatusRequest(ctx);
        } else if (packetId == 0x01) {
                statusHandler.handlePing(ctx, data);
        }
    }
    
    private void handleLogin(ChannelHandlerContext ctx, int packetId, ByteBuf data) {
        try {
            if (packetId == 0x00) {
                loginHandler = new LoginHandler(onlineMode);
                loginHandler.handleLoginStart(ctx, data);
            } else if (packetId == 0x01) {
                if (loginHandler != null) {
                    loginHandler.handleEncryptionResponse(ctx, data);
                    
                    if (!playerJoined && (loginHandler.getSharedSecret() != null || !onlineMode)) {
                        server.onPlayerJoin(
                            ctx,
                            loginHandler.getUuid(),
                            loginHandler.getUsername(),
                            loginHandler.getEncryptCipher(),
                            loginHandler.getDecryptCipher()
                        );
                        
                        playerJoined = true;
                        
                        currentState = ProtocolState.PLAY;
                        System.out.println("Player " + loginHandler.getUsername() + " joined the game");
                        
                        sendJoinGamePacket(ctx);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error handling login: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleConfiguration(ChannelHandlerContext ctx, int packetId, ByteBuf data) {
    }
    
    private void handlePlay(ChannelHandlerContext ctx, int packetId, ByteBuf data) {
        if (packetId == 0x12) {
            keepAliveHandler.handleKeepAlive(ctx, data);
        }
        
        if (packetId == 0x00) {
            handleConfirmTeleport(ctx, data);
        }
        
        if (packetId == 0x17) {
            handlePlayerPosition(ctx, data);
        }
        
        if (packetId == 0x18) {
            handlePlayerPositionAndRotation(ctx, data);
        }
        
        if (packetId == 0x19) {
            handlePlayerRotation(ctx, data);
        }
        
        if (packetId == 0x1A) {
            handlePlayerOnGround(ctx, data);
        }
        
        if (packetId == 0x0C) {
            handleUseEntity(ctx, data);
        }
        
        if (packetId == 0x05) {
            handleChatMessage(ctx, data);
        }
        
        if (packetId == 0x04) {
            handleCommand(ctx, data);
        }
        
        if (packetId == 0x08) {
            handleClientInformation(ctx, data);
        }
        
        if (packetId == 0x09) {
            handlePluginMessage(ctx, data);
        }
        
        if (packetId == 0x0B) {
            handleClickContainer(ctx, data);
        }
        
        if (packetId == 0x0D) {
            handleCloseContainer(ctx, data);
        }
        
        if (packetId == 0x10) {
            handleCreativeInventoryAction(ctx, data);
        }
        
        if (packetId == 0x25) {
            handleHeldItemChange(ctx, data);
        }
        
        if (packetId == 0x26) {
            handleSetHeldItem(ctx, data);
        }
        
        if (packetId == 0x03) {
            handleAnimate(ctx, data);
        }
        
        if (packetId == 0x1C) {
            handlePlayerAbilities(ctx, data);
        }
        
        if (packetId == 0x1E) {
            handlePlayerDigging(ctx, data);
        }
        
        if (packetId == 0x2E) {
            handlePlayerBlockPlacement(ctx, data);
        }
        
    }
    
    private void handleConfirmTeleport(ChannelHandlerContext ctx, ByteBuf data) {
        int teleportId = readVarInt(data);
        System.out.println("Confirm teleport: " + teleportId);
    }
    
    private void handleClickContainer(ChannelHandlerContext ctx, ByteBuf data) {
        int windowId = data.readByte();
        int slot = data.readShort();
        byte button = data.readByte();
        int stateId = readVarInt(data);
        
        if (loginHandler != null) {
            PlayerInterface player = server.getPlayerManager().getPlayer(loginHandler.getUuid());
            if (player != null) {
                server.getContainerManager().handleClickContainer(player, windowId, slot, button, stateId);
            }
        }
    }
    
    private void handleCloseContainer(ChannelHandlerContext ctx, ByteBuf data) {
        int windowId = data.readByte();
        
        if (loginHandler != null) {
            PlayerInterface player = server.getPlayerManager().getPlayer(loginHandler.getUuid());
            if (player != null) {
                server.getContainerManager().handleCloseContainer(player, windowId);
            }
        }
    }
    
    private void handleCreativeInventoryAction(ChannelHandlerContext ctx, ByteBuf data) {
        short slot = data.readShort();
        
        boolean hasItem = data.readBoolean();
        int itemId = 0;
        int count = 0;
        
        if (hasItem) {
            itemId = readVarInt(data);
            count = data.readByte();
            int nbt = data.readByte();
        }
        
        if (loginHandler != null) {
            PlayerInterface player = server.getPlayerManager().getPlayer(loginHandler.getUuid());
            if (player != null) {
                server.getContainerManager().handleSetCreativeSlot(player, slot, itemId, count);
            }
        }
    }
    
    private void handleHeldItemChange(ChannelHandlerContext ctx, ByteBuf data) {
        short slot = data.readShort();
        if (slot < 0 || slot > 8 || loginHandler == null) {
            return;
        }
        PlayerInterface player = server.getPlayerManager().getPlayer(loginHandler.getUuid());
        if (player != null) {
            player.setMainHand(slot);
        }
    }
    
    private void handleSetHeldItem(ChannelHandlerContext ctx, ByteBuf data) {
        byte slot = data.readByte();
        System.out.println("Set held item: " + slot);
    }
    
    private void handleAnimate(ChannelHandlerContext ctx, ByteBuf data) {
        int hand = readVarInt(data);
        System.out.println("Animate: hand=" + hand);
    }
    
    private void handlePlayerAbilities(ChannelHandlerContext ctx, ByteBuf data) {
        byte flags = data.readByte();
        float flySpeed = data.readFloat();
        float fov = data.readFloat();
        
        boolean invulnerable = (flags & 0x01) != 0;
        boolean flying = (flags & 0x02) != 0;
        boolean allowFlying = (flags & 0x04) != 0;
        boolean creativeMode = (flags & 0x08) != 0;
        
        System.out.println("Player abilities: invulnerable=" + invulnerable + " flying=" + flying + 
                          " allowFlying=" + allowFlying + " creativeMode=" + creativeMode);
    }
    
    private void handlePlayerDigging(ChannelHandlerContext ctx, ByteBuf data) {
        int status = readVarInt(data);
        long position = data.readLong();
        int x = decodePositionX(position);
        int y = decodePositionY(position);
        int z = decodePositionZ(position);
        int face = readVarInt(data);
        if (data.isReadable()) {
            readVarInt(data);
        }
        
        if (loginHandler != null) {
            PlayerInterface player = server.getPlayerManager().getPlayer(loginHandler.getUuid());
            if (player != null) {
                if (server.getAntiCheatManager().validateBlockReach(player, x, y, z)) {
                    server.getBlockManager().handleBlockBreak(player, x, y, z, status);
                }
            }
        }
    }
    
    private void handlePlayerBlockPlacement(ChannelHandlerContext ctx, ByteBuf data) {
        int hand = readVarInt(data);
        long position = data.readLong();
        int x = decodePositionX(position);
        int y = decodePositionY(position);
        int z = decodePositionZ(position);
        int face = readVarInt(data);
        
        float cursorX = data.readFloat();
        float cursorY = data.readFloat();
        float cursorZ = data.readFloat();
        
        boolean insideBlock = data.readBoolean();
        if (data.isReadable()) {
            readVarInt(data);
        }
        
        if (loginHandler != null) {
            PlayerInterface player = server.getPlayerManager().getPlayer(loginHandler.getUuid());
            if (player != null) {
                if (server.getAntiCheatManager().validateBlockReach(player, x, y, z) &&
                    server.getAntiCheatManager().validateBlockPlaceSpeed(player)) {
                    server.getBlockManager().handleBlockPlacement(player, hand, x, y, z, face, cursorX, cursorY, cursorZ);
                }
            }
        }
    }
    
    private void handleUseEntity(ChannelHandlerContext ctx, ByteBuf data) {
        int entityId = readVarInt(data);
        int action = readVarInt(data);
        
        if (loginHandler != null) {
            PlayerInterface player = server.getPlayerManager().getPlayer(loginHandler.getUuid());
            if (player != null) {
                server.getCombatManager().handleAttackEntity(player, entityId, action);
            }
        }
    }
    
    private void handlePlayerPosition(ChannelHandlerContext ctx, ByteBuf data) {
        double x = data.readDouble();
        double y = data.readDouble();
        double z = data.readDouble();
        boolean onGround = data.readBoolean();
        
        if (loginHandler != null) {
            PlayerInterface player = server.getPlayerManager().getPlayer(loginHandler.getUuid());
            if (player != null) {
                if (server.getAntiCheatManager().validateMovement(player, x, y, z) &&
                    server.getAntiCheatManager().validateFlight(player, y, onGround)) {
                    player.setPosition(x, y, z);
                    player.setOnGround(onGround);
                    player.setLastMovementTime(System.currentTimeMillis());
                    
                    broadcastPositionUpdate(player);
                }
            }
        }
    }
    
    private void handlePlayerPositionAndRotation(ChannelHandlerContext ctx, ByteBuf data) {
        double x = data.readDouble();
        double y = data.readDouble();
        double z = data.readDouble();
        float yaw = data.readFloat();
        float pitch = data.readFloat();
        boolean onGround = data.readBoolean();
        
        if (loginHandler != null) {
            PlayerInterface player = server.getPlayerManager().getPlayer(loginHandler.getUuid());
            if (player != null) {
                if (server.getAntiCheatManager().validateMovement(player, x, y, z) &&
                    server.getAntiCheatManager().validateRotation(player, yaw, pitch) &&
                    server.getAntiCheatManager().validateFlight(player, y, onGround)) {
                    player.setPosition(x, y, z);
                    player.setRotation(yaw, pitch);
                    player.setOnGround(onGround);
                    player.setLastMovementTime(System.currentTimeMillis());
                    
                    broadcastPositionAndRotationUpdate(player);
                }
            }
        }
    }
    
    private void handlePlayerRotation(ChannelHandlerContext ctx, ByteBuf data) {
        float yaw = data.readFloat();
        float pitch = data.readFloat();
        boolean onGround = data.readBoolean();
        
        if (loginHandler != null) {
            PlayerInterface player = server.getPlayerManager().getPlayer(loginHandler.getUuid());
            if (player != null) {
                if (server.getAntiCheatManager().validateRotation(player, yaw, pitch)) {
                    player.setRotation(yaw, pitch);
                    player.setOnGround(onGround);
                    
                    broadcastRotationUpdate(player);
                }
            }
        }
    }
    
    private void handlePlayerOnGround(ChannelHandlerContext ctx, ByteBuf data) {
        boolean onGround = data.readBoolean();
        
        if (loginHandler != null) {
            PlayerInterface player = server.getPlayerManager().getPlayer(loginHandler.getUuid());
            if (player != null) {
                player.setOnGround(onGround);
            }
        }
    }
    
    private void broadcastPositionUpdate(PlayerInterface player) {
        for (PlayerInterface other : server.getPlayerManager().getOnlinePlayers()) {
            if (other.getEntityId() != player.getEntityId()) {
                ByteBuffer buffer = ByteBuffer.allocate(32);
                buffer.putInt(player.getEntityId());
                buffer.putDouble(player.getX());
                buffer.putDouble(player.getY());
                buffer.putDouble(player.getZ());
                buffer.put((byte) (player.isOnGround() ? 1 : 0));
                
                byte[] packetData = new byte[buffer.position()];
                buffer.flip();
                buffer.get(packetData);
                
                other.sendPacket(0x18, packetData);
            }
        }
    }
    
    private void broadcastPositionAndRotationUpdate(PlayerInterface player) {
        for (PlayerInterface other : server.getPlayerManager().getOnlinePlayers()) {
            if (other.getEntityId() != player.getEntityId()) {
                ByteBuffer buffer = ByteBuffer.allocate(48);
                buffer.putInt(player.getEntityId());
                buffer.putDouble(player.getX());
                buffer.putDouble(player.getY());
                buffer.putDouble(player.getZ());
                buffer.putFloat(player.getYaw());
                buffer.putFloat(player.getPitch());
                buffer.put((byte) (player.isOnGround() ? 1 : 0));
                
                byte[] packetData = new byte[buffer.position()];
                buffer.flip();
                buffer.get(packetData);
                
                other.sendPacket(0x19, packetData);
            }
        }
    }
    
    private void broadcastRotationUpdate(PlayerInterface player) {
        for (PlayerInterface other : server.getPlayerManager().getOnlinePlayers()) {
            if (other.getEntityId() != player.getEntityId()) {
                ByteBuffer buffer = ByteBuffer.allocate(24);
                buffer.putInt(player.getEntityId());
                buffer.putFloat(player.getYaw());
                buffer.putFloat(player.getPitch());
                buffer.put((byte) (player.isOnGround() ? 1 : 0));
                
                byte[] packetData = new byte[buffer.position()];
                buffer.flip();
                buffer.get(packetData);
                
                other.sendPacket(0x1A, packetData);
            }
        }
    }
    
    private void handleChatMessage(ChannelHandlerContext ctx, ByteBuf data) {
        String message = readString(data);
        long timestamp = data.readLong();
        long salt = data.readLong();
        
        System.out.println("Chat message: " + message);
        
        if (loginHandler != null) {
            PlayerInterface player = server.getPlayerManager().getPlayer(loginHandler.getUuid());
            if (player != null) {
                server.getChatManager().handleChatMessage(player, message);
            }
        }
    }
    
    private void handleCommand(ChannelHandlerContext ctx, ByteBuf data) {
        String command = readString(data);
        
        System.out.println("Command: " + command);
        
        if (loginHandler != null) {
            PlayerInterface player = server.getPlayerManager().getPlayer(loginHandler.getUuid());
            if (player != null) {
                server.getChatManager().handleCommand(player, "/" + command);
            }
        }
    }
    
    private void handleClientInformation(ChannelHandlerContext ctx, ByteBuf data) {
        String locale = readString(data);
        byte viewDistance = data.readByte();
        byte chatMode = data.readByte();
        boolean chatColors = data.readBoolean();
        byte skinParts = data.readByte();
        byte mainHand = data.readByte();
        boolean enableTextFiltering = data.readBoolean();
        boolean allowServerListings = data.readBoolean();
        
        System.out.println("Client information:");
        System.out.println("  Locale: " + locale);
        System.out.println("  View distance: " + viewDistance);
        System.out.println("  Chat mode: " + chatMode);
        System.out.println("  Chat colors: " + chatColors);
        System.out.println("  Skin parts: " + skinParts);
        System.out.println("  Main hand: " + mainHand);
        
    }
    
    private void handlePluginMessage(ChannelHandlerContext ctx, ByteBuf data) {
        String channel = readString(data);
        byte[] messageData = new byte[data.readableBytes()];
        data.readBytes(messageData);
        
        System.out.println("Plugin message: " + channel);
        
        if (channel.equals("minecraft:brand")) {
            String brand = new String(messageData);
            System.out.println("Client brand: " + brand);
        }
    }
    
    private void sendSystemChatMessage(ChannelHandlerContext ctx, String message) {
        ByteBuf buf = ctx.alloc().buffer();
        
        String json = "{\"text\":\"" + message + "\"}";
        writeString(buf, json);
        
        buf.writeBoolean(false);
        
        ctx.writeAndFlush(new PacketEncoder.PacketData(0x64, buf));
    }
    
    private int readVarInt(ByteBuf buf) {
        int value = 0;
        int shift = 0;
        byte b;
        
        do {
            b = buf.readByte();
            value |= (b & 0x7F) << shift;
            
            if ((b & 0x80) == 0) {
                return value;
            }
            
            shift += 7;
            if (shift >= 35) {
                throw new RuntimeException("Varint is too big");
            }
        } while (true);
    }
    
    private int decodePositionX(long position) {
        return (int) (position >> 38);
    }
    
    private int decodePositionY(long position) {
        return (int) (position << 52 >> 52);
    }
    
    private int decodePositionZ(long position) {
        return (int) (position << 26 >> 38);
    }
    
    private String readString(ByteBuf buf) {
        int length = readVarInt(buf);
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        return new String(bytes);
    }
    
    private void sendJoinGamePacket(ChannelHandlerContext ctx) {
        ByteBuf buf = ctx.alloc().buffer();
        
        PlayerInterface player = loginHandler != null ? server.getPlayerManager().getPlayer(loginHandler.getUuid()) : null;
        buf.writeInt(player != null ? player.getEntityId() : 0);
        
        buf.writeBoolean(false);
        
        writeVarInt(buf, 1);
        
        writeString(buf, "minecraft:overworld");
        
        writeVarInt(buf, server.getMaxPlayers());
        
        writeVarInt(buf, 10);
        
        writeVarInt(buf, 10);
        
        buf.writeBoolean(false);
        
        buf.writeBoolean(true);
        
        buf.writeBoolean(false);
        
        writeString(buf, "minecraft:overworld");
        
        writeString(buf, "minecraft:overworld");
        
        buf.writeLong(0);
        
        buf.writeByte(0);
        
        buf.writeByte(-1);
        
        buf.writeBoolean(false);
        
        buf.writeBoolean(false);
        
        buf.writeBoolean(false);
        
        writeVarInt(buf, 0);
        
        ctx.writeAndFlush(new PacketEncoder.PacketData(0x29, buf));
        
        System.out.println("Join game packet sent");
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connected: " + ctx.channel().remoteAddress());
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client disconnected: " + ctx.channel().remoteAddress());
        keepAliveHandler.removeContext(ctx);
        
        if (playerJoined && loginHandler != null) {
            server.onPlayerDisconnect(loginHandler.getUuid());
        }
    }
    
    private void writeVarInt(ByteBuf buf, int value) {
        while ((value & ~0x7F) != 0) {
            buf.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        buf.writeByte(value);
    }
    
    private void writeString(ByteBuf buf, String str) {
        byte[] bytes = str.getBytes();
        writeVarInt(buf, bytes.length);
        buf.writeBytes(bytes);
    }
}