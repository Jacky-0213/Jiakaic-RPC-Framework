package top.jiakaic.config;


import top.jiakaic.loadBalance.LoadBalanceAlgorithm;
import top.jiakaic.protocol.Serializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author JK
 * @date 2021/11/8 -23:31
 * @Description
 **/
public abstract class Config {
    static Properties properties;

    static {
        try (InputStream in = Config.class.getResourceAsStream("/application.properties")) {
            properties = new Properties();
            properties.load(in);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    /**
     * @Author:JK
     * @Description:  使用Config类从配置文件中获取动态代理方式，如果未配置默认采用JDK动态代理
     *
     */
    public static String getProxyWay() {
        String value = properties.getProperty("proxy.way");
        if (value == null) {
            return "jdk";
        } else {
            return value;
        }
    }
    /**
     * @Author:JK
     * @Description:  使用Config类从配置文件中获取序列化算法，如果未配置默认采用Java序列化方式
     *
     */
    public static Serializer.Algorithm getSerializerAlgorithm() {
        String value = properties.getProperty("serializer.algorithm");
        if (value == null) {
            return Serializer.Algorithm.Java;
        } else {
            return Serializer.Algorithm.valueOf(value);
        }
    }
    /**
     * @Author:JK
     * @Description: 使用Config类从配置文件中获取负载均衡算法，如果未配置则默认采用轮询算法
     *
     */
    public static LoadBalanceAlgorithm getLoadBalanceAlgorithm(){
        String value = properties.getProperty("loadBalance");
        if(value == null){
            return LoadBalanceAlgorithm.RoundRobinLoadBalance;
        }else{
            return LoadBalanceAlgorithm.valueOf(value);
        }
    }
}
