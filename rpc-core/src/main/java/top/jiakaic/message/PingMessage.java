package top.jiakaic.message;

/**
 * @Author:JK
 * @Description: 客户端心跳包，用于客户端保活，防止被服务器误判为异常连接
 *
 */
public class PingMessage extends Message {
    @Override
    public int getMessageType() {
        return PingMessage;
    }
}
