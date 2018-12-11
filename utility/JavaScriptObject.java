package com.iptv.hn.utility;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.Toast;
import  com.iptv.hn.entity.Utils;
import com.google.gson.Gson;
import com.iptv.hn.entity.AppInfoBean;
import com.iptv.hn.entity.DeviceInfoBean;
import com.iptv.hn.entity.StartAppsBean;

import org.json.JSONObject;


public class JavaScriptObject{
    private static final String TAG = "JavaScriptObject";
    private  Activity activity;
    private WebView webview;
    public JavaScriptObject(Activity activity, WebView webview) {
        this.activity = activity;
        this.webview = webview;
    }
        @JavascriptInterface
        public void sendMessageToJAVA(String json) {
            Log.i("tag", "sendMessageToJAVA: json:"+json);
            if(TextUtils.isEmpty(json)||json.equals("undefined")){
                Log.i(TAG, "setAppData: get jsonData is null ro  is undefined");
                return ;
            }
            try {
                if (!TextUtils.isEmpty(json)){
                    JSONObject jsonObject = new JSONObject(json);
                    if(jsonObject.has("startApp")){
                        StartAppsBean.StartAppBean startApp = GetStartAppBean(json);
                        String s = "";
                        if (startApp.getJsonData()!=null){
                            s = new Gson().toJson(startApp.getJsonData());
                        }
                        Log.i(TAG, "getAppIntent: jsonData:"+s);
                        Utils.StartAPP(startApp.getPackageName(),startApp.getClassName(),s,activity);
                    }else if(jsonObject.has("record_param")){
                        RecordParam = jsonObject.getString("record_param");
                        Log.i(TAG, "sendMessageToJAVA: 记录当前浏览器传参:"+ RecordParam);
                    }else if(jsonObject.has("toast")){
                        String toastData = jsonObject.getString("toast");
                        if (!TextUtils.isEmpty(toastData)){
                            Toast.makeText(activity, toastData, Toast.LENGTH_SHORT).show();
                        }else{
                            Log.i(TAG, "sendMessageToJAVA: 网页获取的参数为空");
                        }
                    }
                }
            }catch (Exception e){
                Log.i(TAG, "sendMessageToJAVA: 浏览器交互出现异常");
                e.printStackTrace();
            }

        }
    @JavascriptInterface
    public void sendMessageToJAVA(String json,String key,String value){
        if(TextUtils.isEmpty(json)||json.equals("undefined")){
            Log.i(TAG, "sendMessageToJAVA json key value: get jsonData is null ro  is undefined");
            return ;
        }
        Log.i(TAG, "sendMessageToJAVA2: json:"+json);
        try {
            if (!TextUtils.isEmpty(json)){
                JSONObject jsonObject = new JSONObject(json);
                if(jsonObject.has("startApp")){
                    StartAppsBean.StartAppBean startApp = GetStartAppBean(json);
                    Utils.StartAPP2(startApp.getPackageName(),startApp.getClassName(),key,value,activity);
                }
            }
        }catch (Exception e){
            Log.i(TAG, "sendMessageToJAVA: 浏览器交互出现异常");
            e.printStackTrace();
        }
    }
        @JavascriptInterface
        public void sendMessageToJAVA2(String json,String[] key,String[] value){
            if(TextUtils.isEmpty(json)||json.equals("undefined")){
                Log.i(TAG, "sendMessageToJAVA json key value: get jsonData is null ro  is undefined");
                return ;
            }
            Log.i(TAG, "sendMessageToJAVA2: json:"+json);
            try {
                if (!TextUtils.isEmpty(json)){
                    JSONObject jsonObject = new JSONObject(json);
                    if(jsonObject.has("startApp")){
                        StartAppsBean.StartAppBean startApp = GetStartAppBean(json);
                        Utils.StartAPP2(startApp.getPackageName(),startApp.getClassName(),key,value,activity);
                    }
                }
            }catch (Exception e){
                Log.i(TAG, "sendMessageToJAVA: 浏览器交互出现异常");
                e.printStackTrace();
            }
        }

