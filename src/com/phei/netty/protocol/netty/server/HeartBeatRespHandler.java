package com.phei.netty.protocol.netty.server;

import com.phei.netty.protocol.netty.MessageType;
import com.phei.netty.protocol.netty.struct.Header;
import com.phei.netty.protocol.netty.struct.NettyMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class HeartBeatRespHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        NettyMessage message = (NettyMessage) msg;
        if (message.getHeader().getType() == MessageType.HEARTBEAT_REQ.value()) {
            System.out.println("收到心跳消息");
            NettyMessage heartBeat = buildHeatBeat();
            System.out.println("发送心跳反馈");
            ctx.writeAndFlush(heartBeat);
        } else {
			ctx.fireChannelRead(msg);
		}
    }

    private NettyMessage buildHeatBeat() {
        NettyMessage message = new NettyMessage();
        Header header = new Header();
        header.setType(MessageType.HEARTBEAT_RESP.value());
        message.setHeader(header);
        return message;
    }
}
