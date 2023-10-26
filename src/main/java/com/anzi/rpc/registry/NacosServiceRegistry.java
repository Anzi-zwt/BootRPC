package com.anzi.rpc.registry;

import com.alibaba.nacos.api.exception.NacosException;
import com.anzi.rpc.enumeration.RpcError;
import com.anzi.rpc.exception.RpcException;
import com.anzi.rpc.util.NacosService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Nacos服务注册中心
 * @author anzi
 */
public class NacosServiceRegistry implements ServiceRegistry {

    private static final Logger logger = LoggerFactory.getLogger(NacosServiceRegistry.class);

    @Override
    public void register(String serviceName) {
        try {
            NacosService.getInstance().registerService(serviceName);
        } catch (NacosException e) {
            logger.error("注册服务时有错误发生:", e);
            throw new RpcException(RpcError.REGISTER_SERVICE_FAILED);
        }
    }

}
