package top.jiakaic.loadBalance;

import java.util.List;
import java.util.Random;

/**
 * @author JK
 * @date 2021/11/16 -8:46
 * @Description 负载均衡算法
 **/
public enum LoadBalanceAlgorithm implements LoadBalance {

    RandomLoadBalance {
        @Override
        public String select(List<String> services) {
            return services.get(new Random().nextInt(services.size()));
        }
    },
    RoundRobinLoadBalance {
        private int index = 0;

        @Override
        public String select(List<String> services) {
            if (index >= services.size()) {
                index %= services.size();
            }
            return services.get(index++);
        }
    }
}
