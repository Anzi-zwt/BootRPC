package com.anzi.rpc.annotation;


import com.anzi.rpc.spring.ConsumerProcessor;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Import(ConsumerProcessor.class)
@Documented
public @interface EnableRpcConsumer {
}
