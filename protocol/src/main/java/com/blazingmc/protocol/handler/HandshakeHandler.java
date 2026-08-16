package com.blazingmc.protocol.handler;

import com.blazingmc.protocol.ProtocolState;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.netty.channel.ChannelHandlerContext;

public class HandshakeHandler {
    private static final Logger logger = LoggerFactory.getLogger(HandshakeHandler.class);
    private ProtocolState nextState;
    private String serverAddress;
    private int serverPort;
    
    public void handle(ChannelHandlerContext ctx, ByteBuf data) {
        int protocolVersion = readVarInt(data);
        
        serverAddress = readString(data);
        
        serverPort = data.readUnsignedShort();
        
        int nextStateId = readVarInt(data);
        nextState = ProtocolState.values()[nextStateId];
        
        logger.debug("Handshake received: protocol={}, address={}, port={}, nextState={}",
                     protocolVersion, serverAddress, serverPort, nextState);
    }
    
    public ProtocolState getNextState() {
        return nextState;
    }
    
    public String getServerAddress() {
        return serverAddress;
    }
    
    public int getServerPort() {
        return serverPort;
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
    
    private String readString(ByteBuf buf) {
        int length = readVarInt(buf);
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        return new String(bytes);
    }
}