package com.anzi.rpc.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author anzi
 */
public class ShutdownHook {

    private static final Logger logger = LoggerFactory.getLogger(ShutdownHook.class);

    private static final ShutdownHook shutdownHook = new ShutdownHook();

    public static ShutdownHook getShutdownHook() {
        return shutdownHook;
    }

    public void addClearAllHook() {
        logger.info("关闭后将自动注销所有注册服务");
        // 添加一个jvm即将关闭时会调用启动的线程
        Runtime.getRuntime().addShutdownHook(new Thread(() -> { // lambda表达式实现Runnable接口，表示钩子线程启动之后需要做什么
            NacosService.getInstance().clearRegistry();
            logger.info("成功注销所有服务");
        }));
    }

}
