package com.iptv.hn.utility;

import java.util.Calendar;

/**
 * Created by Administrator on 2017/12/27.
 */

public class TimeUtils {
    public static long getNowSeconds() {
        Calendar calendar = Calendar.getInstance();

        long seconds = calendar.get(Calendar.HOUR)*60*60 + calendar.get(Calendar.MINUTE)*60 + calendar.get(Calendar.SECOND);
//        System.out.println("seconds = " + seconds);
        return seconds;
    }
}
