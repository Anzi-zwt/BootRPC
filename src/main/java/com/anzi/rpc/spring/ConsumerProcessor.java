package com.anzi.rpc.spring;

import com.anzi.rpc.annotation.RpcReference;
import com.anzi.rpc.netty.client.NettyClient;
import com.anzi.rpc.util.PropertiesUtils;
import com.anzi.rpc.netty.client.NettyClientProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;

/**
 * @description: 消费方处理器，为需要远程调用的Bean注入代理对象，并注入远程调用需要的Bean
 * @Author: anzi
 */
@Configuration
public class ConsumerProcessor implements BeanPostProcessor, EnvironmentAware {

    private Logger logger = LoggerFactory.getLogger(ConsumerProcessor.class);

    private RpcConfig rpcConfig;



    /**
     * 从配置文件中读取配置，并放入配置对象中，配置对象有默认配置，创建这个Bean的时候就会调用
     * @param environment
     */
    @Override
    public void setEnvironment(Environment environment) {
        RpcConfig properties = RpcConfig.getInstance();
        if(properties.isInit()){ // 不需要第二次处理
            rpcConfig = properties;
            return;
        }
        PropertiesUtils.init(properties,environment);
        properties.setInit(true);
        rpcConfig = properties;
        logger.info("读取配置文件成功");
    }

    /**
     * 代理层注入，其余的Bean注册初始化之后的时候都会经过这个方法
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 获取所有字段
        final Field[] fields = bean.getClass().getDeclaredFields();
        // 遍历所有字段找到 RpcReference 注解的字段
        for (Field field : fields) {
            if(field.isAnnotationPresent(RpcReference.class)){
                final RpcReference rpcReference = field.getAnnotation(RpcReference.class);
                String serviceVersion = rpcReference.serviceVersion(); // 需要的版本
                NettyClientProxy nettyClientProxy = new NettyClientProxy(NettyClient.getInstance(), serviceVersion);
                final Class<?> aClass = field.getType();
                field.setAccessible(true);
                // 获得对应接口的代理对象
                Object object = nettyClientProxy.getProxy(aClass);
                try {
                    // 将代理对象设置给字段，注入代理对象
                    field.set(bean,object);
                    field.setAccessible(false);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    logger.info("注入代理对象出错");
                }
            }
        }
        return bean;
    }
}
