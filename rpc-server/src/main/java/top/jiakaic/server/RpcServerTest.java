package top.jiakaic.server;

/**
 * @author JK
 * @date 2021/11/11 -11:13
 * @Description
 **/
public class RpcServerTest {
    public static void main(String[] args) {
        RpcServerManager serverManager = new RpcServerManager("localhost", 8080);
        serverManager.publishService("top.jiakaic.service.HelloService","top.jiakaic.service.serviceImpl.HelloServiceImpl");
        serverManager.start();
    }
}
