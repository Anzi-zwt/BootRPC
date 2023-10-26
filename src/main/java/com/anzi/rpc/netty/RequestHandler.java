package com.anzi.rpc.netty;

import com.anzi.rpc.entity.RpcRequest;
import com.anzi.rpc.entity.RpcResponse;
import com.anzi.rpc.enumeration.ResponseCode;
import com.anzi.rpc.registry.ServiceRegistryLocal;
import com.anzi.rpc.registry.ServiceRegistryLocalImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 服务端的远程调用请求处理器
 *
 * @author anzi
 */
public class RequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
    private static final ServiceRegistryLocal SERVICE_REGISTRY_LOCAL;

    static {
        SERVICE_REGISTRY_LOCAL = new ServiceRegistryLocalImpl();
    }

    public Object handle(RpcRequest rpcRequest) {
        Object service = SERVICE_REGISTRY_LOCAL.getServiceProvider(rpcRequest.getInterfaceName() + rpcRequest.getVersionId());
        return invokeTargetMethod(rpcRequest, service);
    }

    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) {
        Object result;
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            result = method.invoke(service, rpcRequest.getParameters());
            logger.info("服务:{} 成功调用方法:{}", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return RpcResponse.fail(ResponseCode.METHOD_NOT_FOUND, rpcRequest.getRequestId());
        }
        return result;
    }

}
