package top.jiakaic.loadBalance;

import java.util.List;

/**
 * @author JK
 * @date 2021/11/16 -8:42
 * @Description 负载均衡机的
 **/
public interface LoadBalance {
    String select(List<String> services);

}
