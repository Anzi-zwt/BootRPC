package com.anzi.rpc.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @description: 服务提供方，标记一个实现类
 * @Author: anzi
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RpcService {

    /**
     * 版本
     * @return
     */
    String serviceVersion() default "";

    @AliasFor(
            annotation = Component.class
    )
    String value() default "";
}
