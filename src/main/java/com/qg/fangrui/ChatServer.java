package com.qg.fangrui;

import com.qg.fangrui.code.BaseServer;
import com.qg.fangrui.handler.MessageHandler;
import com.qg.fangrui.handler.UserAuthHandler;
import com.qg.fangrui.handler.UserInfoManager;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by FunriLy on 2017/6/11.
 * From small beginnings comes great things.
 */
public class ChatServer extends BaseServer {

    private static final Logger logger = LoggerFactory.getLogger(ChatServer.class);

    private ScheduledExecutorService executorService;
    private int port;

    public ChatServer(int port){
        this.port = port;
        //创建一个定长线程池，支持定时及周期性任务执行
        executorService = Executors.newScheduledThreadPool(2);
    }

    @Override
    public void start() {
        System.out.println("服务器初始化");
        b.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                //用于可能长时间没有数据交流的连接
                .option(ChannelOption.SO_KEEPALIVE, true)   //设置TCP连接，连接会自动测试链接的状态
                .option(ChannelOption.TCP_NODELAY, true)    //启用TCP保活检测
                .option(ChannelOption.SO_BACKLOG, 1024)     //当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度
                .localAddress(new InetSocketAddress(port))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(defLoopGroup,
                                new HttpServerCodec(),   //请求解码器
                                new HttpObjectAggregator(65536),//将多个消息转换成单一的消息对象
                                new ChunkedWriteHandler(),  //支持异步发送大的码流，一般用于发送文件流
                                new IdleStateHandler(60, 0, 0), //检测链路是否读空闲
                                new UserAuthHandler(), //处理握手和认证
                                new MessageHandler()    //处理消息的发送
                        );
                    }
                });

        try {

            cf = b.bind(port).sync();
            InetSocketAddress address = (InetSocketAddress) cf.channel().localAddress();
            logger.info("WebSocketServer start success, port is:{}", address.getPort());

            //定时任务:扫描所有的Channel，关闭失效的Channel
            executorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    logger.info("scanNotActiveChannel --------");
                    UserInfoManager.scanNotActiveChannel();
                }
            }, 3, 60, TimeUnit.SECONDS);

            //定时任务:向所有客户端发送Ping消息
            executorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    UserInfoManager.broadCastPing();
                }
            }, 3, 50, TimeUnit.SECONDS);
            cf.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("WebSocketServer start fail,", e);
        }
    }

    @Override
    public void shutdown() {
        if (executorService != null) {
            executorService.shutdown();
        }
        super.shutdown();
    }
}
