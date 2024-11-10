package com.qg.fangrui.proto;

import com.alibaba.fastjson.JSONObject;
import com.qg.fangrui.util.DateUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 聊天室的协议
 * {head(四字节)}{body}{tend}
 * 模仿《Netty权威指南》私有协议栈的开发
 * Created by FunriLy on 2017/6/10.
 * From small beginnings comes great things.
 */
public class ChatProto {

    public static final int PING_PROTO = 1 << 8 | 220; //ping消息(476)
    public static final int PONG_PROTO = 2 << 8 | 220; //pong消息(732)
    public static final int SYST_PROTO = 3 << 8 | 220; //系统消息(988)
    public static final int EROR_PROTO = 4 << 8 | 220; //错误消息(1244)
    public static final int AUTH_PROTO = 5 << 8 | 220; //认证消息(1500)
    public static final int MESS_PROTO = 6 << 8 | 220; //普通消息(1756)
    public static final int PRIV_PROTO = 7 << 8 | 220; //私聊消息(2012)

    private int version = 1;
    private int head;
    private String body;
    private Map<String,Object> extend = new HashMap<String,Object>();

    public ChatProto(int head, String body){
        this.head = head;
        this.body = body;
    }

    /**
     * 构建多种消息的构造器
     */
    public static String buildPingProto(){
        return buildProto(PING_PROTO, null);
    }

    public static String buildPongProto(){
        return buildProto(PONG_PROTO, null);
    }

    public static String buildSystProto(int code, Object mess){
        ChatProto chatProto = new ChatProto(SYST_PROTO, null);
        chatProto.extend.put("code", code);
        chatProto.extend.put("mess", mess);
        return JSONObject.toJSONString(chatProto);
    }

    public static String buildErrorProtr(int code,String mess){
        ChatProto chatProto = new ChatProto(EROR_PROTO, null);
        chatProto.extend.put("code", code);
        chatProto.extend.put("mess", mess);
        return JSONObject.toJSONString(chatProto);
    }

    public static String buildAuthProto(boolean isSuccess) {
        ChatProto chatProto = new ChatProto(AUTH_PROTO, null);
        chatProto.extend.put("isSuccess", isSuccess);
        return JSONObject.toJSONString(chatProto);
    }

    /**
     * 广播消息的主要封装方法
     * @param uid
     * @param nick
     * @param mess
     * @return
     */
    public static String buildMessProto(int uid, String nick, String mess) {
        ChatProto chatProto = new ChatProto(MESS_PROTO, mess);
        chatProto.extend.put("uid", uid);
        chatProto.extend.put("nick", nick);
        chatProto.extend.put("time", DateUtil.getCurrentDateTime());
        return JSONObject.toJSONString(chatProto);
    }

    public static String buildPrivateMessage(int from, String fromNick, int to, String toNick, String mess){
        // TODO: 2017/6/23  
        return null;
    }

    public static String buildProto(int head, String body){
        ChatProto chatProto = new ChatProto(head, body);
        return JSONObject.toJSONString(chatProto);
    }

    /**
     * get & set
     */
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getHead() {
        return head;
    }

    public void setHead(int head) {
        this.head = head;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, Object> getExtend() {
        return extend;
    }

    public void setExtend(Map<String, Object> extend) {
        this.extend = extend;
    }
}
