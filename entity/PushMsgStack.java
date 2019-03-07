package com.iptv.hn.entity;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.iptv.hn.utility.JsonUtil;
import com.iptv.hn.utility.Md5Util;
import com.iptv.hn.utility.PfUtil;
import com.iptv.hn.utility.TimeUtils;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/26.
 */

public class PushMsgStack {
    /**
     * 将推送过来的消息序列化到本地
     * @param adsBean
     * @throws Exception
     */
    public static void putMessage(Context context, AdsBean adsBean) throws Exception {

        JSONObject json = new JSONObject();
        json.put("msg_id", adsBean.getMsg_id());
        json.put("model_id", adsBean.getModel_id());
        json.put("show_time", adsBean.getShow_time());
        json.put("time_text",adsBean.getTime_text());
        json.put("time_color",adsBean.getTime_color());
        json.put("video_url",adsBean.getVideo_url());
        json.put("file_type", adsBean.getFile_type());
        json.put("file_url", adsBean.getFile_url());
        json.put("live_swift", adsBean.getLive_swift());
        json.put("is_back", adsBean.getIs_back());
        json.put("float_position", adsBean.getFloat_position());
        json.put("special_type", adsBean.getSpecial_type());
        json.put("special_url", adsBean.getSpecial_url());
        json.put("down_type", adsBean.getDown_type());
        json.put("down_url", adsBean.getDown_url());
        json.put("priority_level", adsBean.getPriority_level());
        json.put("pay_type", adsBean.getPay_type());
        json.put("exce_starttime", adsBean.getExce_starttime());
        json.put("exce_endtime", adsBean.getExce_endtime());

        json.put("position", adsBean.getPosition());
        json.put("busi_id", adsBean.getBusi_id());

        json.put("jsonData", adsBean.getJsonData());

        PfUtil pfUtil = PfUtil.getInstance();
        pfUtil.init(context);

        pfUtil.putString("push" + adsBean.getMsg_id(), json.toString());

        Log.i("iptv", "insert message = " + adsBean.getMsg_id());
    }

    public static int getMessageCount(Context context) {
        PfUtil pfUtil = PfUtil.getInstance();
        pfUtil.init(context);

        List<AdsBean> adsLists = new ArrayList<AdsBean>();

        Map<String,String> items = (Map<String,String>) pfUtil.getAll();
        return items.size();
    }

    public static void removeMessage(Context context, AdsBean adsBean) {
        PfUtil pfUtil = PfUtil.getInstance();
        pfUtil.init(context);

        List<AdsBean> adsLists = new ArrayList<AdsBean>();

        Map<String,String> items = (Map<String,String>) pfUtil.getAll();
        Iterator keySets = items.keySet().iterator();
        while(keySets.hasNext()) {
            String messageId = keySets.next().toString();
            if (messageId.equals("push" +adsBean.getMsg_id())) {
                pfUtil.remove(messageId);

                Log.i("iptv", "remove message = " + adsBean.getMsg_id());
            }
        }
    }

    public static void deleteResources(AdsBean adsBean) {
        String fileName = Md5Util.MD5Encode(adsBean.getFile_url());
        String SDPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File file = new File(SDPath, fileName);
        if (file.exists()) {
            file.delete();
            Log.i("iptv", "delete message file " + file.getAbsolutePath());
        }
    }

    /**
     * 唤起定时任务消息
     */
    public static AdsBean notifyAlarmMessage(Context context, long duration) {
        PfUtil pfUtil = PfUtil.getInstance();
        pfUtil.init(context);

        List<AdsBean> adsLists = new ArrayList<AdsBean>();

        Map<String,String> items = (Map<String,String>) pfUtil.getAll();
        Iterator keySets = items.keySet().iterator();
        while(keySets.hasNext()) {
            String messageId = keySets.next().toString();

            Log.i("iptv", "find message = " + messageId);

            if (messageId.startsWith("push")) {
                String jsonString = items.get(messageId);
                AdsBean adsBean = JsonUtil.jsonStringToObject(jsonString, AdsBean.class);

                long time = TimeUtils.getNowSeconds();
                long execStartTime = adsBean.getExce_starttime();
                long startTime = adsBean.getExce_starttime();

                Log.i("iptv", "currenttime = " + time);
                Log.i("iptv", "startTime = " + startTime);
                Log.i("iptv", "execStartTime = " + execStartTime);

                if (execStartTime!=0 && execStartTime > time && (execStartTime-time) < duration) {
                    //未过期消息，并且快要执行了，启动定时任务
                    Log.i("iptv", "return message = " + messageId);
                    return adsBean;
                }

            }
        }

        return null;
    }

    public static AdsBean getTopMessage(Context context) {

        PfUtil pfUtil = PfUtil.getInstance();
        pfUtil.init(context);

        List<AdsBean> adsLists = new ArrayList<AdsBean>();

        Map<String,String> items = (Map<String,String>) pfUtil.getAll();
        Iterator keySets = items.keySet().iterator();
        while(keySets.hasNext()) {
            String messageId = keySets.next().toString();
            if (messageId.startsWith("push")) {
                String jsonString = items.get(messageId);
                AdsBean adsBean = JsonUtil.jsonStringToObject(jsonString, AdsBean.class);

                long time = TimeUtils.getNowSeconds();
                long execEndTime = adsBean.getExce_endtime();
                long startTime = adsBean.getExce_starttime();
                if (execEndTime!=0 && execEndTime < time) {
                    //过期消息，丢弃
                    removeMessage(context, adsBean);
                    continue;
                }

                if (startTime!=0 && startTime < time) {
                    //过期消息，丢弃
                    removeMessage(context, adsBean);
                    continue;
                }

                if (startTime==0) {
                    //没有指定时间的才会被选择, 指定时间的在另外一个方法返回
                    adsLists.add(adsBean);
                }


            }
        }

        if (adsLists.isEmpty()) {

            return null;
        }

        Collections.sort(adsLists, new Comparator<AdsBean>(){
            @Override
            public int compare(AdsBean b1, AdsBean b2) {
                return b2.getPriority_level() - b1.getPriority_level();
            }

        });
        return adsLists.get(0);
    }

}
