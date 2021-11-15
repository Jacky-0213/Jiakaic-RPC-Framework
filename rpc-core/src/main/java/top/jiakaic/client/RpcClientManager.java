package top.jiakaic.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import top.jiakaic.handler.RpcResponseMessageHandler;
import top.jiakaic.protocol.MessageCodecSharable;
import top.jiakaic.protocol.ProtocolFrameDecoder;
import top.jiakaic.service.HelloService;


import java.net.InetSocketAddress;

import static top.jiakaic.protocol.ProxyClient.getProxyService;


/**
 * @author JK
 * @date 2021/11/9 -16:01
 * @Description
 **/
@Slf4j
public class RpcClientManager {
    private static Channel channel = null;
    private static Object lock = new Object();

    public static void main(String[] args) {
        HelloService service = getProxyService(HelloService.class);
        String 张三 = service.sayHello("张三");
        System.out.println(张三+"!!!!!!");
    }

    //DCL单例channel
    public static Channel getChannel(InetSocketAddress inetSocketAddress) {
        if (channel != null) {
            return channel;
        }
        synchronized (lock) {
            if (channel != null) {
                return channel;
            }
            initChannel(inetSocketAddress);
            return channel;
        }
    }

    //初始化channel
    private static void initChannel(InetSocketAddress inetSocketAddress) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable CODEC = new MessageCodecSharable();
        RpcResponseMessageHandler RPC_HANDLER = new RpcResponseMessageHandler();
        Bootstrap bootstrap = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ProtocolFrameDecoder());
                        ch.pipeline().addLast(LOGGING_HANDLER);
                        ch.pipeline().addLast(CODEC);
                        ch.pipeline().addLast(RPC_HANDLER);
                    }
                });
        try {
            channel = bootstrap.connect(inetSocketAddress).sync().channel();
            channel.closeFuture().addListener(future -> {
                group.shutdownGracefully();
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
