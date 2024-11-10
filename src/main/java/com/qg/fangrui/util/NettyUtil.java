package com.qg.fangrui.util;

import io.netty.channel.Channel;

import java.net.SocketAddress;

/**
 * Created by FunriLy on 2017/6/10.
 * From small beginnings comes great things.
 */
public class NettyUtil {

    /**
     * 获得Channel远程主机IP地址
     * @param channel
     * @return
     */
    public static String parseChannelRemoteAddr(final Channel channel){
        if (null == channel){
            return "";
        }

        SocketAddress address = channel.remoteAddress();
        String addr = (address != null ? address.toString() : "");
        if (addr.length() >= 0){
            int index = addr.lastIndexOf("/");
            if (index >= 0 ){
                return addr.substring(index+1);
            }
            return addr;
        }
        return "";
    }
}
