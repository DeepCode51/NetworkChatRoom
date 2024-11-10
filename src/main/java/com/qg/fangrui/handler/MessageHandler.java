package com.qg.fangrui.handler;

import com.alibaba.fastjson.JSONObject;
import com.qg.fangrui.model.UserInfo;
import com.qg.fangrui.util.Constants;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by FunriLy on 2017/6/11.
 * From small beginnings comes great things.
 */
public class MessageHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
        UserInfo userInfo = UserInfoManager.getUserInfo(ctx.channel());
        if (userInfo != null && userInfo.isAuth()) {
            JSONObject json = JSONObject.parseObject(frame.text());
            // 广播返回用户发送的消息文本
            UserInfoManager.broadcastMess(userInfo.getUserId(), userInfo.getNick(), json.getString("mess"));
        }

        // TODO: 2017/6/23  
//        if (userInfo != null && userInfo.isAuth()) {
//            JSONObject json = JSONObject.parseObject(frame.text());
//            // 判断是聊天消息
//            if (json.getString("code").equals(String.valueOf(Constants.MESS_CODE))) {
//                // 广播返回用户发送的消息文本
//                UserInfoManager.broadcastMess(userInfo.getUserId(), userInfo.getNick(), json.getString("mess"));
//            } else if (json.getString("code").equals(String.valueOf(Constants.PRIV_CODE))) {
//
//            }
//        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        UserInfoManager.removeChannel(ctx.channel());
        UserInfoManager.broadCastInfo(Constants.SYSTEM_USER_COUNT,UserInfoManager.getAuthUserCount());
        UserInfoManager.broadCastInfo(Constants.SYSTEM_USER_LIST, UserInfoManager.getUserInfoList());
        super.channelUnregistered(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        UserInfo userInfo = UserInfoManager.getUserInfo(ctx.channel());
        logger.error("connection error and close the channel", cause);
        UserInfoManager.removeChannel(ctx.channel());
        UserInfoManager.broadCastInfo(Constants.SYSTEM_USER_COUNT, UserInfoManager.getAuthUserCount());
        UserInfoManager.broadCastInfo(Constants.SYSTEM_USER_LIST, UserInfoManager.getUserInfoList());
        UserInfoManager.broadCastInfo(Constants.SYSTEM_OTHER_INFO, "网络连接出错，已尝试重新连接!<hr/>");
    }
}
