package com.qg.fangrui.model;

import io.netty.channel.Channel;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by FunriLy on 2017/6/9.
 * From small beginnings comes great things.
 */
public class UserInfo {
    //线程安全自动递增，产生UID
    private static AtomicInteger uidGener = new AtomicInteger(1000);

    private boolean isAuth = false; // 是否认证
    private long time = 0;          // 登录时间
    private int userId;             // UID
    private String nick;            // 昵称
    private String addr;            // 地址
    private Channel channel;        // 通道
    private String password;        // 密码，后续可以可以完善

    /**
     * get & set
     */
    public boolean isAuth() {
        return isAuth;
    }

    public void setAuth(boolean auth) {
        isAuth = auth;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId() {
        this.userId = uidGener.incrementAndGet();
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
