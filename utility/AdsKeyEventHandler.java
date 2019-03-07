package com.iptv.hn.utility;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;

import com.iptv.hn.WebViewActivity;
import com.iptv.hn.entity.AdsBean;
import com.iptv.hn.entity.DeviceInfoBean;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/2/24.
 */

public class AdsKeyEventHandler {
    private static final int KeyEventOk = 9, KeyEventExit = 0;

//    public static void onKeyEvent(Context context, AdsBean adsBean, int keyCode) {
//        if (keyCode == KeyEventOk) {
//            onKeyOk(context, adsBean);
//        } else if (keyCode == KeyEventExit) {
//
//        }
//    }

    public static void onKeyOk(Context context, AdsBean adsBean) {
        Log.d("keyOk", "onKeyOk: "+adsBean.getSpecial_type());
        if (adsBean.getSpecial_type() == AdsBean.ACTION_WEBVIEW) {
            String url = adsBean.getSpecial_url();
            int buzId = adsBean.getBusi_id();
//            openWebView(context, url, adsBean.getBusi_id()+"",adsBean);
//            showWeb(adsBean);
        } else if (adsBean.getSpecial_type() == AdsBean.ACTION_APP) {
            String packageName = adsBean.getSpecial_url();
            if( isAppInstalled(context, packageName) ) {

                invokeAppByPackageName(context,packageName, adsBean);
                //invokeAppByClassName
            }else {

                if (adsBean.getDown_type() == AdsBean.DOWNLOAD_FROM_STORE) {
                    //从商店下载
                    openAppStore(context, packageName);
                } else {
                    //从网站下载
                    downloadFromUrl(context, adsBean.getDown_url());
                }
            }
        }else if (adsBean.getSpecial_type() == AdsBean.START_AMNGUO){
           // Log.e("mangoJson",adsBean.getSpecial_type()+"--->adsBean.getSpecial_type()");
            //打开芒果
            Intent intent = new Intent();
            intent.setAction(adsBean.getSpecial_url());
            intent.putExtra("jsonData", adsBean.getJsonData());
           // Log.e("mangoJson",adsBean.getJsonData()+"--->adsBean.getJsonData()");
            //发送广播
            context.sendBroadcast(intent);
        }
    }

    private static void downloadFromUrl(final Context context, String url) {
        Callback callback = new Callback() {
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
                installAppByManager(context, pathOnSdcard);
//                installAppBackground(context, pathOnSdcard);
            }

            @Override
            public void onFail(Object... o) {

            }
        };

        DownloadManager.dl(callback, url);
    }

    private static void installAppBackground(Context context, String apkPath) {
        String shell = "pm install -r " + apkPath;
        try {
            Process process = Runtime.getRuntime().exec(shell);
        } catch (IOException e) {

            Utils.showToast(context, "静默安装失败");

            installAppByManager(context, apkPath);

            e.printStackTrace();
        }
    }

    public static void installAppByManager(Context context, String apkPath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(apkPath)),
                "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    private static void openAppStore(Context context, String packageName) {
        try {
            String str = "market://details?id=" + packageName;
            Intent localIntent = new Intent(Intent.ACTION_VIEW);
            localIntent.setData(Uri.parse(str));
            context.startActivity(localIntent);
        } catch (Exception e) {
            // 打开应用商店失败 可能是没有手机没有安装应用市场
            e.printStackTrace();
            // 调用系统浏览器进入商城
//            String url = "http://app.mi.com/detail/163525?ref=search";
//            openLinkBySystem(url);
        }
    }

    public static void invokeAppByClassName() {

    }

    private static void invokeAppByPackageName(Context context,String packageName, AdsBean adsBean) {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager mPackageManager = context.getPackageManager();
        List<ResolveInfo> mAllApps = mPackageManager.queryIntentActivities(mainIntent, 0);
        //按包名排序
        Collections.sort(mAllApps, new ResolveInfo.DisplayNameComparator(mPackageManager));

        for(ResolveInfo res : mAllApps){
            //该应用的包名和主Activity
            String pkg = res.activityInfo.packageName;
            String cls = res.activityInfo.name;

            if(pkg.contains(packageName)){
                ComponentName componet = new ComponentName(pkg, cls);
                Intent intent = new Intent();
                intent.setComponent(componet);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("jsonData", adsBean.getJsonData());
                context.startActivity(intent);
            }
        }
    }

    private static boolean isAppInstalled(Context context, String packageName) {

        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
        }catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
            e.printStackTrace();
        }

        return packageInfo!=null;
    }

    public static void openWebView(Context context, String url, String busi_id,AdsBean adsBean) {

        String userToken = Utils.getTvUserToken(context);
        DeviceInfoBean deviceData = com.iptv.hn.entity.Utils.getDeviceData(context, new DeviceInfoBean());
        if (url.contains("?")) {
            url = url + "&userId=" + Utils.getTvUserId(context) + "&userToken=" + userToken+
                    "&mac="+deviceData.getMac_addr()+"&ip="+deviceData.getIp_addr()+
                    "&payMod="+adsBean.getPay_type();
        } else {
            url = url + "?userId=" + Utils.getTvUserId(context)
                    + "&userToken=" + userToken+"&mac="+deviceData.getMac_addr()
                    +"&ip="+deviceData.getIp_addr()+"&payMod="+adsBean.getPay_type();
        }
        Log.e("test",url);
        Intent intent = new Intent();
        intent.putExtra("url", url);
        intent.putExtra("busi_id", busi_id);
        intent.setClass(context, WebViewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

}
