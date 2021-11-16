package top.jiakaic.client;

import top.jiakaic.service.HelloService;

import static top.jiakaic.protocol.ProxyClient.getProxyService;

/**
 * @author JK
 * @date 2021/11/11 -11:12
 * @Description
 **/
public class RpcClientTest {
    public static void main(String[] args) {
        HelloService service = getProxyService(HelloService.class);
        String res = service.sayHello("，佳凯进阿里和腾讯");
        System.out.println(res+"!!!!!!");
    }
}
