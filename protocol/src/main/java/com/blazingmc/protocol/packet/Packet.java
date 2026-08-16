package com.blazingmc.protocol.packet;

import io.netty.buffer.ByteBuf;

public interface Packet {
    int getId();
    void encode(ByteBuf buf);
    void decode(ByteBuf buf);
}