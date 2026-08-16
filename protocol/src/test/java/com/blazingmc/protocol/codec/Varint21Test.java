package com.blazingmc.protocol.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class Varint21Test {
    
    @Test
    void testVarintEncodingSingleByte() {
        ByteBuf buf = Unpooled.buffer();
        try {
            Varint21Encoder encoder = new Varint21Encoder();
            encoder.encode(null, 0, buf);
            
            assertEquals(1, buf.readableBytes());
            assertEquals(0, buf.readByte() & 0xFF);
        } catch (Exception e) {
            fail("Exception during encoding: " + e.getMessage());
        } finally {
            buf.release();
        }
    }
    
    @Test
    void testVarintEncodingTwoBytes() {
        ByteBuf buf = Unpooled.buffer();
        try {
            Varint21Encoder encoder = new Varint21Encoder();
            encoder.encode(null, 128, buf);
            
            assertEquals(2, buf.readableBytes());
            assertEquals(0x80, buf.readByte() & 0xFF);
            assertEquals(0x01, buf.readByte() & 0xFF);
        } catch (Exception e) {
            fail("Exception during encoding: " + e.getMessage());
        } finally {
            buf.release();
        }
    }
    
    @Test
    void testVarintEncodingThreeBytes() {
        ByteBuf buf = Unpooled.buffer();
        try {
            Varint21Encoder encoder = new Varint21Encoder();
            encoder.encode(null, 16384, buf);
            
            assertEquals(3, buf.readableBytes());
            assertEquals(0x80, buf.readByte() & 0xFF);
            assertEquals(0x80, buf.readByte() & 0xFF);
            assertEquals(0x01, buf.readByte() & 0xFF);
        } catch (Exception e) {
            fail("Exception during encoding: " + e.getMessage());
        } finally {
            buf.release();
        }
    }
    
    @Test
    void testVarintRoundTrip() {
        int[] testValues = {0, 1, 127, 128, 255, 256, 16383, 16384, 2097151, 2097152, Integer.MAX_VALUE};
        
        for (int value : testValues) {
            ByteBuf encoded = Unpooled.buffer();
            ByteBuf decodedBuf = Unpooled.buffer();
            
            try {
                Varint21Encoder encoder = new Varint21Encoder();
                encoder.encode(null, value, encoded);
                
                decodedBuf.writeBytes(encoded);
                
                Varint21Decoder decoder = new Varint21Decoder();
                java.util.List<Object> out = new java.util.ArrayList<>();
                decoder.decode(null, decodedBuf, out);
                
                assertFalse(out.isEmpty(), "Decoded result should not be empty for value: " + value);
                assertEquals(value, out.get(0), "Round trip failed for value: " + value);
            } catch (Exception e) {
                fail("Exception during round trip for value " + value + ": " + e.getMessage());
            } finally {
                encoded.release();
                decodedBuf.release();
            }
        }
    }
    
    @Test
    void testVarintDecoderZero() {
        ByteBuf buf = Unpooled.buffer();
        try {
            buf.writeByte(0);
            
            Varint21Decoder decoder = new Varint21Decoder();
            java.util.List<Object> out = new java.util.ArrayList<>();
            decoder.decode(null, buf, out);
            
            assertEquals(1, out.size());
            assertEquals(0, out.get(0));
        } catch (Exception e) {
            fail("Exception during decoding: " + e.getMessage());
        } finally {
            buf.release();
        }
    }
    
    @Test
    void testVarintDecoderIncomplete() {
        ByteBuf buf = Unpooled.buffer();
        try {
            buf.writeByte(0x80);
            
            Varint21Decoder decoder = new Varint21Decoder();
            java.util.List<Object> out = new java.util.ArrayList<>();
            decoder.decode(null, buf, out);
            
            assertTrue(out.isEmpty(), "Incomplete varint should not produce output");
        } catch (Exception e) {
            fail("Exception during decoding: " + e.getMessage());
        } finally {
            buf.release();
        }
    }
    
    @Test
    void testVarintDecoderMaxValue() {
        ByteBuf buf = Unpooled.buffer();
        try {
            buf.writeByte(0xFF);
            buf.writeByte(0xFF);
            buf.writeByte(0xFF);
            buf.writeByte(0xFF);
            buf.writeByte(0x07);
            
            Varint21Decoder decoder = new Varint21Decoder();
            java.util.List<Object> out = new java.util.ArrayList<>();
            decoder.decode(null, buf, out);
            
            assertEquals(1, out.size());
            assertEquals(Integer.MAX_VALUE, out.get(0));
        } catch (Exception e) {
            fail("Exception during decoding: " + e.getMessage());
        } finally {
            buf.release();
        }
    }
}
