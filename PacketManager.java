package com.iptv.hn;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/2/21.
 */

public class PacketManager {

    /**
     * JSON Data when APP initialed.
     * @param user
     * @param ip
     * @return
     */
    public static String getAppInitData(String user, String ip) {

        JSONObject json = new JSONObject();
        try {
            json.put("time", System.currentTimeMillis()/1000);
            json.put("username", user);
            json.put("ip", ip);
            json.put("dtype", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json.toString();
    }

    /**
     * Response Data Packet when receive thd message pushed.
     * @return
     */
    public static String getResponseUdpData(long msgId, String user, String ip) {
        JSONObject json = new JSONObject();
        try {

            JSONArray array = new JSONArray();
            array.put(msgId);

            json.put("list_msg_id", array);
            json.put("time", System.currentTimeMillis()/1000);
            json.put("username", user);
            json.put("ip", ip);
            json.put("dtype", 2);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json.toString();
    }

    public static String getPingData() {
        JSONObject json = new JSONObject();
        try {
            json.put("dtype", 3);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json.toString();
    }
}
