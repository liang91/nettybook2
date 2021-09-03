/*
 * Copyright 2013-2018 Lilinfeng.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.phei.netty.protocol.netty.client;

import com.phei.netty.protocol.netty.Constant;
import com.phei.netty.protocol.netty.codec.NettyMessageDecoder;
import com.phei.netty.protocol.netty.codec.NettyMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NettyClient {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
    private int count = 0;

    public void connect(int port, String host) throws Exception {
        try {
            Bootstrap b = new Bootstrap();
            b.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new NettyMessageDecoder(1024 * 1024, 4, 4));
                            ch.pipeline().addLast(new NettyMessageEncoder());
                            ch.pipeline().addLast(new ReadTimeoutHandler(15));
                            ch.pipeline().addLast(new LoginAuthReqHandler());
                            ch.pipeline().addLast(new HeartBeatReqHandler());
                        }
                    });
            ChannelFuture future = b.connect(new InetSocketAddress(host, port)).sync();
            future.channel().closeFuture().sync();
        } finally {
            // 所有资源释放完成之后，清空资源，再次发起重连操作
            executor.execute(() -> {
                ++count;
                if (count > 3) {
                    executor.shutdown();
                    eventLoopGroup.shutdownGracefully();
                    System.out.println("客户端退出");
                    return;
                }
                try {
                    TimeUnit.SECONDS.sleep(1);
                    try {
                        System.out.println("发起第" + count + "次数重连");
                        connect(Constant.PORT, Constant.REMOTE_IP);// 发起重连操作
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static void main(String[] args) throws Exception {
        new NettyClient().connect(Constant.PORT, Constant.REMOTE_IP);
    }
}
