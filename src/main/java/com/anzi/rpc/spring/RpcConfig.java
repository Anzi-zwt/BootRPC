package com.anzi.rpc.spring;


import com.anzi.rpc.annotation.PropertiesField;
import com.anzi.rpc.annotation.PropertiesPrefix;
import lombok.Data;

/**
 * springboot集成框架时的配置信息，组件从这里拿到配置信息
 *
 * @author anzi
 * */
@PropertiesPrefix("rpc")
@Data
public class RpcConfig {


    @PropertiesField
    private String nacosServerAddress = "localhost:8848"; // Nacos的连接地址

    @PropertiesField
    private String serializerType = "Kryo"; // 序列化方式

    @PropertiesField
    private String loadBalanceType = "random";

    @PropertiesField
    private Integer serverPort = 20880; // 默认服务开启端口

    @PropertiesField
    private String serverIp = "localhost"; // 服务默认在本地启动，只有本机回环可以访问

    static RpcConfig rpcProperties;

    private boolean isInit = false;

    public static RpcConfig getInstance(){
        if (rpcProperties == null){
            rpcProperties = new RpcConfig();
        }
        return rpcProperties;
    }

}
