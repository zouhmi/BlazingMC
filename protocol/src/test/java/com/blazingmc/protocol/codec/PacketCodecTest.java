package com.blazingmc.protocol.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PacketCodecTest {
    
    @Test
    void testPacketDataCreation() {
        ByteBuf data = Unpooled.buffer();
        try {
            data.writeByte(0x01);
            data.writeByte(0x02);
            data.writeByte(0x03);
            
            PacketDecoder.PacketData packetData = new PacketDecoder.PacketData(0x0E, data);
            
            assertEquals(0x0E, packetData.getPacketId());
            assertEquals(3, packetData.getData().readableBytes());
        } finally {
            data.release();
        }
    }
    
    @Test
    void testEncoderPacketDataCreation() {
        ByteBuf data = Unpooled.buffer();
        try {
            data.writeByte(0x01);
            data.writeByte(0x02);
            
            PacketEncoder.PacketData packetData = new PacketEncoder.PacketData(0x0F, data);
            
            assertEquals(0x0F, packetData.getPacketId());
            assertEquals(2, packetData.getData().readableBytes());
        } finally {
            data.release();
        }
    }
    
    @Test
    void testPacketEncoderWritesVarintLengthAndId() {
        ByteBuf out = Unpooled.buffer();
        ByteBuf data = Unpooled.buffer();
        try {
            data.writeByte(0x42);
            data.writeByte(0x43);
            
            PacketEncoder encoder = new PacketEncoder();
            PacketEncoder.PacketData packetData = new PacketEncoder.PacketData(0x05, data);
            
            encoder.encode(null, packetData, out);
            
            assertTrue(out.readableBytes() > 0, "Encoded packet should have bytes");
            
            int packetLength = readVarInt(out);
            assertEquals(3, packetLength);
            
            int packetId = readVarInt(out);
            assertEquals(0x05, packetId);
            
            assertEquals(0x42, out.readByte() & 0xFF);
            assertEquals(0x43, out.readByte() & 0xFF);
        } catch (Exception e) {
            fail("Exception during encoding: " + e.getMessage());
        } finally {
            out.release();
            data.release();
        }
    }
    
    @Test
    void testPacketDecoderReadsLengthIdAndData() {
        ByteBuf in = Unpooled.buffer();
        try {
            writeVarInt(in, 3);
            writeVarInt(in, 0x0E);
            in.writeByte(0xAA);
            in.writeByte(0xBB);
            
            PacketDecoder decoder = new PacketDecoder();
            java.util.List<Object> out = new java.util.ArrayList<>();
            decoder.decode(null, in, out);
            
            assertEquals(1, out.size());
            
            PacketDecoder.PacketData packetData = (PacketDecoder.PacketData) out.get(0);
            assertEquals(0x0E, packetData.getPacketId());
            assertEquals(2, packetData.getData().readableBytes());
            
            assertEquals(0xAA, packetData.getData().readByte() & 0xFF);
            assertEquals(0xBB, packetData.getData().readByte() & 0xFF);
        } catch (Exception e) {
            fail("Exception during decoding: " + e.getMessage());
        } finally {
            in.release();
        }
    }
    
    @Test
    void testPacketDecoderIncompletePacket() {
        ByteBuf in = Unpooled.buffer();
        try {
            writeVarInt(in, 10);
            writeVarInt(in, 0x01);
            in.writeByte(0x01);
            
            PacketDecoder decoder = new PacketDecoder();
            java.util.List<Object> out = new java.util.ArrayList<>();
            decoder.decode(null, in, out);
            
            assertTrue(out.isEmpty(), "Incomplete packet should not be decoded");
        } catch (Exception e) {
            fail("Exception during decoding: " + e.getMessage());
        } finally {
            in.release();
        }
    }
    
    @Test
    void testPacketDecoderEmptyBuffer() {
        ByteBuf in = Unpooled.buffer();
        try {
            PacketDecoder decoder = new PacketDecoder();
            java.util.List<Object> out = new java.util.ArrayList<>();
            decoder.decode(null, in, out);
            
            assertTrue(out.isEmpty(), "Empty buffer should not produce output");
        } catch (Exception e) {
            fail("Exception during decoding: " + e.getMessage());
        } finally {
            in.release();
        }
    }
    
    @Test
    void testEncodeDecodeRoundTrip() {
        ByteBuf encoded = Unpooled.buffer();
        ByteBuf data = Unpooled.buffer();
        try {
            data.writeByte(0x01);
            data.writeByte(0x02);
            data.writeByte(0x03);
            
            PacketEncoder encoder = new PacketEncoder();
            PacketEncoder.PacketData packetData = new PacketEncoder.PacketData(0x0F, data);
            encoder.encode(null, packetData, encoded);
            
            PacketDecoder decoder = new PacketDecoder();
            java.util.List<Object> out = new java.util.ArrayList<>();
            decoder.decode(null, encoded, out);
            
            assertEquals(1, out.size());
            
            PacketDecoder.PacketData decoded = (PacketDecoder.PacketData) out.get(0);
            assertEquals(0x0F, decoded.getPacketId());
            assertEquals(3, decoded.getData().readableBytes());
        } catch (Exception e) {
            fail("Exception during round trip: " + e.getMessage());
        } finally {
            encoded.release();
            data.release();
        }
    }
    
    @Test
    void testLargePacketId() {
        ByteBuf encoded = Unpooled.buffer();
        ByteBuf data = Unpooled.buffer();
        try {
            data.writeByte(0xFF);
            
            PacketEncoder encoder = new PacketEncoder();
            PacketEncoder.PacketData packetData = new PacketEncoder.PacketData(255, data);
            encoder.encode(null, packetData, encoded);
            
            PacketDecoder decoder = new PacketDecoder();
            java.util.List<Object> out = new java.util.ArrayList<>();
            decoder.decode(null, encoded, out);
            
            assertEquals(1, out.size());
            PacketDecoder.PacketData decoded = (PacketDecoder.PacketData) out.get(0);
            assertEquals(255, decoded.getPacketId());
        } catch (Exception e) {
            fail("Exception during large packet test: " + e.getMessage());
        } finally {
            encoded.release();
            data.release();
        }
    }
    
    private int readVarInt(ByteBuf buf) {
        int value = 0;
        int shift = 0;
        byte b;
        
        do {
            if (!buf.isReadable()) return -1;
            b = buf.readByte();
            value |= (b & 0x7F) << shift;
            if ((b & 0x80) == 0) return value;
            shift += 7;
        } while (true);
    }
    
    private void writeVarInt(ByteBuf buf, int value) {
        while ((value & ~0x7F) != 0) {
            buf.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        buf.writeByte(value);
    }
}
