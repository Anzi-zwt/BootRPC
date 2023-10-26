package com.anzi.rpc.spring;

import com.anzi.rpc.annotation.RpcService;
import com.anzi.rpc.netty.server.NettyServer;
import com.anzi.rpc.util.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

public class ServiceProcessor implements EnvironmentAware, BeanPostProcessor, InitializingBean {


    private Logger logger = LoggerFactory.getLogger(ServiceProcessor.class);

    private RpcConfig rpcConfig;

    private NettyServer nettyServer;

    /**
     * @配置文件的读取
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
     * @在这个Bean调用自身初始化方法初始化之前调用，即这个Bean注册的时候就会调用，用来一开始启动Netty服务
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        NettyServer nettyServer = new NettyServer(RpcConfig.getInstance().getServerPort(), RpcConfig.getInstance().getSerializerType());
        this.nettyServer = nettyServer;
        // 新建一个线程去启动，这个线程会阻塞，最后负责处理关闭服务线程
        Thread thread = new Thread(() -> {
            nettyServer.start();
        });
        thread.start();
    }


    /**
     * @RpcService注解的处理，Bean注册初始化的后处理，注册和发布服务
     */
    public Object postProcessAfterInitialization(Object bean, String beanName){
        if (!bean.getClass().isAnnotationPresent(RpcService.class)) {
            return bean;
        }
        String serviceVersion = bean.getClass().getAnnotation(RpcService.class).serviceVersion();
        nettyServer.publishService(bean, bean.getClass().getInterfaces()[0].getCanonicalName() + serviceVersion);
        return bean;

    }



}
