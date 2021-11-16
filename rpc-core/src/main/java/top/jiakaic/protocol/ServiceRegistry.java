package top.jiakaic.protocol;

import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author JK
 * @date 2021/11/9 -16:15
 * @Description
 **/
@Slf4j
public class ServiceRegistry {

    static Map<Class<?>, Object> map;

    static {
        map = new ConcurrentHashMap<>();
    }

    public static void putService(String serviceName, String serviceInstance) {
        try {
            Class<?> interfaceClass = Class.forName(serviceName);
            Class<?> instanceClass = Class.forName(serviceInstance);
            map.put(interfaceClass, instanceClass.newInstance());
        } catch (Exception e) {
            log.debug("服务本地注册失败", e);
            throw new RuntimeException("服务本地注册失败");
        }
    }

    public static <T> T getService(Class<T> interfaceClass) {
        return (T) map.get(interfaceClass);
    }
}