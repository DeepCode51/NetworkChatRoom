package com.qg.fangrui.code;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by FunriLy on 2017/6/10.
 * From small beginnings comes great things.
 */
public abstract class BaseServer implements Server {

    protected DefaultEventLoopGroup defLoopGroup;   //池
    protected NioEventLoopGroup bossGroup;          //两个线程组
    protected NioEventLoopGroup workGroup;
    protected NioServerSocketChannel ssch;
    protected ChannelFuture cf;
    protected ServerBootstrap b;

    public void init(){
//        defLoopGroup = new DefaultEventLoopGroup(8, new Executor() {
//            private AtomicInteger index = new AtomicInteger(0);
//            @Override
//            public void execute(Runnable command) {
//                new Thread(command, "DEFAULTEVENTLOOPGROUP_" + index.incrementAndGet());
//            }
//        });
//        //Runtime.getRuntime().availableProcessors() 返回 处理器数
//        bossGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors(),
//                new Executor() {
//                    private AtomicInteger index = new AtomicInteger(0);
//                    @Override
//                    public void execute(Runnable command) {
//                        new Thread(command, "BOSS_" + index.incrementAndGet());
//                    }
//                });
//        workGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors(),
//                new Executor() {
//                    private AtomicInteger index = new AtomicInteger(0);
//                    @Override
//                    public void execute(Runnable command) {
//                        new Thread(command, "WORK_" + index.incrementAndGet());
//                    }
//                });
        defLoopGroup = new DefaultEventLoopGroup(8);
        bossGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());
        workGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());
        b = new ServerBootstrap();
    }

    @Override
    public void shutdown(){
        if (defLoopGroup != null){
            defLoopGroup.shutdownGracefully();
        }
        //优雅退出
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }
}
