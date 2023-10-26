package com.anzi.rpc.registry;

/**
 * 保存和提供服务实例对象
 * @author anzi
 */
public interface ServiceRegistryLocal {


    <T> void addServiceProvider(T service, String serviceName);

    Object getServiceProvider(String serviceName);

}
