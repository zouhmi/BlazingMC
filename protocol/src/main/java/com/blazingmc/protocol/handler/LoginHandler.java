package com.blazingmc.protocol.handler;

import com.blazingmc.protocol.codec.PacketEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

public class LoginHandler {
    private final EncryptionUtil encryptionUtil;
    private final boolean onlineMode;
    private final byte[] verifyToken;
    private byte[] sharedSecret;
    private String username;
    private UUID uuid;
    private Cipher encryptCipher;
    private Cipher decryptCipher;

    public LoginHandler(boolean onlineMode) throws NoSuchAlgorithmException {
        this.encryptionUtil = new EncryptionUtil();
        this.onlineMode = onlineMode;
        this.verifyToken = EncryptionUtil.generateVerifyToken();
    }

    public void handleLoginStart(ChannelHandlerContext ctx, ByteBuf data) {
        int usernameLength = readVarInt(data);
        if (usernameLength < 0 || usernameLength > data.readableBytes()) {
            disconnect(ctx, "Invalid username");
            return;
        }

        byte[] usernameBytes = new byte[usernameLength];
        data.readBytes(usernameBytes);
        username = new String(usernameBytes, StandardCharsets.UTF_8);

        if (data.readableBytes() >= 16) {
            uuid = new UUID(data.readLong(), data.readLong());
        } else {
            uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
        }

        if (onlineMode) {
            sendEncryptionRequest(ctx);
        } else {
            sendLoginSuccess(ctx);
        }
    }

    public void handleEncryptionResponse(ChannelHandlerContext ctx, ByteBuf data) {
        try {
            int sharedSecretLength = readVarInt(data);
            if (sharedSecretLength < 0 || sharedSecretLength > data.readableBytes()) {
                disconnect(ctx, "Invalid encryption response");
                return;
            }
            byte[] encryptedSharedSecret = new byte[sharedSecretLength];
            data.readBytes(encryptedSharedSecret);

            int verifyTokenLength = readVarInt(data);
            if (verifyTokenLength < 0 || verifyTokenLength > data.readableBytes()) {
                disconnect(ctx, "Invalid encryption response");
                return;
            }
            byte[] encryptedVerifyToken = new byte[verifyTokenLength];
            data.readBytes(encryptedVerifyToken);

            sharedSecret = encryptionUtil.decryptSharedSecret(encryptedSharedSecret);
            byte[] receivedVerifyToken = encryptionUtil.decryptVerifyToken(encryptedVerifyToken);
            if (!Arrays.equals(verifyToken, receivedVerifyToken)) {
                disconnect(ctx, "Invalid verify token");
                return;
            }

            SecretKeySpec keySpec = new SecretKeySpec(sharedSecret, "AES");
            encryptCipher = Cipher.getInstance("AES/CFB8/NoPadding");
            encryptCipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(sharedSecret));
            decryptCipher = Cipher.getInstance("AES/CFB8/NoPadding");
            decryptCipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(sharedSecret));
            sendLoginSuccess(ctx);
        } catch (Exception exception) {
            disconnect(ctx, "Encryption error");
        }
    }

    private void sendEncryptionRequest(ChannelHandlerContext ctx) {
        ByteBuf buffer = ctx.alloc().buffer();
        writeString(buffer, "");
        byte[] encodedPublicKey = encryptionUtil.getEncodedPublicKey();
        writeVarInt(buffer, encodedPublicKey.length);
        buffer.writeBytes(encodedPublicKey);
        writeVarInt(buffer, verifyToken.length);
        buffer.writeBytes(verifyToken);
        ctx.writeAndFlush(new PacketEncoder.PacketData(0x01, buffer));
    }

    private void sendLoginSuccess(ChannelHandlerContext ctx) {
        ByteBuf buffer = ctx.alloc().buffer();
        buffer.writeLong(uuid.getMostSignificantBits());
        buffer.writeLong(uuid.getLeastSignificantBits());
        writeString(buffer, username);
        writeVarInt(buffer, 0);
        ctx.writeAndFlush(new PacketEncoder.PacketData(0x02, buffer));
    }

    private void disconnect(ChannelHandlerContext ctx, String reason) {
        ByteBuf buffer = ctx.alloc().buffer();
        writeString(buffer, "{\"text\":\"" + reason + "\"}");
        ctx.writeAndFlush(new PacketEncoder.PacketData(0x00, buffer));
        ctx.close();
    }

    private int readVarInt(ByteBuf buffer) {
        int value = 0;
        int shift = 0;
        while (shift < 35) {
            if (!buffer.isReadable()) {
                return -1;
            }
            byte current = buffer.readByte();
            value |= (current & 0x7F) << shift;
            if ((current & 0x80) == 0) {
                return value;
            }
            shift += 7;
        }
        throw new IllegalArgumentException("VarInt is too big");
    }

    private void writeVarInt(ByteBuf buffer, int value) {
        while ((value & ~0x7F) != 0) {
            buffer.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        buffer.writeByte(value);
    }

    private void writeString(ByteBuf buffer, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeVarInt(buffer, bytes.length);
        buffer.writeBytes(bytes);
    }

    public String getUsername() { return username; }
    public UUID getUuid() { return uuid; }
    public boolean isOnlineMode() { return onlineMode; }
    public Cipher getEncryptCipher() { return encryptCipher; }
    public Cipher getDecryptCipher() { return decryptCipher; }
    public byte[] getSharedSecret() { return sharedSecret; }
}
