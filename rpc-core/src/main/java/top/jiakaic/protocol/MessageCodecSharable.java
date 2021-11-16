package top.jiakaic.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;
import top.jiakaic.config.Config;
import top.jiakaic.message.Message;
import java.util.List;

/**
 * @author JK
 * @date 2021/11/8 -10:14
 * @Description 可共享的MessageCodec
 * 注意泛型有两个参数，表明是再哪个类之间进行转换
 * 需要配合LengthFieldBasedFrameDecoder使用，确保解码接收到的ByteBuf是完整的
 **/
@Slf4j
@ChannelHandler.Sharable
public class MessageCodecSharable extends MessageToMessageCodec<ByteBuf, Message> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> list) throws Exception {
        ByteBuf out = ctx.alloc().buffer();
        // 4 字节 魔数， 可以第一时间判断消息是否有效
        out.writeBytes(new byte[]{0, 2, 1, 3});
        // 1 字节 协议版本号
        out.writeByte(1);
        // 1 字节 序列化方式  0表示jdk  1表示json
        out.writeByte(Config.getSerializerAlgorithm().ordinal());
        // 1 字节 消息类型
        out.writeByte(msg.getMessageType());
        // 4 字节 请求序号
        out.writeInt(msg.getSequenceId());
        // 1 字节 对齐
        out.writeByte(0xff);
        // 4 字节 消息正文长度
        byte[] bytes = Config.getSerializerAlgorithm().serializer(msg);

        out.writeInt(bytes.length);
        // 消息正文
        out.writeBytes(bytes);

        list.add(out);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        int magicNum = in.readInt();
        byte version = in.readByte();
        byte serializerType = in.readByte();
        byte messageType = in.readByte();
        int sequenceId = in.readInt();
        in.readByte();

        int messageLength = in.readInt();
        byte[] bytes = new byte[messageLength];
        in.readBytes(bytes, 0, messageLength);
        Serializer.Algorithm alogrithm =  Serializer.Algorithm.values()[serializerType];
        Object message = alogrithm.deserializer(Message.getMessageClass(messageType), bytes);//如果是Gson这里不能给父类型

        log.debug("{},{},{},{},{},{}", magicNum, version, serializerType, messageType, sequenceId, messageLength);
        log.debug("消息正文{}", message);
        out.add(message);
    }
}
