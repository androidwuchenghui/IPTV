package com.iptv.hn.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by hs on 18/6/12.
 */

public class AliveService extends Service {
    //   http://ip:9090/mp_push/upgradeLog
    private static final String TAG = "AliveService";
   /* private File file;

    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            Log.d(TAG, "  runTest: " + "  安装－－－ ");
            Rest installRest = new Rest(Contants.Rest_api_v2_test + "mp_push/upgradeLog?");

            String tvUserId = Utils.getTvUserId(AliveService.this);
            installRest.addParam("account", tvUserId);
            long paramValue = System.currentTimeMillis();
            installRest.addParam("time_stamp", paramValue);
            installRest.addParam("result_code", 0+"");
            String path = file.getPath();
            int apkVersionCode = getApkVersionCode(AliveService.this, path);
            installRest.addParam("version_number",apkVersionCode);
            Log.d(TAG, "run: installRest "+tvUserId+" "+paramValue);
            installRest.post(new HttpCallback() {
                @Override
                public void onSuccess(JSONObject rawJsonObj, int state, String msg) throws JSONException {
                    Log.d(TAG, "onSuccess: installRest"+rawJsonObj.toString());
                }

                @Override
                public void onFailure(JSONObject rawJsonObj, int state, String msg) {
                    Log.d(TAG, "onFailure: installRest");
                }

                @Override
                public void onError() {
                    Log.d(TAG, "onError: installRest");
                }
            });

            Intent intent = new Intent("com.android.SilenceInstall.Start");
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            startService(intent);

        }
    };*/

    @Override
    public int onStartCommand(Intent intent, final int flags, int startId) {
        Intent innerIntent = new Intent(this, KeepAliveService.class);
        startService(innerIntent);
        startForeground(1001, new Notification());
/**
//        升级请求：  http://ip:9090/mp_push/apkUpgrade
        Rest restUpdate = new Rest(Contants.Rest_api_v2_test + "mp_push/apkUpgrade?");
        restUpdate.addParam("timestamp", System.currentTimeMillis());
        restUpdate.addParam("account", Utils.getTvUserId(this));
        restUpdate.get(new HttpCallback() {
            @Override
            public void onSuccess(JSONObject rawJsonObj, int state, String msg) throws JSONException {
                Object download_url = rawJsonObj.get("download_url");
                String version = (String) rawJsonObj.get("version_number");
                int is_open = (int) rawJsonObj.get("is_open");

                String localVersionName = getLocalVersionName(AliveService.this.getBaseContext());

                Log.d("apkUpgrade", "onSuccess: --- " + rawJsonObj.toString() + "   " + localVersionName + " == " + version+"  is: "+is_open);
                String url = download_url.toString();
                if (version.equals(localVersionName) || url == null || is_open==0) {
                    return;
                }

                OkHttpClient client = new OkHttpClient();
                final Request request = new Request.Builder().get()
                        .url(url)
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d(TAG, "onFailure: ");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                        int len;
                        byte[] buf = new byte[2048];
                        InputStream inputStream = response.body().byteStream();
                        file = null;
                        String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                        file = new File(sdcardPath + "/testMaoPao.apk");
//                        Log.d(TAG, "onResponse: "+file.length());
                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        while ((len = inputStream.read(buf)) != -1) {
                            fileOutputStream.write(buf, 0, len);
                        }
                        fileOutputStream.flush();
                        fileOutputStream.close();
                        inputStream.close();
                        Log.d(TAG, "onResponse: " + file.length());

                        mHandler.post(mRunnable);

                    }
                });


            }

            @Override
            public void onFailure(JSONObject rawJsonObj, int state, String msg) {
                Log.d("apkUpgrade", "onFailure: --- ");
            }

            @Override
            public void onError() {
                Log.d("apkUpgrade", "onError: --- ");
            }
        });

        */

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }
}
