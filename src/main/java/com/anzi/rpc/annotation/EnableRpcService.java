package com.anzi.rpc.annotation;




import com.anzi.rpc.spring.ServiceProcessor;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ServiceProcessor.class)
public @interface EnableRpcService {

}
