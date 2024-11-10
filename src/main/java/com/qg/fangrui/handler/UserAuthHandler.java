package com.qg.fangrui.handler;

import com.alibaba.fastjson.JSONObject;
import com.qg.fangrui.model.UserInfo;
import com.qg.fangrui.util.Constants;
import com.qg.fangrui.util.NettyUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用户认证处理类
 * Created by FunriLy on 2017/6/11.
 * From small beginnings comes great things.
 */
public class UserAuthHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger logger = LoggerFactory.getLogger(UserAuthHandler.class);

    private WebSocketServerHandshaker handshaker;

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object o) throws Exception {
        //传统HTTP接入
        if (o instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) o);
        }
        //WebSocket接入
        else if (o instanceof WebSocketFrame) {
            handleWebSocket(ctx, (WebSocketFrame) o);
        }
    }

    /**
     * 重写心跳检测机制
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //判断evt事件是不是IdleStateEvent事件
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent evnet = (IdleStateEvent) evt;
            //判断是读空闲事件还是写空闲事件还是读写空闲事件
            if (evnet.state().equals(IdleState.READER_IDLE)) {
                //读操作发起操作
                final String remoteAddress = NettyUtil.parseChannelRemoteAddr(ctx.channel());
                logger.warn("NETTY SERVER PIPELINE: IDLE exception [{}]", remoteAddress);
                //移除用户并更新数量
                UserInfo userInfo = UserInfoManager.getUserInfo(ctx.channel());
                UserInfoManager.removeChannel(ctx.channel());
                UserInfoManager.broadCastInfo(Constants.SYSTEM_USER_COUNT,UserInfoManager.getAuthUserCount());
                UserInfoManager.broadCastInfo(Constants.SYSTEM_USER_LIST, UserInfoManager.getUserInfoList());
                if (null != userInfo){
                    UserInfoManager.broadCastInfo(Constants.SYSTEM_OTHER_INFO, "心跳检测发生异常，用户 "+userInfo.getNick()+"("
                            +userInfo.getUserId()+") 已经强制下线!<hr/>");

                }
            }
        }
        ctx.fireUserEventTriggered(evt);
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        //对URL进行判断，如果HTTP解码失败，返回HTTP异常
        if(!request.decoderResult().isSuccess() || !"websocket".equals(request.headers().get("Upgrade"))){
            logger.warn("protobuf don't support websocket");
            ctx.channel().close();
            return;
        }
        WebSocketServerHandshakerFactory handshakerFactory = new WebSocketServerHandshakerFactory(
                Constants.WEBSOCKET_URL,null, false);
        handshaker = handshakerFactory.newHandshaker(request);

        if (handshaker == null){
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            // 动态加入websocket的编解码处理
            handshaker.handshake(ctx.channel(), request);
            UserInfo userInfo = new UserInfo();
            userInfo.setAddr(NettyUtil.parseChannelRemoteAddr(ctx.channel()));
            // 存储已经连接的Channel
            UserInfoManager.addChannel(ctx.channel());
        }
    }

    private void handleWebSocket(ChannelHandlerContext ctx, WebSocketFrame frame) {
        // 判断是否关闭链路命令
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            UserInfo userInfo = UserInfoManager.getUserInfo(ctx.channel());
            System.out.println(userInfo.toString());

            UserInfoManager.removeChannel(ctx.channel());
            //TODO:提示用户退出聊天室
            if (null != userInfo) {
                UserInfoManager.broadCastInfo(Constants.SYSTEM_OTHER_INFO, "用户 "+userInfo.getNick()+"("
                        +userInfo.getUserId()+") 退出聊天室!<hr/>");
            }
            return;
        }
        // 判断是否Ping消息
        if (frame instanceof PingWebSocketFrame) {
            logger.info("ping message:{}", frame.content().retain());
            ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        // 判断是否Pong消息
        if (frame instanceof PongWebSocketFrame) {
            logger.info("pong message:{}", frame.content().retain());
            ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        //非文本(二进制)信息
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(frame.getClass().getName() + " frame type not supported");
        }

        String message = ((TextWebSocketFrame) frame).text();
        JSONObject json = JSONObject.parseObject(message);
        int code = json.getInteger("code");
        Channel channel = ctx.channel();
        switch (code){
            case Constants.PING_CODE:
            case Constants.PONG_CODE:{
                UserInfoManager.updateUserTime(channel);
                logger.info("receive pong message, address: {}",NettyUtil.parseChannelRemoteAddr(channel));
                return;
            }
            case Constants.AUTH_CODe:{
                boolean isSuccess = UserInfoManager.saveUser(channel, json.getString("nick"));
                UserInfoManager.sendInfo(channel,Constants.SYSTEM_AUTH_STATE,isSuccess);
                if (isSuccess) {
                    UserInfo userInfo = UserInfoManager.getUserInfo(channel);
                    //更新在线人数
                    UserInfoManager.broadCastInfo(Constants.SYSTEM_USER_COUNT,UserInfoManager.getAuthUserCount());
                    //更新列表
                    UserInfoManager.broadCastInfo(Constants.SYSTEM_USER_LIST, UserInfoManager.getUserInfoList());
                    if (null != userInfo) {
                        //增加人数
                        UserInfoManager.broadCastInfo(Constants.SYSTEM_OTHER_INFO, "用户 "+ userInfo.getNick() + "(" + userInfo.getUserId()
                                + ") 进入网络聊天室，大家热烈欢迎~<hr/>");
                    }
                }
                return;
            }
            case Constants.MESS_CODE: //普通的消息留给MessageHandler处理
                break;
            case Constants.PRIV_CODE: //私聊的消息留给MessageHandler处理
                break;
            default:
                logger.warn("The code [{}] can't be auth!!!", code);
                return;
        }
        //后续消息交给MessageHandler处理
        ctx.fireChannelRead(frame.retain());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception{
        logger.warn("NETTY SERVER PIPELINE: Unknown exception [{}]", cause.getMessage());
        UserInfoManager.removeChannel(ctx.channel());
        UserInfoManager.broadCastInfo(Constants.SYSTEM_USER_COUNT, UserInfoManager.getAuthUserCount());
        UserInfoManager.broadCastInfo(Constants.SYSTEM_USER_LIST, UserInfoManager.getUserInfoList());
        UserInfoManager.broadCastInfo(Constants.SYSTEM_OTHER_INFO, "网络发生未知错误，与部分用户断开连接，请确保网络正常!<hr/>");
    }

}
