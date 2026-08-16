package com.blazingmc.protocol.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class PacketDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (!in.isReadable()) {
            return;
        }
        
        in.markReaderIndex();
        
        int packetLength = readVarInt(in);
        if (packetLength < 0) {
            in.resetReaderIndex();
            return;
        }
        
        if (in.readableBytes() < packetLength) {
            in.resetReaderIndex();
            return;
        }
        
        int packetId = readVarInt(in);
        
        int readableBytes = packetLength - getVarIntSize(packetId);
        ByteBuf packetData = in.readBytes(readableBytes);
        
        out.add(new PacketData(packetId, packetData));
    }
    
    private int readVarInt(ByteBuf buf) {
        int value = 0;
        int shift = 0;
        byte b;
        
        do {
            if (!buf.isReadable()) {
                return -1;
            }
            
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
    
    private int getVarIntSize(int value) {
        int size = 0;
        while ((value & ~0x7F) != 0) {
            size++;
            value >>>= 7;
        }
        size++;
        return size;
    }
    
    public static class PacketData {
        private final int packetId;
        private final ByteBuf data;
        
        public PacketData(int packetId, ByteBuf data) {
            this.packetId = packetId;
            this.data = data;
        }
        
        public int getPacketId() { return packetId; }
        public ByteBuf getData() { return data; }
    }
}