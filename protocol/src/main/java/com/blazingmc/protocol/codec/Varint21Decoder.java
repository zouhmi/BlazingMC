package com.blazingmc.protocol.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;

import java.util.List;

public class Varint21Decoder extends ByteToMessageDecoder {
    private static final int MAX_VARINT_SIZE = 5;
    
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (!in.isReadable()) {
            return;
        }
        
        in.markReaderIndex();
        int temp = 0;
        int shift = 0;
        byte b;
        
        do {
            if (!in.isReadable()) {
                in.resetReaderIndex();
                return;
            }
            
            b = in.readByte();
            temp |= (b & 0x7F) << shift;
            
            if ((b & 0x80) == 0) {
                out.add(temp);
                return;
            }
            
            shift += 7;
            if (shift >= MAX_VARINT_SIZE * 7) {
                throw new CorruptedFrameException("Varint is too big");
            }
        } while (true);
    }
}