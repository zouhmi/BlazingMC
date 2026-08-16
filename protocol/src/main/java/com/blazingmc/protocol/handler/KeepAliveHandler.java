package com.blazingmc.protocol.handler;

import com.blazingmc.protocol.codec.PacketEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KeepAliveHandler {
    private static final long KEEP_ALIVE_INTERVAL = 30000;
    private static final long KEEP_ALIVE_TIMEOUT = 10000;
    private final Map<ChannelHandlerContext, PendingKeepAlive> pendingKeepAlives;
    private long lastKeepAliveTime;
    private boolean enabled;

    public KeepAliveHandler() {
        this.pendingKeepAlives = new ConcurrentHashMap<>();
        this.lastKeepAliveTime = System.currentTimeMillis();
        this.enabled = true;
    }

    public void tick(ChannelHandlerContext ctx) {
        if (!enabled) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastKeepAliveTime >= KEEP_ALIVE_INTERVAL) {
            sendKeepAlive(ctx);
            lastKeepAliveTime = currentTime;
        }

        for (Map.Entry<ChannelHandlerContext, PendingKeepAlive> entry : pendingKeepAlives.entrySet()) {
            if (currentTime - entry.getValue().sentAt() >= KEEP_ALIVE_TIMEOUT) {
                entry.getKey().close();
                pendingKeepAlives.remove(entry.getKey(), entry.getValue());
            }
        }
    }

    public void handleKeepAlive(ChannelHandlerContext ctx, ByteBuf data) {
        if (data.readableBytes() < Long.BYTES) {
            return;
        }

        long responseId = data.readLong();
        PendingKeepAlive pending = pendingKeepAlives.get(ctx);
        if (pending != null && pending.id() == responseId) {
            pendingKeepAlives.remove(ctx, pending);
        }
    }

    private void sendKeepAlive(ChannelHandlerContext ctx) {
        long keepAliveId = System.currentTimeMillis();
        ByteBuf buffer = ctx.alloc().buffer(Long.BYTES);
        buffer.writeLong(keepAliveId);
        ctx.writeAndFlush(new PacketEncoder.PacketData(0x21, buffer));
        pendingKeepAlives.put(ctx, new PendingKeepAlive(keepAliveId, System.currentTimeMillis()));
    }

    public void removeContext(ChannelHandlerContext ctx) {
        pendingKeepAlives.remove(ctx);
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public int getPendingCount() { return pendingKeepAlives.size(); }

    private record PendingKeepAlive(long id, long sentAt) { }
}
