package com.iptv.hn;

/**
 * Created by Administrator on 2017/2/23.
 */
public class Contants {

    public static final String IPTV_REST_API = "http://124.232.135.241:8080/";

    public static final String Rest_api_v3 =  "http://124.232.135.241:8080/";

    //  ---  上线正式地址
    public static final String Rest_api_v2 =  "http://124.232.135.246:9090/";
    //  -- 测试接口
//    public static final String Rest_api_v2 =  "http://124.232.135.241:8087/";
    //  测试接口
//    public static final String Rest_api_v2_test =  "http://124.232.135.241:8087/";

    // 正式接口
    public static final String Rest_api_v2_test =  "http://124.232.135.246:9090/";

    /**
     * UDP server ip
     * */
    // public static final String IPTV_UDP_IP = "120.76.52.66"; //外网

     public static String IPTV_UDP_IP = ""; //内网124.232.135.241

    /**
     * UDP server port
     */
    public static int IPTV_UDP_PORT = 0; //3088

    public static int DURATION_TOAST_MESSAGE = 1000 * 30; //默认消息弹窗间隔时间 space_time

    public static int DURATION_PING = 3600 * 1000; //默认60秒一次心跳

    public static long LAST_PING_TIMESTAMP = System.currentTimeMillis();

    public static boolean isInMangoLiving = false;

    public static int APP_INIT_TIME = 1000 * 10; //客户端启动后，向服务器请求数据的时间间隔

    public static int SERVICE_GET = 0; //客户端启动后，向服务器请求数据的时间间隔

}

