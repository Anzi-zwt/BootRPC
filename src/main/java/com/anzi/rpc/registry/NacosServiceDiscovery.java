package com.anzi.rpc.registry;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.anzi.rpc.spring.RpcConfig;
import com.anzi.rpc.util.NacosService;
import com.anzi.rpc.enumeration.RpcError;
import com.anzi.rpc.exception.RpcException;
import com.anzi.rpc.loadbalancer.LoadBalancer;
import com.anzi.rpc.loadbalancer.RandomLoadBalancer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author anzi
 */
public class NacosServiceDiscovery implements ServiceDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(NacosServiceDiscovery.class);

    private final LoadBalancer loadBalancer;

    private HashMap<String, List<Instance>> serviceAddressMap = new HashMap<>();

    private static NacosServiceDiscovery nacosServiceDiscovery;

    private NacosServiceDiscovery() {
        this.loadBalancer = LoadBalancer.getByType(RpcConfig.getInstance().getLoadBalanceType());
    }

    public static NacosServiceDiscovery getInstance() {
        if(nacosServiceDiscovery == null){
            nacosServiceDiscovery = new NacosServiceDiscovery();
        }
        return nacosServiceDiscovery;
    }


    /**
     * 改进，实现服务的订阅，不需要每次都去nacos查询
     */
    @Override
    public InetSocketAddress lookupService(String serviceName) {
        List<Instance> instances = serviceAddressMap.getOrDefault(serviceName, new ArrayList<>());
        if(instances.size() == 0) {
            logger.error("找不到对应的服务: " + serviceName);
            throw new RpcException(RpcError.SERVICE_NOT_FOUND);
        }
        Instance instance = loadBalancer.select(instances);
        return new InetSocketAddress(instance.getIp(), instance.getPort()); // 获取服务地址对象
    }

    /**
     * 实现服务的订阅
     */
    public void subscribeService(String serviceName){
        if(!serviceAddressMap.containsKey(serviceName)){
            try {
                NacosService.getInstance().getNamingService().subscribe(serviceName, event -> {
                    if(event instanceof NamingEvent){ // 是服务变更事务的话，就更新本地缓存
                        serviceAddressMap.put(serviceName, ((NamingEvent) event).getInstances());
                    }
                });
            } catch (NacosException e) {
                logger.error("订阅" + serviceName + "服务时出现错误");
                throw new RuntimeException(e);
            }
        }
    }

}
