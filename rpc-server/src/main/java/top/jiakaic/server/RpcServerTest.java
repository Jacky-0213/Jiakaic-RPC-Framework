package top.jiakaic.server;

import top.jiakaic.annotation.ServiceScan;

/**
 * @author JK
 * @date 2021/11/11 -11:13
 * @Description
 **/
@ServiceScan
public class RpcServerTest {
    public static void main(String[] args) {
        RpcServerManager serverManager = new RpcServerManager("localhost", 8080);
        serverManager.start();
    }
}
