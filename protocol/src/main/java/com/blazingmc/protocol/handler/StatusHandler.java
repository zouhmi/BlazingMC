package com.blazingmc.protocol.handler;

import com.google.gson.JsonObject;
import com.blazingmc.protocol.codec.PacketEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;

public class StatusHandler {
    private final String serverName;
    private final String motd;
    private final int protocolVersion;
    private final String versionName;
    private final int maxPlayers;
    private int onlinePlayers;

    public StatusHandler(String serverName, String motd, int protocolVersion, String versionName,
                         int maxPlayers, int onlinePlayers) {
        this.serverName = serverName;
        this.motd = motd;
        this.protocolVersion = protocolVersion;
        this.versionName = versionName;
        this.maxPlayers = maxPlayers;
        this.onlinePlayers = onlinePlayers;
    }

    public void updateOnlinePlayers(int onlinePlayers) {
        this.onlinePlayers = Math.max(0, onlinePlayers);
    }

    public void handleStatusRequest(ChannelHandlerContext ctx) {
        JsonObject response = new JsonObject();
        JsonObject version = new JsonObject();
        version.addProperty("name", versionName);
        version.addProperty("protocol", protocolVersion);
        response.add("version", version);

        JsonObject players = new JsonObject();
        players.addProperty("max", maxPlayers);
        players.addProperty("online", onlinePlayers);
        response.add("players", players);

        JsonObject description = new JsonObject();
        description.addProperty("text", motd);
        response.add("description", description);
        response.addProperty("enforcesSecureChat", false);
        sendStatusResponse(ctx, response.toString());
    }

    public void handlePing(ChannelHandlerContext ctx, ByteBuf data) {
        if (data.readableBytes() < Long.BYTES) {
            return;
        }
        sendPong(ctx, data.readLong());
    }

    private void sendStatusResponse(ChannelHandlerContext ctx, String json) {
        ByteBuf buffer = ctx.alloc().buffer();
        writeString(buffer, json);
        ctx.writeAndFlush(new PacketEncoder.PacketData(0x00, buffer));
    }

    private void sendPong(ChannelHandlerContext ctx, long payload) {
        ByteBuf buffer = ctx.alloc().buffer(Long.BYTES);
        buffer.writeLong(payload);
        ctx.writeAndFlush(new PacketEncoder.PacketData(0x01, buffer));
    }

    private void writeString(ByteBuf buffer, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeVarInt(buffer, bytes.length);
        buffer.writeBytes(bytes);
    }

    private void writeVarInt(ByteBuf buffer, int value) {
        while ((value & ~0x7F) != 0) {
            buffer.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        buffer.writeByte(value);
    }

    public String getServerName() { return serverName; }
    public String getMotd() { return motd; }
    public int getProtocolVersion() { return protocolVersion; }
    public String getVersionName() { return versionName; }
    public int getMaxPlayers() { return maxPlayers; }
    public int getOnlinePlayers() { return onlinePlayers; }
}
