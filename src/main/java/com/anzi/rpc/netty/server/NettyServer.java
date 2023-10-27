package com.anzi.rpc.netty.server;

import com.anzi.rpc.registry.ServiceRegistry;
import com.anzi.rpc.registry.ServiceRegistryLocal;
import com.anzi.rpc.codec.CommonDecoder;
import com.anzi.rpc.codec.CommonEncoder;
import com.anzi.rpc.util.ShutdownHook;
import com.anzi.rpc.registry.ServiceRegistryLocalImpl;
import com.anzi.rpc.registry.NacosServiceRegistry;
import com.anzi.rpc.serializer.CommonSerializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * NIO方式服务提供侧
 *
 * @author anzi
 */
public class NettyServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private int port;

    private String ip;

    private ServiceRegistry serviceRegistry;

    private ServiceRegistryLocal serviceRegistryLocal;

    private final CommonSerializer serializer; // 这个序列化器给服务端的编码器使用，解码的时候根据请求使用对应的序列化器



    public NettyServer(String ip, int port, String serializerType) {
        this.ip = ip;
        this.port = port;
        serviceRegistry = new NacosServiceRegistry();
        serviceRegistryLocal = new ServiceRegistryLocalImpl();
        this.serializer = CommonSerializer.getByType(serializerType);
    }

    public void start() { // 开启网络服务
        ShutdownHook.getShutdownHook().addClearAllHook(); // 首先加一个关闭钩子，清空线程池，注销所有Nacos中的服务
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {

            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .option(ChannelOption.SO_BACKLOG, 256)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS))
                                    .addLast(new CommonEncoder(serializer))
                                    .addLast(new CommonDecoder())
                                    .addLast(new NettyServerHandler());
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(ip, port).sync(); // 线程阻塞直到启动完成
            logger.info("Netty server started on port {}", port);
            channelFuture.channel().closeFuture().sync(); // 当前线程阻塞，直到服务关闭
            logger.info("Netty server stop");
        } catch (InterruptedException e) {
            logger.error("启动服务器时有错误发生: ", e);
        } finally { // 关闭服务线程
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public <T> void publishService(T service, String serviceName) {
        serviceRegistryLocal.addServiceProvider(service, serviceName);
        serviceRegistry.register(serviceName);
    }

}
