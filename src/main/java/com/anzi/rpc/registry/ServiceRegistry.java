package com.anzi.rpc.registry;

import java.net.InetSocketAddress;

/**
 * 服务注册接口
 * @author anzi
 */
public interface ServiceRegistry {

    /**
     * 将一个服务注册进注册表
     *
     * @param serviceName 服务名称
     */
    void register(String serviceName);


}
