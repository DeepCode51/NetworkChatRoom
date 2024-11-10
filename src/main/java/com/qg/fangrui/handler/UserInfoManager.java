package com.qg.fangrui.handler;

import com.qg.fangrui.model.UserInfo;
import com.qg.fangrui.proto.ChatProto;
import com.qg.fangrui.util.IsEmptyUtil;
import com.qg.fangrui.util.NettyUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Channel 管理器
 * Created by FunriLy on 2017/6/11.
 * From small beginnings comes great things.
 */
public class UserInfoManager {

    private static final Logger logger = LoggerFactory.getLogger(UserInfoManager.class);
    //设置读写锁
    private static ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);
    //构建安全的数据结构
    private static ConcurrentMap<Channel, UserInfo> userInfos = new ConcurrentHashMap<Channel, UserInfo>();
    private static AtomicInteger userCount = new AtomicInteger(0);

    /**
     * 添加Channel
     * @param channel
     */
    public static void addChannel(Channel channel){
        String remoteAddress = NettyUtil.parseChannelRemoteAddr(channel);
        if (!channel.isActive()){
            logger.error("channel is not active, address : {}", remoteAddress);
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setAddr(remoteAddress);
        userInfo.setChannel(channel);
        userInfo.setTime(System.currentTimeMillis());
        userInfos.put(channel, userInfo);
    }

    /**
     * 保存用户信息与Channel
     * @param channel
     * @param nick
     * @return
     */
    public static boolean saveUser(Channel channel, String nick){
        UserInfo userInfo = userInfos.get(channel);
        if (null == userInfo){
            return false;
        }
        if (!channel.isActive()) {
            logger.error("channel is not active, address: {}, nick: {}", userInfo.getAddr(), nick);
            return false;
        }

        //添加认证
        userCount.incrementAndGet();
        userInfo.setNick(nick);
        userInfo.setAuth(true);
        userInfo.setUserId();
        userInfo.setTime(System.currentTimeMillis());
        return true;
    }

    /**
     * 从缓存中移除Channel，并且关闭Channel
     * @param channel
     */
    public static void removeChannel(Channel channel){
        try {
            logger.warn("channel will be remove, address is :{}", NettyUtil.parseChannelRemoteAddr(channel));
            //加写锁
            rwLock.writeLock().lock();
            channel.close();
            UserInfo userInfo = userInfos.get(channel);
            if (null != userInfo){
                UserInfo tmp = userInfos.remove(channel);
                if (tmp != null && tmp.isAuth()){
                    //减少认证用户
                    userCount.decrementAndGet();
                }
            }
        } finally {
            rwLock.writeLock().unlock();    //解锁
        }
    }

    /**
     * 广播普通消息
     * @param uid
     * @param nick
     * @param message
     */
    public static void broadcastMess(int uid, String nick, String message) {
        if (!IsEmptyUtil.isEmpty(message)) {
            try {
                rwLock.readLock().lock();
                Set<Channel> keySet = userInfos.keySet();
                for (Channel channel : keySet) {
                    UserInfo userInfo = userInfos.get(channel);
                    if (userInfo == null || !userInfo.isAuth()) continue;
                    channel.writeAndFlush(new TextWebSocketFrame(ChatProto.buildMessProto(uid, nick, message)));
                }
            } finally {
                rwLock.readLock().unlock();
            }
        }
    }

    public static void broadcastMessToSomeone(int from, int to, String message){
        // TODO: 2017/6/23  
    }

    /**
     * 广播系统消息
     * @param code
     * @param mess
     */
    public static void broadCastInfo(int code, Object mess) {
        try {
            rwLock.readLock().lock();
            Set<Channel> keySet = userInfos.keySet();
            for (Channel channel : keySet){
                UserInfo userInfo = userInfos.get(channel);
                if (null == userInfo || !userInfo.isAuth()) continue;
                channel.writeAndFlush(new TextWebSocketFrame(ChatProto.buildSystProto(code, mess)));
            }
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * 广播Ping
     */
    public static void broadCastPing(){
        try {
            rwLock.readLock().lock();
            logger.info("broadCastPing userCount: {}", userCount.intValue());
            Set<Channel> keySet = userInfos.keySet();
            for (Channel channel : keySet) {
                UserInfo userInfo = userInfos.get(channel);
                if (userInfo == null || !userInfo.isAuth()) continue;
                channel.writeAndFlush(new TextWebSocketFrame(ChatProto.buildPingProto()));
            }
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * 向某个Channel发送系统消息
     * @param channel
     * @param code
     * @param mess
     */
    public static void sendInfo(Channel channel, int code, Object mess) {
        channel.writeAndFlush(new TextWebSocketFrame(ChatProto.buildSystProto(code, mess)));
    }

    /**
     * 向某个Channel发送Pong
     * @param channel
     */
    public static void sendPong(Channel channel) {
        channel.writeAndFlush(new TextWebSocketFrame(ChatProto.buildPongProto()));
    }

    /**
     * 扫描并关闭失效的Channel
     */
    public static void scanNotActiveChannel() {
        Set<Channel> keySet = userInfos.keySet();
        for (Channel channel : keySet) {
            UserInfo userInfo = userInfos.get(channel);
            if (userInfo == null) continue;
            if (!channel.isOpen() || !channel.isActive() || (!userInfo.isAuth() &&
                    (System.currentTimeMillis() - userInfo.getTime()) > 10000)) {
                removeChannel(channel);
            }
        }
    }

    /**
     * 获取某个Channel的用户信息
     * @param channel
     * @return
     */
    public static UserInfo getUserInfo(Channel channel) {
        return userInfos.get(channel);
    }

    /**
     * 获取所有在线用户集合
     * @return
     */
    public static ConcurrentMap<Channel, UserInfo> getUserInfos() {
        return userInfos;
    }

    /**
     * 获得在线晕乎乎列表
     * @return
     */
    public static List<String> getUserInfoList(){
        List<String> userInfoList = new ArrayList<String>();
        try {
            rwLock.readLock().lock();
            Set<Channel> keySet = userInfos.keySet();
            for (Channel channel : keySet) {
                UserInfo userInfo = getUserInfo(channel);
                if (null == userInfo) continue;
                userInfoList.add(userInfo.getNick() + "(" + userInfo.getUserId() + ")");
            }
        } finally {
            rwLock.readLock().unlock();
        }
        return userInfoList;
    }

    /**
     * 获取在线人数
     * @return
     */
    public static int getAuthUserCount() {
        return userCount.get();
    }

    /**
     * 更新用户的登录时间
     * @param channel
     */
    public static void updateUserTime(Channel channel) {
        UserInfo userInfo = getUserInfo(channel);
        if (userInfo != null) {
            userInfo.setTime(System.currentTimeMillis());
        }
    }
}
