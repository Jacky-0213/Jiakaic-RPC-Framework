package top.jiakaic.protocol;

import io.netty.util.concurrent.DefaultPromise;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import top.jiakaic.config.Config;
import top.jiakaic.handler.RpcResponseMessageHandler;
import top.jiakaic.message.RpcRequestMessage;
import top.jiakaic.registry.ZookeeperRegistryCenter;

import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

import static top.jiakaic.client.RpcClientManager.getChannel;


/**
 * @author JK
 * @date 2021/11/9 -22:59
 * @Description
 **/
public class ProxyClient {
    static final ZookeeperRegistryCenter client;

    static {
        client = new ZookeeperRegistryCenter();
    }

    public static <T> T getProxyService(Class<T> serviceClass) {
        String proxyWay = Config.getProxyWay();
        if ("cglib".equals(proxyWay)) {
//            System.out.println("cglib阿");
            return getProxyServiceByCglib(serviceClass);
        } else {
//            System.out.println("jdk阿");
            return getProxyServiceByJdk(serviceClass);
        }
    }

    public static <T> T getProxyServiceByJdk(Class<T> serviceClass) {
        ClassLoader loader = serviceClass.getClassLoader();
        Class<?>[] interfaces = {serviceClass};
        int sequenceId = SequenceIdGenerator.nextId();
        String serviceName = serviceClass.getName();
        InetSocketAddress serviceAddress = client.lookupService(serviceName);
        Object o = Proxy.newProxyInstance(loader, interfaces, (proxy, method, args) -> {//proxy用来加载代理类
            getChannel(serviceAddress).writeAndFlush(new RpcRequestMessage(
                    sequenceId,
                    serviceName,
                    method.getName(),
                    String.class,
                    method.getParameterTypes(),
                    args
            ));
            DefaultPromise<Object> promise = new DefaultPromise<>(getChannel(serviceAddress).eventLoop());
            RpcResponseMessageHandler.PROMISES.put(sequenceId, promise);

            promise.await();
            if (promise.isSuccess()) {
                return promise.getNow();
            } else {
                throw new RuntimeException(promise.cause());
            }

        });
        return (T) o;
    }

    public static <T> T getProxyServiceByCglib(Class<T> serviceClass) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(serviceClass);
        int sequenceId = SequenceIdGenerator.nextId();
        String serviceName = serviceClass.getName();
        InetSocketAddress serviceAddress = client.lookupService(serviceName);
        enhancer.setCallback((MethodInterceptor) (o, method, objects, methodProxy) -> {
            getChannel(serviceAddress).writeAndFlush(new RpcRequestMessage(
                    sequenceId,
                    serviceName,
                    method.getName(),
                    String.class,
                    method.getParameterTypes(),
                    objects
            ));
            return null;
        });
        T t = (T) enhancer.create();
        System.out.println(t);
        return t;
    }

}
