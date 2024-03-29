package top.jiakaic.registry;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author JK
 * @date 2021/11/15 -20:37
 * @Description
 **/
@Slf4j
public class ZookeeperRegistryCenter implements RegistryCenter {
    final CuratorFramework client;

    public ZookeeperRegistryCenter() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(3000, 10);
        //2.第二种方式
        //CuratorFrameworkFactory.builder();
        client = CuratorFrameworkFactory.builder()
                .connectString("192.168.200.188:2181")
                .sessionTimeoutMs(60 * 1000)
                .connectionTimeoutMs(15 * 1000)
                .retryPolicy(retryPolicy)
                .namespace("registry")
                .build();
        //开启连接
        client.start();
    }

    @Override
    public void register(String serviceName, InetSocketAddress inetSocketAddress) {
        String servicePath = "/" + serviceName;
        try {
            if (client.checkExists().forPath(servicePath) == null) {
                client.create().forPath(servicePath);
            }
            String instanceAddress = servicePath + "/" + inetSocketAddress.getHostString() + ":" + inetSocketAddress.getPort();
            client.create().forPath(instanceAddress);
        } catch (Exception e) {
            log.debug("服务注册失败:{}",e);
            throw new RuntimeException("服务注册失败");
        }
    }

    @Override
    public InetSocketAddress lookupService(String serviceName) {
        try {
            String servicePath = "/" + serviceName;
            List<String> services = client.getChildren().forPath(servicePath);
            String address = services.get(0);
            String[] split = address.split(":");
            return new InetSocketAddress(split[0],Integer.valueOf(split[1]));
        } catch (Exception e) {
            log.debug("未找到服务的实现",e);
            throw new RuntimeException("未找到服务实现");
        }
    }
}
