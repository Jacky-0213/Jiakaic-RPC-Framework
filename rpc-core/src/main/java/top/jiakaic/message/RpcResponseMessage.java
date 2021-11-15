package top.jiakaic.message;

import lombok.Data;
import lombok.ToString;

/**
 * @author JK
 * @date 2021/11/9 -15:25
 * @Description
 **/
@Data
@ToString(callSuper = true)
public class RpcResponseMessage extends Message {
    private Object returnValue;
    private Exception exceptionValue;

    @Override
    public int getMessageType() {
        return RPC_MESSAGE_TYPE_RESPONSE;
    }
}
