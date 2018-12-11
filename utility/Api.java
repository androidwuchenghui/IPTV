package com.iptv.hn.utility;

import android.content.Context;
import android.util.Log;

import com.iptv.hn.Contants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/3/12.
 */

public class Api {

    public static final String Key_Apk_Get = "apkgetUrl";
    public static final String Key_Behavior_Post = "behaviorUrl";
    public static final String Key_File_Server = "fileUrl";
    public static final String Key_Msg_Interval = "msgShowIntervalTime";

    public static void getApkVersion(final Context context, String user) {
        PfUtil pfUtil = PfUtil.getInstance();
        pfUtil.init(context);

        String url = pfUtil.getString("apkgetUrl", "");
        Log.i("iptv", "getApkVersion = " + url);

        //http://120.76.52.66:8080/newVersion/getVersion/getNewsVersion2.htm?apkName=test1&accounts=admin
        Rest restApi = new Rest(url);
//        restApi.addParam("apkName", "com.iptv.maopao");
        restApi.addParam("apkName", "tv");
        restApi.addParam("accounts", user);
        restApi.get(new HttpCallback() {
            @Override
            public void onSuccess(JSONObject rawJsonObj, int state, String msg) throws JSONException {

                String apkUrl = "";
                String version = "";

                if (!rawJsonObj.has("data") && rawJsonObj.getJSONObject("data")!=null && !rawJsonObj.getJSONObject("data").toString().equals("null")) {
//                    apkUrl = "http://120.76.52.66:8080/newVersion/downLoadApk/tv-releaseV2.0.apk";
//                    version = "2.0";
                    return;
                }
                else
                {
                    JSONObject data = rawJsonObj.getJSONObject("data");
                    apkUrl = data.getString("loadUrl");
                    version = data.getString("version");
                    //{"msg":"查询成功","code":"0","data":{"account":"admin","apkName":"tv","loadUrl":"http://120.76.52.66:8080/newVersion/downLoadApk/tv-releaseV2.0.apk","version":"2.0"}}

                }

                String localVersion = ManifestMetaDataUtil.getVersionName(context);
                if (localVersion.compareTo(version) < 0) {
                    DownloadManager.dl(new Callback() {
                        @Override
                        public void onStart(Object... o) {

                        }

                        @Override
                        public void onProgress(Object... o) {

                        }

                        @Override
                        public void onFinish(Object... o) {
                            final String pathOnSdcard = o[0].toString();
                            Log.i("iptv", "download file to local = " + pathOnSdcard);
                            AdsKeyEventHandler.installAppByManager(context, pathOnSdcard);
                        }

                        @Override
                        public void onFail(Object... o) {

                        }
                    }, apkUrl);
                }
            }

            @Override
            public void onFailure(JSONObject rawJsonObj, int state, String msg) {

            }

            @Override
            public void onError() {

            }
        });
    }

    /**
     * 上传用户行为数据
     * @param readType 行为类型：readType=0  推送到达的时候；readType=10，用户对弹窗 按了确定按钮
     */
    public static void postUserBehaviors(Context context, String msgId, String account,
                                         String ip, String readType, int bizId) {
        //http://120.76.52.66:8080/newVersion/BehaviorDatas/save.htm?msgId=20170310_11&accountName=admin&timeStamp=20&ipAddress=192.16.8.1.127
//        Rest restApi = new Rest("http://120.76.52.66:8080/", "newVersion/BehaviorDatas/save.htm");

        PfUtil pfUtil = PfUtil.getInstance();
        pfUtil.init(context);

        String url = pfUtil.getString("behaviorUrl", "");
        Log.i("iptv", "postUserBehaviors = " + url);

        Rest restApi = new Rest(url);
        restApi.addParam("msgId", msgId);
        restApi.addParam("accountName", account);
        restApi.addParam("timeStamp", System.currentTimeMillis()/1000);
        restApi.addParam("ipAddress", ip);
        restApi.addParam("readType", readType);
        restApi.addParam("ext1", bizId);

        String mac = MACUtils.getLocalMacAddressFromBusybox();
        restApi.addParam("ext2", mac==null ? "" : mac);

        restApi.get(new HttpCallback() {
            @Override
            public void onSuccess(JSONObject rawJsonObj, int state, String msg) throws JSONException {
                Log.i("iptv", "postUserBehaviors success");
            }

            @Override
            public void onFailure(JSONObject rawJsonObj, int state, String msg) {

            }

            @Override
            public void onError() {

            }
        });
    }

    /**
     * 获取应用配置信息
     */
    public static void getConfigure(final Context context, final HttpCallback callback) {
        //http://120.76.52.66:8080/newVersion/ConfigitemsControl/getConfigitems.htm
        Rest restApi = new Rest(Contants.IPTV_REST_API, "newVersion/ConfigitemsControl/getConfigitems.htm?&accountName=" + Utils.getTvUserId(context));
        restApi.get(new HttpCallback() {
            @Override
            public void onSuccess(JSONObject rawJsonObj, int state, String msg) throws JSONException {
                JSONArray array = rawJsonObj.getJSONArray("data");

                PfUtil pfUtil = PfUtil.getInstance();
                pfUtil.init(context);

                for(int index=0; index<array.length(); index++) {
                    JSONObject item = array.getJSONObject(index);
                    String name = item.getString("nameTxt");
                    String value = item.getString("urlAddress");

                    Log.i("iptv", name + " = " + value);

                    if (name.equalsIgnoreCase("udpServer")) {
                        Log.i("iptv", "get tcp server = " + value);

                        if (value.contains(":")) {
                            String[] addr = value.split(":");
                            String ip = addr[0];
                            String port = addr[1];

                            Contants.IPTV_UDP_IP = ip;
                            Contants.IPTV_UDP_PORT = Integer.parseInt(port);
                            callback.onSuccess(null, 0, "");
                        }



                    }

                    if (name.equals("apkgetUrl")) {
                        String apkGet = value.substring(0, value.indexOf("?"));
                        if (apkGet.contains("?")) {
                            int indexQ = apkGet.indexOf('?');
                            apkGet = apkGet.substring(0, indexQ);
                        }

                        pfUtil.putString(name, apkGet);
                        getApkVersion(context, Utils.getTvUserId(context));

                    }
                    if (name.equals("behaviorUrl")) {
                        String behaviorUrl = value.substring(0, value.indexOf("?"));
                        if (behaviorUrl.contains("?")) {
                            int indexQ = behaviorUrl.indexOf('?');
                            behaviorUrl = behaviorUrl.substring(0, indexQ);
                        }

                        pfUtil.putString(name, behaviorUrl);
                    }
                    if (name.equals("fileUrl")) {
                        pfUtil.putString(name, value);
                    }
                    if (name.equals("msgShowIntervalTime")) {
                        //默认消息间隔时间
                        try {
                            int duration = Integer.parseInt(value);
//                            Contants.DURATION_TOAST_MESSAGE = duration * 1000;
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(JSONObject rawJsonObj, int state, String msg) {
                 Log.i("iptv", msg);
            }

            @Override
            public void onError() {
                Log.i("iptv", "onError");
            }
        });
    }
}
