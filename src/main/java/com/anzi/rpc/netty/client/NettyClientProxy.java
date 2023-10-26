package com.anzi.rpc.netty.client;

import com.anzi.rpc.entity.RpcRequest;
import com.anzi.rpc.entity.RpcResponse;
import com.anzi.rpc.netty.RpcMessageChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * RPC调用端动态代理类，每个代理对象，对应一个这个对象帮他执行方法，并调用client发送请求
 *
 * @author anzi
 */
public class NettyClientProxy implements InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(NettyClientProxy.class);

    private final NettyClient client;

    private final String version;

    public NettyClientProxy(NettyClient client, String version) {
        this.client = client;
        this.version = version;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        logger.info("调用方法: {}#{}", method.getDeclaringClass().getName(), method.getName());
        RpcRequest rpcRequest = new RpcRequest(UUID.randomUUID().toString(), method.getDeclaringClass().getName(),
                method.getName(), args, method.getParameterTypes(), false, version);
        RpcResponse rpcResponse = null;
        if (client instanceof NettyClient) {
            try {
                CompletableFuture<RpcResponse> completableFuture = (CompletableFuture<RpcResponse>) client.sendRequest(rpcRequest);
                rpcResponse = completableFuture.get();
            } catch (Exception e) {
                logger.error("方法调用请求发送失败", e);
                return null;
            }
        }
        RpcMessageChecker.check(rpcRequest, rpcResponse);
        return rpcResponse.getData();
    }
}
