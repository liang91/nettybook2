package com.phei.netty.protocol.netty.codec;

import com.phei.netty.protocol.netty.struct.Header;
import com.phei.netty.protocol.netty.struct.NettyMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class NettyMessageEncoder extends MessageToByteEncoder<NettyMessage> {

    private final MarshallingEncoder marshaller;

    public NettyMessageEncoder() throws IOException {
        marshaller = new MarshallingEncoder();
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, NettyMessage msg, ByteBuf sendBuf) throws Exception {
        // header
        Header header = msg.getHeader();
        sendBuf.writeInt(header.getCrcCode());
        sendBuf.writeInt(header.getLength());
        sendBuf.writeLong(header.getSessionID());
        sendBuf.writeByte(header.getType());
        sendBuf.writeByte(header.getPriority());
        sendBuf.writeInt(header.getAttachment().size());
        byte[] keyBytes;
        Object value;
        for (Map.Entry<String, Object> entry : header.getAttachment().entrySet()) {
            keyBytes = entry.getKey().getBytes(StandardCharsets.UTF_8);
            sendBuf.writeInt(keyBytes.length);
            sendBuf.writeBytes(keyBytes);
            value = entry.getValue();
            marshaller.encode(value, sendBuf);
        }

        // body
        if (msg.getBody() != null) {
            marshaller.encode(msg.getBody(), sendBuf);
        } else {
            sendBuf.writeInt(0);
        }
        sendBuf.setInt(4, sendBuf.readableBytes() - 8);
    }
}
