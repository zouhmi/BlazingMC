package com.blazingmc.protocol.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class PacketEncoder extends MessageToByteEncoder<PacketEncoder.PacketData> {
    @Override
    protected void encode(ChannelHandlerContext ctx, PacketData msg, ByteBuf out) throws Exception {
        int packetId = msg.getPacketId();
        ByteBuf data = msg.getData();
        
        int packetLength = getVarIntSize(packetId) + data.readableBytes();
        
        writeVarInt(out, packetLength);
        
        writeVarInt(out, packetId);
        
        out.writeBytes(data);
    }
    
    private void writeVarInt(ByteBuf buf, int value) {
        while ((value & ~0x7F) != 0) {
            buf.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        buf.writeByte(value);
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