package com.qg.fangrui.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by FunriLy on 2017/6/10.
 * From small beginnings comes great things.
 */
public class DateUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DateUtil.class);
    private static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";

    /**
     * 获得当前时间的字符串格式
     * @return
     */
    public static String getCurrentDateTime(){
        SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_DATE_PATTERN);
        return sdf.format(System.currentTimeMillis());
    }

    /**
     * 比较两个时间
     * 如果A>B，返回1；如果A=B，返回0；如果A<B，返回-1；
     * @param time1
     * @param time2
     * @return
     * @throws ParseException
     */
    public static int compareTime(String time1,String time2) throws ParseException{
        SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_DATE_PATTERN);
        Date date1 = sdf.parse(time1);
        Date date2 = sdf.parse(time2);
        long result = date1.getTime() - date2.getTime();
        if(result > 0){
            return 1;
        }else if(result==0){
            return 0;
        }else{
            return -1;
        }
    }

    /**
     * 把字符串日期转换成日期对象
     * @param dateString
     * @param dateFromat
     * @return
     */
    public static Date convertStringToDate(String dateString, String dateFromat){
        if (!IsEmptyUtil.isEmpty(dateString) && !IsEmptyUtil.isEmpty(dateFromat)){
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(dateFromat);
                return sdf.parse(dateString);
            } catch (Exception e){
                LOGGER.warn("将字符串转化为日期出错！参数为 " + dateString + "-" + dateFromat, e);
            }
        }
        return null;
    }

}
