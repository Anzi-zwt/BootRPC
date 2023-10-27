package com.anzi.rpc.netty.client;

import com.anzi.rpc.entity.RpcRequest;
import com.anzi.rpc.entity.RpcResponse;
import com.anzi.rpc.enumeration.RpcError;
import com.anzi.rpc.exception.RpcException;
import com.anzi.rpc.util.SingletonFactory;
import com.anzi.rpc.spring.RpcConfig;
import com.anzi.rpc.loadbalancer.LoadBalancer;
import com.anzi.rpc.registry.NacosServiceDiscovery;
import com.anzi.rpc.registry.ServiceDiscovery;
import com.anzi.rpc.serializer.CommonSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * NIO方式消费侧客户端类
 *
 * @author anzi
 */
public class NettyClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);
    private static final EventLoopGroup group;
    private static final Bootstrap bootstrap;

    static {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class);
    }

    private final ServiceDiscovery serviceDiscovery;
    private final CommonSerializer serializer;

    private final UnprocessedRequests unprocessedRequests;

    private static NettyClient nettyClient;

    public static NettyClient getInstance(){
        if(nettyClient == null){
            nettyClient = new NettyClient(
                    CommonSerializer.getByType(RpcConfig.getInstance().getSerializerType()));
        }
        return nettyClient;
    }


    private NettyClient(CommonSerializer serializer) {
        this.serviceDiscovery = NacosServiceDiscovery.getInstance();
        this.serializer = serializer;
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
    }

    public CompletableFuture<RpcResponse> sendRequest(RpcRequest rpcRequest) {
        if (serializer == null) {
            logger.error("未设置序列化器");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }
        CompletableFuture<RpcResponse> resultFuture = new CompletableFuture<>();
        try {
            InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest.getInterfaceName() + rpcRequest.getVersionId());
            Channel channel = ChannelProvider.get(inetSocketAddress, serializer);
            if (!channel.isActive()) {
                group.shutdownGracefully();
                return null;
            }
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
            channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) future1 -> {
                if (!future1.isSuccess()) {
                    future1.channel().close();
                    resultFuture.completeExceptionally(future1.cause());
                    logger.error("发送消息时有错误发生: ", future1.cause());
                }
            });
        } catch (InterruptedException e) {
            unprocessedRequests.remove(rpcRequest.getRequestId());
            logger.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
        return resultFuture;
    }

}