        private StartAppsBean.StartAppBean GetStartAppBean(String json) {
            StartAppsBean startAppBean = new Gson().fromJson(json, StartAppsBean.class);
            return startAppBean.getStartApp();
        }

        private String RecordParam = "";

        @JavascriptInterface
        public void getRecordParam(){
            Log.i(TAG, "getRecordParam: 浏览器获取记录的传参 RecordParam"+RecordParam);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                webview.evaluateJavascript("javascript:getRecordParam(" + RecordParam + ")", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        //此处为 js 返回的结果
                    }
                });
            }else{
                webview.loadUrl("javascript:getRecordParam(" + RecordParam + ")");
            }
        }
        @JavascriptInterface
        public void getAppData(String packageName){
            Log.i(TAG, "getAppData: packageName:"+packageName);
            if(TextUtils.isEmpty(packageName)||packageName.equals("undefined")){
                Log.i(TAG, "getAppData: get jsonData is null ro  is undefined");
                return ;
            }
            try {
                PackageManager packageManager = activity.getPackageManager();
                final PackageInfo packageInfo = packageManager.getPackageInfo(packageName,0);
                if (packageInfo!=null){
                    String  info = packageInfo.toString();
                    Log.i(TAG, "getMangGuoAppData: info:"+info);

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AppInfoBean appInfoBean = new AppInfoBean();
                            AppInfoBean.JsonDataBean  jsonDataBean = new AppInfoBean.JsonDataBean();

                            String  versionName = packageInfo.versionName;
                            int versionCode = packageInfo.versionCode;
                            if (!TextUtils.isEmpty(versionName)){
                                jsonDataBean .setVersionName(packageInfo.versionName);
                            }
                           if (versionCode>0){
                               jsonDataBean.setVersionCode(packageInfo.versionCode);
                           }
                            jsonDataBean.setPackageName(packageInfo.packageName);
                            appInfoBean.setJsonData(jsonDataBean);
                            setAppData(new Gson().toJson(appInfoBean));
                        }
                    });
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        public void setAppData(String jsonData){
            Log.i(TAG, "setAppData: jsonData:"+jsonData);
            if(TextUtils.isEmpty(jsonData)||jsonData.equals("undefined")){
                Log.i(TAG, "setAppData: get jsonData is null ro  is undefined");
                return ;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                webview.evaluateJavascript("javascript:setAppData(" + jsonData + ")", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        //此处为 js 返回的结果
                    }
                });
            }else{
                webview.loadUrl("javascript:setAppData(" + jsonData + ")");
            }
        }
        @JavascriptInterface
        public void LoadUrl(final String url){
            Log.i(TAG, "LoadUrl: url:"+url);
            if (TextUtils.isEmpty(url)||url.equals("undefined")){
                Log.i(TAG, "LoadUrl: get url is null");
            }else{
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        webview.loadUrl(url);
                    }
                });

            }
        }
        @JavascriptInterface
      public void finishActivity(){
            activity.finish();
      }
    @JavascriptInterface
    public void getDeviceInfo(){
        Log.i(TAG, "getDeviceInfo: ");
        try{
            DeviceInfoBean deviceInfoBean = new DeviceInfoBean();
            deviceInfoBean = Utils.getDeviceData(activity,deviceInfoBean);
            final DeviceInfoBean finalDeviceInfoBean = deviceInfoBean;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sendDeviceInfo(new Gson().toJson(finalDeviceInfoBean));
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void sendDeviceInfo(String devicesInfo){
        Log.i(TAG, "sendDeviceInfo: devicesInfo:"+devicesInfo);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webview.evaluateJavascript("javascript:DeviceInfo(" + devicesInfo + ")", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    //此处为 js 返回的结果
                }
            });
        }else{
            webview.loadUrl("javascript:DeviceInfo(" + devicesInfo + ")");
        }
    }
    }