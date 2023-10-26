package com.anzi.rpc.loadbalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;

/**
 * @author anzi
 */
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
