package top.jiakaic.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @author JK
 * @date 2021/11/8 -14:49
 * @Description 粘包半包处理器
 **/
public class ProtocolFrameDecoder extends LengthFieldBasedFrameDecoder {
    public ProtocolFrameDecoder() {
        this(1024, 12, 4, 0, 0);
    }

    public ProtocolFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }
}
