package com.anzi.rpc.loadbalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.anzi.rpc.spi.SPI;

import java.util.List;

/**
 * @author anzi
 */
@SPI
public interface LoadBalancer {

    static LoadBalancer getByType(String loadBalanceType){
        switch (loadBalanceType){
            case "random" :
                return new RandomLoadBalancer();
            default:
                return new RoundRobinLoadBalancer();
        }
    }

    Instance select(List<Instance> instances);

}
