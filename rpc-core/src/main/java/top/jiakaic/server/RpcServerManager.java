package top.jiakaic.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import top.jiakaic.handler.RpcRequestMessageHandler;
import top.jiakaic.protocol.MessageCodecSharable;
import top.jiakaic.protocol.ProtocolFrameDecoder;
import top.jiakaic.protocol.ServiceRegistry;
import top.jiakaic.registry.ZookeeperRegistryCenter;

import java.net.InetSocketAddress;

/**
 * @author JK
 * @date 2021/11/9 -16:01
 * @Description
 **/
public class RpcServerManager {
    private final String host;
    private final int port;
    private final ServiceRegistry registry;
    private final ZookeeperRegistryCenter registryCenter;

    public RpcServerManager(String host, int port) {
        this.host = host;
        this.port = port;
        registry = new ServiceRegistry();
        registryCenter = new ZookeeperRegistryCenter();
    }

    public void publishService(String serviceName,String serviceInstance) {
        registry.putService(serviceName,serviceInstance);
        registryCenter.register(serviceName, new InetSocketAddress(host, port));
    }

    public void start() {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable CODEC = new MessageCodecSharable();
        RpcRequestMessageHandler RPC_HANDLER = new RpcRequestMessageHandler();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    .group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ProtocolFrameDecoder());
                            ch.pipeline().addLast(LOGGING_HANDLER);
                            ch.pipeline().addLast(CODEC);
                            ch.pipeline().addLast(RPC_HANDLER);
                        }
                    });
            Channel channel = serverBootstrap.bind(new InetSocketAddress(host, port)).sync().channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        RpcServerManager serverManager = new RpcServerManager("localhost", 8080);
        serverManager.start();
    }
}
