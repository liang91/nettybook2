package com.phei.netty.protocol.netty.codec;

import com.phei.netty.protocol.netty.struct.Header;
import com.phei.netty.protocol.netty.struct.NettyMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class NettyMessageDecoder extends LengthFieldBasedFrameDecoder {
    private final MarshallingDecoder marshallDecoder;

    public NettyMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) throws IOException {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
        marshallDecoder = new MarshallingDecoder();
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null)
            return null;

        NettyMessage message = new NettyMessage();
        Header header = new Header();
        header.setCrcCode(frame.readInt());
        header.setLength(frame.readInt());
        header.setSessionID(frame.readLong());
        header.setType(frame.readByte());
        header.setPriority(frame.readByte());

        int size = frame.readInt();
        if (size > 0) {
            Map<String, Object> attachment = new HashMap<>(size);
            for (int i = 0; i < size; i++) {
                int keyLen = frame.readInt();
                byte[] keyBytes = new byte[keyLen];
                frame.readBytes(keyBytes);
                String key = new String(keyBytes, StandardCharsets.UTF_8);
                attachment.put(key, marshallDecoder.decode(frame));
            }
            header.setAttachment(attachment);
        }
        message.setHeader(header);

        if (frame.readableBytes() > 4) {
            message.setBody(marshallDecoder.decode(frame));
        }
        return message;
    }
}
