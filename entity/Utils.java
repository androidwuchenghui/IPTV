package com.iptv.hn.entity;


import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.iptv.hn.Contants;
import com.iptv.hn.service.BootloaderService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;


/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 陈庚
 * Description:
 * Date: 2016/10/13
 * Time: 9:51
 */

public class Utils {
    private static final String TAG = "Utils";
    /**
     * @param context
     * @return versionCode 版本号
     */
    public static int getVersionCode(Context context) {
        int versionCode = 0;
        try {
            String pkName = context.getPackageName();
            versionCode = context.getPackageManager().getPackageInfo(pkName, 0).versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return versionCode;
    }
    /**
     * @param context
     * @return versionCode 版本号
     */
    public static  String getVersionName(Context context) {
        String versionCode = "";
        try {
            String pkName = context.getPackageName();
            versionCode = context.getPackageManager().getPackageInfo(pkName, 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return versionCode;
    }


    public  static String Authenticator(String str_random, String token, String userId, String stbId, String ip, String mac, String reserved, String password) throws Exception {
        Log.i("tv_launcher", "get Authenticator:");
        String str1 = str_random+"$";
        String str2 = str1+token+"$";
        String str3 = str2+userId+"$";
        String str4 = str3+stbId+"$";
        String str5 = str4+ip+"$";
        String str6 = str5+mac+"$";
        String str7 = str6+reserved+"$";
        String string = str7 + "CTC";//加密的内容
        Log.i("tv_launcher", "Authenticator:密码 "+password);
        Log.i("tv_launcher", "Authenticator:加密内容 "+string);
        byte[] arrayOfByte3 = new byte[24];
        byte[] arrayOfByte4 = password.getBytes();
        for (int m = 0; ; m++) {
            if (m < 24) {
                if (m < arrayOfByte4.length)
                    arrayOfByte3[m] = arrayOfByte4[m];
                else
                    arrayOfByte3[m] = 48;//48表示ASCII 字符“0”
            } else {

                String str8 = DesEncrypt(string, arrayOfByte3).toUpperCase();
                return str8;
            }
        }


    }

    private  static String DesEncrypt(String paramString, byte[] paramArrayOfByte)
            throws Exception {
        Cipher localCipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
        DESedeKeySpec localDESedeKeySpec = new DESedeKeySpec(paramArrayOfByte);
        localCipher.init(Cipher.ENCRYPT_MODE, SecretKeyFactory.getInstance("DESede").generateSecret(localDESedeKeySpec));
        String str = "";
        byte[] arrayOfByte = localCipher.doFinal(paramString.getBytes("ASCII"));
        str =  bytesToHexString(arrayOfByte);

        return str;
    }

    private static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
    public  static String getOperate(String result, String operate) {
        operate ="'"+operate+"','";
        try {
            String[] split = result.split(operate);
            String oper = split[1].substring(0, split[1].indexOf("');"));
            return oper;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String getMacAddr(){
        String mac = "00:00:00:00:00:00";
        String LocalMac= getLocalMacAddressFromBusybox();
        return  isMacAddr(LocalMac)?LocalMac:mac;
    }
    /**
     * 推荐使用 获取MAC正确 根据busybox获取本地Mac
     *
     * @return
     */
    public static String getLocalMacAddressFromBusybox() {
        String result = "";
        String Mac = "";
        result = callCmd("busybox ifconfig", "HWaddr");

        // 如果返回的result == null，则说明网络不可取
        if (result == null) {
            return "网络出错，请检查网络";
        }

        // 对该行数据进行解析
        // 例如：eth0 Link encap:Ethernet HWaddr 00:16:E8:3E:DF:67
        if (result.length() > 0 && result.contains("HWaddr") == true) {
            Mac = result.substring(result.indexOf("HWaddr") + 6, result.length() - 1);
            Log.i("tv_launcher", "Mac:" + Mac + " Mac.length: " + Mac.length());
//
			/*
             * if(Mac.length()>1){ Mac = Mac.replaceAll(" ", ""); result = "";
			 * String[] tmp = Mac.split(":"); for(int i = 0;i<tmp.length;++i){
			 * result +=tmp[i]; } }
			 */
            result = Mac;
            Log.i("tv_launcher", result + " result.length: " + result.length());
        }
        return result.trim();
    }
    private static String callCmd(String cmd, String filter) {
        String result = "";
        String line = "";
        try {
            Process proc = Runtime.getRuntime().exec(cmd);
            InputStreamReader is = new InputStreamReader(proc.getInputStream());
            BufferedReader br = new BufferedReader(is);

            // 执行命令cmd，只取结果中含有filter的这一行
            while ((line = br.readLine()) != null && line.contains(filter) == false) {
                // result += line;
                Log.i("tv_launcher", "line: " + line);
            }

            result = line;
            Log.i("tv_launcher", "result: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String getSTB_ID(){
        String value2 = "00000000000000000000000000000000";
        try {
            Class<?> c = null;
            c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value2 = (String)(get.invoke(c, "ro.product.stb.stbid", "unknown" ));
            Log.i("tv_launcher", "getSTB_ID:"+value2);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("tv_launcher","getStbId failed");
            return value2;
        }
        return value2;
    }

    /**
     * 判断MAC地址是否正确
     * @param mac
     * @return
     */
    public static boolean isMacAddr(@Nullable String mac){
        if (TextUtils.isEmpty(mac)){
            return false;
        }
        return mac.matches("([A-Fa-f0-9]{2}:){5}[A-Fa-f0-9]{2}");
    }
    /**
     * 芒果 内部 获取 10段开头的IP地址
     *
     * @return
     */
    public static String getIpAddr() {

        String str = getIpNetcfg("ppp[0-9]+");
        if ((TextUtils.isEmpty(str)) || ("0.0.0.0".equals(str))) {
            str = getIpNetcfg("eth[0-9]+");
        }
        return str;
    }

    private static String getIpNetcfg(String paramString) {
        try {
            Process process = Runtime.getRuntime().exec("netcfg");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            process.waitFor();
            Pattern pattern = Pattern.compile("^([a-z0-9]+)\\s+(UP|DOWN)\\s+([0-9./]+)\\s+.+\\s+([0-9a-f:]+)$", Pattern.CASE_INSENSITIVE);
            String str5;
            Matcher matcher = null;
            String ready ;
            while ((ready = bufferedReader.readLine()) != null) {
//                Log.i("tv_launcher", "getIpNetcfg: ready:"+ready);
                matcher = pattern.matcher(ready);
                if (matcher!=null&&matcher.matches()){
                    String str1 = matcher.group(1).toLowerCase(Locale.CHINA);
//                    String str2 = matcher.group(2);
                    String str3 = matcher.group(3);
//                    String str4 = matcher.group(4).toUpperCase(Locale.CHINA).replace(':', '-');
//                    LogUtil.i("tv_launcher", "match success:" + str1 + " " + str2 + " " + str3 + " " + str4);
                    Log.i("tv_launcher", "match success:str1:"+str1+"\t str3:" + str3);
                    if(str1.matches(paramString)){
                        str5 = str3.substring(0, str3.indexOf("/"));
                        Log.i("tv_launcher", "addr:" + str5);
                        return str5;
                    }
                }
            }
        } catch (java.io.IOException IOException) {
            Log.i("tv_launcher", "Exception: IOException.");
            IOException.printStackTrace();
        } catch (java.lang.InterruptedException InterruptedException) {
            Log.i("tv_launcher", "Exception: InterruptedException.");
            InterruptedException.printStackTrace();
        }
        return "";
    }
    public static String getAccessMethod(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                String typeName = networkInfo.getTypeName().toLowerCase();
                typeName = "pppoe2";
                if (!typeName.equals("pppon")&&!typeName.equals("lan")&&!typeName.equals("dhcp")){
                    return "pppoe";
                }
                return typeName;
            }
        } catch (Exception e) {
            e.printStackTrace();
            ;
        }
        return "";
    }


    public static  DeviceInfoBean getDeviceData(Context context,DeviceInfoBean deviceInfo) {

        Uri localUri = Uri
                .parse("content://com.hunantv.operator.mango.hndxiptv/userinfo");
        Cursor localCursor = context.getContentResolver().query(localUri, null, null,
                null, null);
        try {
            if (localCursor!=null){
                while (localCursor.moveToNext()) {
                    String name = localCursor.getString(localCursor.getColumnIndex("name"));
                    if ("user_id".equals(name)){
                      String  user_id = localCursor.getString(localCursor.getColumnIndex("value"));
                        deviceInfo.setUser_id(user_id);
                        Log.e("tv_launcher", "initDeviceData: user_id:"+user_id);
                    }
                    if("user_token".equals(name)){
                        String user_token = localCursor.getString(localCursor.getColumnIndex("value"));
                        deviceInfo.setUser_token(user_token);
                        Log.e("tv_launcher", "initDeviceData: user_token:"+user_token);
                    }
                    if ("user_group_nmb".equals(name)) {
                        String user_group_nmb = localCursor.getString(localCursor.getColumnIndex("value"));
                        deviceInfo.setGid(user_group_nmb);
                    }
                }
            }
            String mac_addr = Utils.getMacAddr();
            Log.e("tv_launcher", "initDeviceData: mac:"+mac_addr);
            deviceInfo.setMac_addr(mac_addr);
            String ip_addr = Utils.getIpAddr();
            deviceInfo.setIp_addr(ip_addr);
            Log.e("tv_launcher", "initDeviceData: ipaddr:"+ip_addr);
            String model = Build.MODEL;
            deviceInfo.setModel(model);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (localCursor!=null)localCursor.close();
        }
        return deviceInfo;
    }
    /**
     * 启动应用
     */
    public static void StartAPP(String packageName,String ClassName,String jsonData,Context context){
        Log.i(TAG, "StartAPP: packageName:"+packageName+"\t ClassName:"+ClassName+"\t jsonData"+jsonData);
        if (TextUtils.isEmpty(packageName)&&TextUtils.isEmpty(ClassName)){
            Log.e(TAG,"StartAPP: start app failed ,get appinfo error");
            Toast.makeText(context, "启动应用失败：获取应用失败", Toast.LENGTH_SHORT).show();
        }else  if (!TextUtils.isEmpty(packageName)&&TextUtils.isEmpty(ClassName)){
            Log.e(TAG, "StartAPP: get class is null,startApk");
            if (isPkgInstalled(context,packageName)){
                Log.i(TAG, "StartAPP: app is installed,startApk");
                StartAPK(packageName,jsonData,context);
            }else{
                Log.e(TAG, "StartAPP: app not is installed");
                Toast.makeText(context, "应用未安装", Toast.LENGTH_SHORT).show();
            }
        }else if (TextUtils.isEmpty(packageName)&&!TextUtils.isEmpty(ClassName)){
            Log.e(TAG, "StartAPP: get packageName is null,start locat Activity");
            StartLocatActivity(ClassName,jsonData,context);
        }else  if (!TextUtils.isEmpty(packageName)&&!TextUtils.isEmpty(ClassName)){
            if (isPkgInstalled(context,packageName)){
                Log.e(TAG, "StartAPP: app is installed,startApk");
                StartActivity(packageName,ClassName,jsonData,context);
            }else{
                Log.e(TAG, "StartAPP: app not is installed2");
                Toast.makeText(context, "应用未安装", Toast.LENGTH_SHORT).show();
            }
        }
    }



    /**
     * 根据包名启动APK
     *
     * @param packageName
     * @param context
     */
    public static void StartAPK(String packageName, String jsonData,Context context) {
        if (TextUtils.isEmpty(packageName)) {
            Log.e(TAG, "packageName is null");
            return;
        }
        PackageInfo pi;
        try {
            pi = context.getPackageManager().getPackageInfo(packageName, 0);
            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
            resolveIntent.setPackage(pi.packageName);
            PackageManager pManager = context.getPackageManager();
            List apps = pManager.queryIntentActivities(resolveIntent, 0);
            ResolveInfo ri = (ResolveInfo) apps.iterator().next();
            if (ri != null) {
                packageName = ri.activityInfo.packageName;
                String className = ri.activityInfo.name;
                Log.i(TAG, "start package:"+packageName+",start package launcher:"+ className);
                Intent intent= new Intent(Intent.ACTION_MAIN);
                ComponentName cn = new ComponentName(packageName, className);
                intent.setComponent(cn);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent_putjsonData(jsonData,intent);
                context.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void intent_putjsonData(String jsonData,Intent intent) {
        if (!TextUtils.isEmpty(jsonData)){
            intent.putExtra("jsonData",jsonData);
        }
    }

    /**
     * 判断应用是否安装
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isPkgInstalled(Context context, String packageName) {
        Log.i(TAG, "isPkgInstalled: getPackageName:"+packageName);
        if (TextUtils.isEmpty(packageName)) {
                Log.i(TAG, "isPkgInstalled: get packageName is null");
            return false;
        }
        ApplicationInfo info = null;
        try {
            info = context.getPackageManager().getApplicationInfo(packageName, 0);
            return info != null;
        } catch (Exception e) {
//            e.printStackTrace();
            return false;
        }
    }
    /**
     * 根据包名和类名启动APK
     *
     * @param packageName
     * @param ClassName
     * @param context
     */
    public static void StartActivity(String packageName,String ClassName,String jsonData, Context context) {
        if (TextUtils.isEmpty(packageName)) {
            Log.e(TAG, "packageName is null");
            Toast.makeText(context, "启动失败", Toast.LENGTH_SHORT).show();
            return ;
        }
        if (TextUtils.isEmpty(ClassName)){
            Log.e(TAG, "className is null");
            StartAPK(packageName,jsonData,context);
            return ;
        }
        Log.i(TAG, "StartActivity: get packageName;"+packageName);
        Log.i(TAG, "StartActivity: get className;"+ClassName);
        Intent intent = new Intent();
            intent.setClassName(packageName, ClassName);
//        方法一：
//        if (context.getPackageManager().resolveActivity(intent, 0) == null) {
//            // 说明系统中不存在这个activity
//        }
//        方法二：
        if(intent.resolveActivity(context.getPackageManager()) != null) {
            // 说明系统中不存在这个activity
            Log.i(TAG, "StartActivity: get Activity success");
            intent= new Intent(Intent.ACTION_MAIN);
            ComponentName cn = new ComponentName(packageName, ClassName);
            intent.setComponent(cn);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent_putjsonData(jsonData,intent);
            context.startActivity(intent);
        }else{
            Toast.makeText(context, "启动失败,获取本地页面失败", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "StartActivity: startActivity error ,get Activity failed");
        }
//        方法三：
//        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, 0);
//        if (list.size() == 0) {
//            // 说明系统中不存在这个activity
//        }
    }

    /**
     * 启动本地Activity
     * @param ClassName
     * @param context
     */
    public static void StartLocatActivity(String ClassName,String jsonData,Context context){
        if (TextUtils.isEmpty(ClassName)){
            Log.e(TAG, "className is null");
            Toast.makeText(context, "启动本地页面失败", Toast.LENGTH_SHORT).show();
            return ;
        }
        if (ClassName.contains(context.getPackageName())) {
            Intent intent = new Intent();
            intent.setClassName(context, ClassName);
            if (intent.resolveActivity(context.getPackageManager()) != null) {
               intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent_putjsonData(jsonData,intent);
                context.startActivity(intent);
            }
        }else{
            Toast.makeText(context, "启动失败，无法启动该应用", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 启动应用
     */
    public static void StartAPP2(String packageName,String ClassName,String[]  key,String[] value,Context context){
        Log.i(TAG, "StartAPP: packageName:"+packageName+"\t ClassName:"+ClassName+"\t key:"+key+"\t value:"+value);
        if (TextUtils.isEmpty(packageName)&&TextUtils.isEmpty(ClassName)){
            Log.e(TAG,"StartAPP: start app failed ,get appinfo error");
            Toast.makeText(context, "启动应用失败：获取应用失败", Toast.LENGTH_SHORT).show();
        }else  if (!TextUtils.isEmpty(packageName)&&TextUtils.isEmpty(ClassName)){
            Log.e(TAG, "StartAPP: get class is null,startApk");
            if (isPkgInstalled(context,packageName)){
                Log.i(TAG, "StartAPP: app is installed,startApk");
                StartAPK2(packageName,key,value,context);
            }else{
                Log.e(TAG, "StartAPP: app not is installed");
                Toast.makeText(context, "应用未安装", Toast.LENGTH_SHORT).show();
            }
        }else if (TextUtils.isEmpty(packageName)&&!TextUtils.isEmpty(ClassName)){
            Log.e(TAG, "StartAPP: get packageName is null,start locat Activity");
            StartLocatActivity2(ClassName,key,value,context);
        }else  if (!TextUtils.isEmpty(packageName)&&!TextUtils.isEmpty(ClassName)){
            if (isPkgInstalled(context,packageName)){
                Log.e(TAG, "StartAPP: app is installed,startApk");
                StartActivity2(packageName,ClassName,key,value,context);
            }else{
                Log.e(TAG, "StartAPP: app not is installed2");
                Toast.makeText(context, "应用未安装", Toast.LENGTH_SHORT).show();
            }
        }
    }
    /**
     * 启动应用
     */
    public static void StartAPP2(String packageName,String ClassName,String  key,String value,Context context){
        Log.i(TAG, "StartAPP: packageName:"+packageName+"\t ClassName:"+ClassName+"\t key:"+key+"\t value:"+value);
        if (TextUtils.isEmpty(packageName)&&TextUtils.isEmpty(ClassName)){
            Log.e(TAG,"StartAPP: start app failed ,get appinfo error");
            Toast.makeText(context, "启动应用失败：获取应用失败", Toast.LENGTH_SHORT).show();
        }else  if (!TextUtils.isEmpty(packageName)&&TextUtils.isEmpty(ClassName)){
            Log.e(TAG, "StartAPP: get class is null,startApk");
            if (isPkgInstalled(context,packageName)){
                Log.i(TAG, "StartAPP: app is installed,startApk");
                StartAPK2(packageName,key,value,context);
            }else{
                Log.e(TAG, "StartAPP: app not is installed");
                Toast.makeText(context, "应用未安装", Toast.LENGTH_SHORT).show();
            }
        }else if (TextUtils.isEmpty(packageName)&&!TextUtils.isEmpty(ClassName)){
            Log.e(TAG, "StartAPP: get packageName is null,start locat Activity");
            StartLocatActivity2(ClassName,key,value,context);
        }else  if (!TextUtils.isEmpty(packageName)&&!TextUtils.isEmpty(ClassName)){
            if (isPkgInstalled(context,packageName)){
                Log.e(TAG, "StartAPP: app is installed,startApk");
                StartActivity2(packageName,ClassName,key,value,context);
            }else{
                Log.e(TAG, "StartAPP: app not is installed2");
                Toast.makeText(context, "应用未安装", Toast.LENGTH_SHORT).show();
            }
        }
    }
    /**
     * 根据包名和类名启动APK
     *
     * @param packageName
     * @param ClassName
     * @param context
     */
    public static void StartActivity2(String packageName,String ClassName,String[] key,String[] value, Context context) {
        if (TextUtils.isEmpty(packageName)) {
            Log.e(TAG, "packageName is null");
            Toast.makeText(context, "启动失败", Toast.LENGTH_SHORT).show();
            return ;
        }
        if (TextUtils.isEmpty(ClassName)){
            Log.e(TAG, "className is null");
            StartAPK2(packageName,key,value,context);
            return ;
        }
        Log.i(TAG, "StartActivity: get packageName;"+packageName);
        Log.i(TAG, "StartActivity: get className;"+ClassName);
        Intent intent = new Intent();
        intent.setClassName(packageName, ClassName);
//        方法一：
//        if (context.getPackageManager().resolveActivity(intent, 0) == null) {
//            // 说明系统中不存在这个activity
//        }
//        方法二：
        if(intent.resolveActivity(context.getPackageManager()) != null) {
            // 说明系统中不存在这个activity
            Log.i(TAG, "StartActivity: get Activity success");
            intent= new Intent(Intent.ACTION_MAIN);
            ComponentName cn = new ComponentName(packageName, ClassName);
            intent.setComponent(cn);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PutAllExtra(intent,key,value);
            context.startActivity(intent);
        }else{
            Toast.makeText(context, "启动失败,获取本地页面失败", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "StartActivity: startActivity error ,get Activity failed");
        }
//        方法三：
//        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, 0);
//        if (list.size() == 0) {
//            // 说明系统中不存在这个activity
//        }
    }
    /**
     * 根据包名和类名启动APK
     *
     * @param packageName
     * @param ClassName
     * @param context
     */
    public static void StartActivity2(String packageName,String ClassName,String key,String value, Context context) {
        if (TextUtils.isEmpty(packageName)) {
            Log.e(TAG, "packageName is null");
            Toast.makeText(context, "启动失败", Toast.LENGTH_SHORT).show();
            return ;
        }
        if (TextUtils.isEmpty(ClassName)){
            Log.e(TAG, "className is null");
            StartAPK2(packageName,key,value,context);
            return ;
        }
        Log.i(TAG, "StartActivity: get packageName;"+packageName);
        Log.i(TAG, "StartActivity: get className;"+ClassName);
        Intent intent = new Intent();
        intent.setClassName(packageName, ClassName);
//        方法一：
//        if (context.getPackageManager().resolveActivity(intent, 0) == null) {
//            // 说明系统中不存在这个activity
//        }
//        方法二：
        if(intent.resolveActivity(context.getPackageManager()) != null) {
            // 说明系统中不存在这个activity
            Log.i(TAG, "StartActivity: get Activity success");
            intent= new Intent(Intent.ACTION_MAIN);
            ComponentName cn = new ComponentName(packageName, ClassName);
            intent.setComponent(cn);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PutAllExtra(intent,key,value);
            context.startActivity(intent);
        }else{
            Toast.makeText(context, "启动失败,获取本地页面失败", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "StartActivity: startActivity error ,get Activity failed");
        }
//        方法三：
//        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, 0);
//        if (list.size() == 0) {
//            // 说明系统中不存在这个activity
//        }
    }
    /**
     * 启动本地Activity
     * @param ClassName
     * @param context
     */
    public static void StartLocatActivity2(String ClassName,String[] key,String[] value,Context context){
        if (TextUtils.isEmpty(ClassName)){
            Log.e(TAG, "className is null");
            Toast.makeText(context, "启动本地页面失败", Toast.LENGTH_SHORT).show();
            return ;
        }
        if (ClassName.contains(context.getPackageName())) {
            Intent intent = new Intent();
            intent.setClassName(context, ClassName);
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PutAllExtra(intent,key,value);
                context.startActivity(intent);
            }
        }else{
            Toast.makeText(context, "启动失败，无法启动该应用", Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * 启动本地Activity
     * @param ClassName
     * @param context
     */
    public static void StartLocatActivity2(String ClassName,String key,String value,Context context){
        if (TextUtils.isEmpty(ClassName)){
            Log.e(TAG, "className is null");
            Toast.makeText(context, "启动本地页面失败", Toast.LENGTH_SHORT).show();
            return ;
        }
        if (ClassName.contains(context.getPackageName())) {
            Intent intent = new Intent();
            intent.setClassName(context, ClassName);
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PutAllExtra(intent,key,value);
                context.startActivity(intent);
            }
        }else{
            Toast.makeText(context, "启动失败，无法启动该应用", Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * 根据包名启动APK
     *
     * @param packageName
     * @param context
     */
    public static void StartAPK2(String packageName, String[] key,String[] value,Context context) {
        if (TextUtils.isEmpty(packageName)) {
            Log.e(TAG, "packageName is null");
            return;
        }
        PackageInfo pi;
        try {
            pi = context.getPackageManager().getPackageInfo(packageName, 0);
            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
            resolveIntent.setPackage(pi.packageName);
            PackageManager pManager = context.getPackageManager();
            List apps = pManager.queryIntentActivities(resolveIntent, 0);
            ResolveInfo ri = (ResolveInfo) apps.iterator().next();
            if (ri != null) {
                packageName = ri.activityInfo.packageName;
                String className = ri.activityInfo.name;
                Log.i(TAG, "start package:"+packageName+",start package launcher:"+ className);
                Intent intent= new Intent(Intent.ACTION_MAIN);
                ComponentName cn = new ComponentName(packageName, className);
                intent.setComponent(cn);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PutAllExtra(intent,key,value);
                context.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 根据包名启动APK
     *
     * @param packageName
     * @param context
     */
    public static void StartAPK2(String packageName, String key,String value,Context context) {
        if (TextUtils.isEmpty(packageName)) {
            Log.e(TAG, "packageName is null");
            return;
        }
        PackageInfo pi;
        try {
            pi = context.getPackageManager().getPackageInfo(packageName, 0);
            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
            resolveIntent.setPackage(pi.packageName);
            PackageManager pManager = context.getPackageManager();
            List apps = pManager.queryIntentActivities(resolveIntent, 0);
            ResolveInfo ri = (ResolveInfo) apps.iterator().next();
            if (ri != null) {
                packageName = ri.activityInfo.packageName;
                String className = ri.activityInfo.name;
                Log.i(TAG, "start package:"+packageName+",start package launcher:"+ className);
                Intent intent= new Intent(Intent.ACTION_MAIN);
                ComponentName cn = new ComponentName(packageName, className);
                intent.setComponent(cn);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PutAllExtra(intent,key,value);
                context.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void PutAllExtra(Intent intent,String[] key,String[] value){
        try {
            if (key==null||value==null){
                Log.i(TAG, "PutAllExtra: key 或者 value 为空");
                return;
            }
        if (key.length!=0&&value.length!=0) {
                for (int i = 0; i < key.length; i++) {
                        intent.putExtra(key[i], value[i]);
//                    }
                }
        }else{
            Log.i(TAG, "PutAllExtra: get array key or array value is null");
        }
        }catch (Exception e){
            Log.e(TAG, "PutAllExtra key value 长度不一致 ",e);
        }
    }
    public static void PutAllExtra(Intent intent,String key,String value){
        try {
            if (TextUtils.isEmpty(key)||TextUtils.isEmpty(value)){
                Log.i(TAG, "PutAllExtra:  get key or value is null");
                return;
            }
            intent.putExtra(key, value);
        }catch (Exception e){
            Log.e(TAG, "PutAllExtra key value 长度不一致 ",e);
        }
    }



}
