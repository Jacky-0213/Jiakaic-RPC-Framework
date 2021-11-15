package top.jiakaic.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import top.jiakaic.message.RpcRequestMessage;
import top.jiakaic.message.RpcResponseMessage;
import top.jiakaic.protocol.ServiceRegistry;
import top.jiakaic.service.HelloService;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author JK
 * @date 2021/11/9 -16:09
 * @Description
 **/
@ChannelHandler.Sharable
@Slf4j
public class RpcRequestMessageHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage msg)  {
        RpcResponseMessage response = new RpcResponseMessage();
        response.setSequenceId(msg.getSequenceId());
        log.debug("id:{}",msg.getSequenceId());
        try {
            HelloService service = (HelloService) ServiceRegistry.getService(Class.forName(msg.getInterfaceName()));
            Method method = service.getClass().getMethod(msg.getMethodName(), msg.getParameterTypes());
            Object invoke = method.invoke(service, msg.getParameterValue());
            response.setReturnValue(invoke);
        } catch (Exception e) {
            e.printStackTrace();
            //此处，注意不能直接抛出异常，异常堆栈信息太大会全部返回导致错误。
            //此外，对于客户端来说异常信息用处不大，只需要返回能看懂的简易消息即可
            response.setExceptionValue(new Exception("远程调用出错"+e.getMessage()));
        }
        ctx.writeAndFlush(response);
    }

    /*
    测试
     */
    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        RpcRequestMessage message = new RpcRequestMessage(
                1,
                "netty.nio.netty.chat.server.service.HelloService",
                "sayHello",
                String.class,
                new Class[]{String.class},
                new Object[]{"heqing"}
        );
        HelloService service = (HelloService) ServiceRegistry.getService(Class.forName(message.getInterfaceName()));
        Method method = service.getClass().getMethod(message.getMethodName(), message.getParameterTypes());
        method.invoke(service,message.getParameterValue());
    }
}
