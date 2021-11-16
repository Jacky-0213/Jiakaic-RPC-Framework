package top.jiakaic.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author JK
 * @date 2021/11/16 -10:10
 * @Description  启动类包扫描注解，默认扫描启动类所在路径上层目录及其以下子目录所有服务
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceScan {

    public String value() default "";

}
