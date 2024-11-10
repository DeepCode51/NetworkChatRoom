package com.qg.fangrui.util;

/**
 * Created by FunriLy on 2017/6/10.
 * From small beginnings comes great things.
 */
public class Constants {


    public static String DEFAULT_HOST = "localhost";
    public static int DEFAULT_PORT = 9090;
    public static String WEBSOCKET_URL = "ws://localhost:9090/websocket";

    public static final int AUTH_CODe = 10001;
    public static final int MESS_CODE = 10002;
    public static final int PRIV_CODE = 10003;
    public static final int PING_CODE = 10011;
    public static final int PONG_CODE = 10012;

    /**
     * 系统消息类
     */
    public static final int SYSTEM_USER_COUNT = 20001;  //在线用户数
    public static final int SYSTEM_AUTH_STATE = 20002;  //认证结果
    public static final int SYSTEM_OTHER_INFO = 20003;  //系统通知
    public static final int SYSTEM_USER_LIST = 20004;   //在线用户列表
}
