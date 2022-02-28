package com.phei.netty.protocol.netty.server;

import com.phei.netty.protocol.netty.MessageType;
import com.phei.netty.protocol.netty.struct.Header;
import com.phei.netty.protocol.netty.struct.NettyMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 登录请求处理
 */
public class LoginAuthRespHandler extends ChannelInboundHandlerAdapter {

    private static final Map<String, AtomicInteger> logins = new ConcurrentHashMap<>();
    private static final String[] whiteList = {"127.0.0.1", "192.168.1.104"};
    private String ip;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        NettyMessage message = (NettyMessage) msg;
        // 处理登录消息
        if (message.getHeader().getType() == MessageType.LOGIN_REQ.value()) {
            NettyMessage resp;
            InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
            ip = address.getAddress().getHostAddress();
            System.out.println("IP:" + ip);
            if (logins.containsKey(ip)) {
                logins.get(ip).incrementAndGet();
                resp = buildResponse((byte) -1);
                System.out.println(address + "重复登录，拒绝");
            } else {
                if (Arrays.asList(whiteList).contains(ip)) {
                    resp = buildResponse((byte) 0);
                    logins.put(ip, new AtomicInteger(1));
                    System.out.println(ip + "首次登录，接受");
                } else {
                    resp = buildResponse((byte) -1);
                    System.out.println(ip + "不在白名单内，拒绝");
                }
            }
            ctx.writeAndFlush(resp);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.print("连接关闭");
        if (logins.get(ip).decrementAndGet() == 0) {
            System.out.println("-清除登录状态");
            logins.remove(ip);
        }
        super.channelInactive(ctx);
    }

    private NettyMessage buildResponse(byte result) {
        NettyMessage message = new NettyMessage();
        Header header = new Header();
        header.setType(MessageType.LOGIN_RESP.value());
        message.setHeader(header);
        message.setBody(result);
        return message;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("发生异常，删除登录状态，关闭连接");
        cause.printStackTrace();
        logins.remove(ip);
        ctx.close();
        ctx.fireExceptionCaught(cause);
    }
}
