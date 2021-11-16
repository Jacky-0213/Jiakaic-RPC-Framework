package top.jiakaic.service.serviceImpl;


import top.jiakaic.annotation.Service;
import top.jiakaic.service.HelloService;

/**
 * @author JK
 * @date 2021/11/9 -16:23
 * @Description
 **/
@Service
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        System.out.println("你好" + name);
        return "你好"+name;
    }
}
