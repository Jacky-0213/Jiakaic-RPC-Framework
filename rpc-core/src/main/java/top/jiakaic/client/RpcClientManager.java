package top.jiakaic.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import top.jiakaic.handler.RpcResponseMessageHandler;
import top.jiakaic.message.PingMessage;
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
        String res = service.sayHello("陈佳凯进阿里巴巴或者腾讯！！！");
        System.out.println(res + "!!!!!!");
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
                        ch.pipeline().addLast(new IdleStateHandler(0, 60*3, 0));
                        ch.pipeline().addLast(new ChannelDuplexHandler() {
                            @Override
                            public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                IdleStateEvent event = (IdleStateEvent) evt;
                                if (event.state() == IdleState.WRITER_IDLE) {
                                    ctx.writeAndFlush(new PingMessage());
//                                        log.debug("发送心跳包");
                                }
                            }
                        });
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
