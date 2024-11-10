package com.qg.fangrui.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 判断对象是否为空工具类
 * Created by FunriLy on 2017/6/10.
 * From small beginnings comes great things.
 */
public class IsEmptyUtil {

    /**
     * 判断字符串是否为空
     * @param message
     * @return
     */
    public static boolean isEmpty(final String message){
        return (message == null) || (message.trim().length() <= 0);
    }

    /**
     * 判断字符是否为空
     * @param message
     * @return
     */
    public static boolean isEmpty(final Character message){
        return (message == null) || message.equals(' ');
    }

    /**
     * 判断对象是否为空
     * @param message
     * @return
     */
    public static boolean isEmpty(final Object message){
        return (message == null);
    }

    /**
     * 判断对象数组是否为空
     * @param message
     * @return
     */
    public static boolean isEmpty(final Object[] message){
        return (message == null) || (message.length <= 0);
    }

    /**
     * 判断Collection对象是否为空
     * @param message
     * @return
     */
    public static boolean isEmpty(final Collection<?> message){
        return (message == null) || (message.size() <= 0);
    }

    /**
     * 判断Set对象是否为空
     * @param message
     * @return
     */
    public static boolean isEmpty(final Set<?> message){
        return (message == null) || (message.size() <= 0);
    }

    /**
     * 判断Map对象是否为空
     * @param message
     * @return
     */
    public static boolean isEmpty(final Map<?, ?> message){
        return (message == null) || (message.size() <= 0);
    }
}
