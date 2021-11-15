package top.jiakaic.protocol;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author JK
 * @date 2021/11/9 -20:02
 * @Description
 **/
public abstract class SequenceIdGenerator {
    private static final AtomicInteger id = new AtomicInteger();

    public static int nextId() {
        return id.incrementAndGet();
    }
}
