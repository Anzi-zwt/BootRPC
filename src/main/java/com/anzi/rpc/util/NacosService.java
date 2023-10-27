package com.anzi.rpc.util;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.anzi.rpc.enumeration.RpcError;
import com.anzi.rpc.exception.RpcException;
import com.anzi.rpc.spring.RpcConfig;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 提供Nacos服务
 * @author anzi
 */
@Data
public class NacosService {

    private static final Logger logger = LoggerFactory.getLogger(NacosService.class);

    private NamingService namingService;
    private Set<String> serviceNames = new HashSet<>();


    private static NacosService nacosService;
    public static NacosService getInstance(){
        if(nacosService == null){
            nacosService = new NacosService();
            try {
                nacosService.setNamingService(NamingFactory.createNamingService(RpcConfig.getInstance().getNacosServerAddress()));
            } catch (NacosException e) {
                logger.error("连接到Nacos时发生错误: ", e);
                throw new RpcException(RpcError.FAILED_TO_CONNECT_TO_SERVICE_REGISTRY);
            }
        }
        return nacosService;
    }

    public void registerService(String serviceName) throws NacosException {
        namingService.registerInstance(serviceName, RpcConfig.getInstance().getServerIp(), RpcConfig.getInstance().getServerPort());
        serviceNames.add(serviceName);

    }


    public void clearRegistry() {
        if(!serviceNames.isEmpty()) {
            String host = RpcConfig.getInstance().getServerIp();
            int port = RpcConfig.getInstance().getServerPort();
            Iterator<String> iterator = serviceNames.iterator();
            while(iterator.hasNext()) {
                String serviceName = iterator.next();
                try {
                    namingService.deregisterInstance(serviceName, host, port);
                } catch (NacosException e) {
                    logger.error("注销服务 {} 失败", serviceName, e);
                }
            }
        }
    }
}
