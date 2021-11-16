package top.jiakaic.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import top.jiakaic.annotation.Service;
import top.jiakaic.annotation.ServiceScan;
import top.jiakaic.handler.RpcRequestMessageHandler;
import top.jiakaic.protocol.MessageCodecSharable;
import top.jiakaic.protocol.ProtocolFrameDecoder;
import top.jiakaic.protocol.ServiceRegistry;
import top.jiakaic.registry.ZookeeperRegistryCenter;
import top.jiakaic.util.ReflectUtil;

import java.net.InetSocketAddress;
import java.util.Set;

/**
 * @author JK
 * @date 2021/11/9 -16:01
 * @Description
 **/
@Slf4j
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
        scanServices();
    }

    /**
     * @Author:JK
     * @Description:  服务发布
     *
     */
    public <T> void publishService(T service, String serviceName){
        registry.putService(service, serviceName);
        registryCenter.register(serviceName, new InetSocketAddress(host, port));
    }

    /**
     * @Author:JK
     * @Description:  服务自动注册功能
     *
     */
    public void scanServices() {
        String mainClassName = ReflectUtil.getStackTrace();
        Class<?> startClass;
        try {
            startClass = Class.forName(mainClassName);
            if(!startClass.isAnnotationPresent(ServiceScan.class)) {
                log.error("启动类缺少 @ServiceScan 注解");
                throw new RuntimeException("启动类缺少 @ServiceScan 注解");
            }
        } catch (ClassNotFoundException e) {
            log.error("出现未知错误");
            throw new RuntimeException("出现未知错误");
        }
        String basePackage = startClass.getAnnotation(ServiceScan.class).value();
        if("".equals(basePackage)) {
            String substring = mainClassName.substring(0, mainClassName.lastIndexOf("."));
            basePackage = substring.substring(0,substring.lastIndexOf("."));
        }
        Set<Class<?>> classSet = ReflectUtil.getClasses(basePackage);
        for(Class<?> clazz : classSet) {
            if(clazz.isAnnotationPresent(Service.class)) {
                String serviceName = clazz.getAnnotation(Service.class).name();
                Object obj;
                try {
                    obj = clazz.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    log.error("创建 " + clazz + " 时有错误发生");
                    continue;
                }
                if("".equals(serviceName)) {
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> oneInterface: interfaces){
                        publishService(obj, oneInterface.getCanonicalName());
                    }
                } else {
                    publishService(obj, serviceName);
                }
            }
        }
    }
    /**
     * @Author:JK
     * @Description: 开启服务器，初始化服务器相关设置
     *
     */
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
                            ch.pipeline().addLast(new IdleStateHandler(60*8,0,0));
                            ch.pipeline().addLast(new ChannelDuplexHandler(){
                                @Override
                                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                    IdleStateEvent event = (IdleStateEvent) evt;
                                    if(event.state()== IdleState.READER_IDLE){
                                        ctx.channel().close();
                                        log.debug("关闭客户端");
                                    }
                                }
                            });
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
}
