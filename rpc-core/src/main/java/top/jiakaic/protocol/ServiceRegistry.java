package top.jiakaic.protocol;

import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author JK
 * @date 2021/11/9 -16:15
 * @Description  服务提供者本地服务实现注册中心
 **/
@Slf4j
public class ServiceRegistry {

    static Map<Class<?>, Object> map;

    static {
        map = new ConcurrentHashMap<>();
    }
    /**
     * @Author:JK
     * @Description:  注册服务到本地服务表
     *
     */
    public static <T> void putService(T service, String serviceName){
        try {
            Class<?> interfaceClass = Class.forName(serviceName);
            map.put(interfaceClass, service);
        } catch (Exception e) {
            log.debug("服务本地注册失败", e);
            throw new RuntimeException("服务本地注册失败");
        }
    }
    /**
     * @Author:JK
     * @Description:  获取具体的服务实现
     *
     */
    public static <T> T getService(Class<T> interfaceClass) {
        return (T) map.get(interfaceClass);
    }
}