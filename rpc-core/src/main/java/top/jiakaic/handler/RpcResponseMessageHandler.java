package top.jiakaic.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import top.jiakaic.message.RpcResponseMessage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author JK
 * @date 2021/11/9 -16:09
 * @Description
 **/
@ChannelHandler.Sharable
public class RpcResponseMessageHandler extends SimpleChannelInboundHandler<RpcResponseMessage> {

    public static final Map<Integer, Promise<Object>> PROMISES = new ConcurrentHashMap<>();
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponseMessage msg) throws Exception {
        //注意此处需要移除promise
        Promise<Object> promise = PROMISES.remove(msg.getSequenceId());
        if(promise!=null){
            Exception exceptionValue = msg.getExceptionValue();
            if(exceptionValue!=null){
                promise.setFailure(exceptionValue);
            }else{
                promise.setSuccess(msg.getReturnValue());
            }
        }
    }
}
