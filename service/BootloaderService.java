package com.iptv.hn.service;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.iptv.hn.AdsView;
import com.iptv.hn.Contants;
import com.iptv.hn.R;
import com.iptv.hn.entity.AdsBean;
import com.iptv.hn.entity.DeviceInfoBean;
import com.iptv.hn.entity.PushMsgStack;
import com.iptv.hn.entity.float_position;
import com.iptv.hn.entity.time_color;
import com.iptv.hn.entity.time_text;
import com.iptv.hn.utility.AdsKeyEventHandler;
import com.iptv.hn.utility.Api;
import com.iptv.hn.utility.CRequest;
import com.iptv.hn.utility.Callback;
import com.iptv.hn.utility.DownloadManager;
import com.iptv.hn.utility.HttpCallback;
import com.iptv.hn.utility.JavaScriptObjectContext;
import com.iptv.hn.utility.JsonUtil;
import com.iptv.hn.utility.MACUtils;
import com.iptv.hn.utility.PfUtil;
import com.iptv.hn.utility.Rest;
import com.iptv.hn.utility.Utils;
import com.iptv.hn.utility.WidgetController;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

import static com.iptv.hn.utility.Utils.getApkVersionName;
import static com.iptv.hn.utility.Utils.getLocalVersionName;
import static com.iptv.hn.utility.Utils.getVersionName;
import static com.iptv.hn.utility.Utils.sendUserBehavior;

/**
 * Created by ligao on 2017/12/23 0023.
 * HTTP 方式的自启动加载服务
 *    修改callback和请求
 */


public class BootloaderService extends IntentService {
    protected CountDownTimer timer;
    private static final String TAG = "BootloaderService";
    protected View mAdsLayerView;
    protected int mAdsCounter;
    private static HashMap<Long, Long> mReceivedMessageIds = new HashMap<Long, Long>();
    private AdsBean adsBean;

    //下面两个参数用来控制web返回键的监听
    private int webPage = 0;
    private String bigWebUrl;
    private int versionCode;
    private String imgUrl;
    private String mJsonData;
    private ActivityManager manager;

    public BootloaderService() {
        super("");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public BootloaderService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        Log.d(TAG, "onCreate: BootloaderService onCreateIn ");
        PackageManager pManager = this.getPackageManager();
        Rest rest = new Rest(Contants.Rest_api_v2 + "mp_push/userinfo?");
        DeviceInfoBean deviceData = com.iptv.hn.entity.Utils.getDeviceData(this, new DeviceInfoBean());
        rest.addParam("account", Utils.getTvUserId(this));
        rest.addParam("ip_addr", deviceData.getIp_addr());
        Log.d(TAG, "onCreate: getIp_addr --->  "+deviceData.getIp_addr());
        rest.addParam("mac_addr", deviceData.getMac_addr());
        rest.addParam("bs_version", deviceData.getModel());
        rest.addParam("mtv_version", getAppTVStoreVersionCode(this, "com.hunantv.operator", pManager));
        rest.addParam("group_id", deviceData.getGid());
        rest.addParam("sdk_version", android.os.Build.VERSION.RELEASE);

        String MPversionName = null;
        try {
            //获取软件版本号，对应AndroidManifest.xml下android:versionCode
            MPversionName = this.getPackageManager().
                    getPackageInfo(this.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String versionName = null;
        try {
            versionName = getVersionName(this, "com.hunantv.operator");
            Log.d(TAG, "run: iptv version :" + versionName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        rest.addParam("time_stamp", System.currentTimeMillis());
        rest.addParam("mp_version", MPversionName + "");
        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
//                    rest.addParam("gj_version", tm.getDeviceSoftwareVersion() + " --- ");
        rest.addParam("gj_version", versionName + " - - ");
        rest.post(new HttpCallback() {
            @Override
            public void onSuccess(JSONObject rawJsonObj, int state, String msg) throws JSONException {
                Log.e("mymsg", msg);
            }

            @Override
            public void onFailure(JSONObject rawJsonObj, int state, String msg) {
                Log.e("mymsg", msg + "2");
            }

            @Override
            public void onError() {
                Log.e("mymsg", "err");

            }
        });
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: in--maopao- service -启动");
        String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File file = new File(sdcardPath + "/testMaoPao.apk");
        if (file.exists()) {
            file.delete();
        }
        Intent innerIntent = new Intent(this, AliveService.class);
        startService(innerIntent);

        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    private boolean isLive;
    private Long getTime = 0l, inLiveTime = 0l, outLiveTime = 0l, liveGetTime = 0l;
    private File file;

    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
//            Log.d(TAG, "  runTest: " + "  安装－－－ ");
            Rest installRest = new Rest(Contants.Rest_api_v2_test + "mp_push/upgradeLog?");
            String tvUserId = Utils.getTvUserId(BootloaderService.this);
            installRest.addParam("account", tvUserId);
            long paramValue = System.currentTimeMillis() / 1000;
            installRest.addParam("time_stamp", paramValue);
            installRest.addParam("result_code", 0 + "");
            String path = file.getPath();
            String apkVersionName = getApkVersionName(BootloaderService.this, path);
            installRest.addParam("version_number", apkVersionName);
            Log.d(TAG, "run: installRest " + tvUserId + " " + paramValue);
            installRest.post(new HttpCallback() {
                @Override
                public void onSuccess(JSONObject rawJsonObj, int state, String msg) throws JSONException {
                    Log.d(TAG, "onSuccess: installRest" + rawJsonObj.toString());
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
    };

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "onHandleIntent: in--maopao --" + " thread :  " + Thread.currentThread().getName());
        //更新服务器端配置文件， 替换本地配置参数
//        loadConfigure();//TODO remove it in release version
//        IjkMediaPlayer.loadLibrariesOnce(null);
//        sendClientInitCommand();
//        String s = Environment.getExternalStorageDirectory().getAbsolutePath() + "/testThink.apk";
//        File file = new File(s);
//        boolean exists = file.exists();
//        Log.e("test",exists+"--exists---new2");
//        TinkerInstaller.onReceiveUpgradePatch(getApplicationContext(), s);
//        try {
//            String versionName = getVersionName(this, "com.hunantv.operator");
//            Log.d(TAG, "onHandleIntent: "+versionName);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        mangoLiveReceiver = new MangoLiveReceiver();
//        intentFilter = new IntentFilter(); //初始化意图过滤器
//        intentFilter.addAction("com.hooray.tvchat.livelog"); //添加动作
//
//        registerReceiver(mangoLiveReceiver, intentFilter); //注册广播

        try {
            Thread.sleep(20000);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        getProcesses();

    }

    boolean started = false;

    private void start() {
        started = true;
        try {
            //获取软件版本号，对应AndroidManifest.xml下android:versionCode
            versionCode = this.getPackageManager().
                    getPackageInfo(this.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        //  芒果直播进入退出监听
        MangoLiveReceiver mangoLiveReceiver = new MangoLiveReceiver();
        mangoLiveReceiver.setLiveListener(new MangoLiveReceiver.LiveListener() {
            @Override
            public void onLive(int status) {
                long currentTimeMillis = System.currentTimeMillis();

                Log.d(TAG, "onLive: ---- " + status + "  time:  " + currentTimeMillis);
                if (status != 1) {
                    isLive = true;
                    Log.d(TAG, "onLive: " + " 进入直播 " + isLive);
                    inLiveTime = currentTimeMillis;

                    if (currentTimeMillis - liveGetTime < Contants.DURATION_PING) {
                        boolean b = currentTimeMillis - liveGetTime < Contants.DURATION_PING;
                        Log.d(TAG, "onLive:  " + b + "   " + "  -  " + currentTimeMillis + "   " + liveGetTime + "   " + Contants.DURATION_PING);
                        return;
                    }

                    pullHander.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "onLive: pullinlive");
                            pullInLive();
                        }
                    }, Contants.DURATION_TOAST_MESSAGE);
                } else {
                    isLive = false;
                    Log.d(TAG, "onLive: " + "  退出直播 " + isLive);
                    outLiveTime = currentTimeMillis;
                    Long per = inLiveTime - getTime;
                    Long li = outLiveTime - inLiveTime;
                    long sum = per + li;
                    Log.d(TAG, "onLive: 请求和进入直播都间隔：" + per + " 直播中待了多久：" + li + " 总时间：" + sum + " req: " + Contants.DURATION_PING);
                    if (sum > Contants.DURATION_PING) {
                        Log.d(TAG, "onLive: " + " 退出直播进行 拉起原来请求 ：－－ pullMessages() ");
                        pullMessages();
                    }

                }
            }
        });

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

                String localVersionName = getLocalVersionName(BootloaderService.this.getBaseContext());

                Log.d("apkUpgrade", "apkUpgrade onSuccess: --- " + rawJsonObj.toString() + "   " + localVersionName + " == " + version + "  is: " + is_open);
                String url = download_url.toString();
                if (version.equals(localVersionName) || url == null || is_open == 0) {
                    //  不进行升级时 获取冒泡数据   isopen = 0 不升级
                    Log.d(TAG, "apkUpgrade isInMangoLiving : " + Contants.isInMangoLiving);
                    if (Contants.isInMangoLiving) {
                        pullInLive();
                    } else {
                        pullMessages();
                    }
                    return;
                }

                OkHttpClient client = new OkHttpClient();
                final Request request = new Request.Builder().get()
                        .url(url)
                        .build();
                client.newCall(request).enqueue(new okhttp3.Callback() {
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
                /**
                if (Contants.isInMangoLiving) {
                    pullInLive();
                } else {
                    pullMessages();
                }
                 */
            }

            @Override
            public void onError() {
                Log.d("apkUpgrade", "onError: --- " + Contants.isInMangoLiving);
                if (Contants.isInMangoLiving) {
                    pullInLive();
                } else {
                    pullMessages();
                }
            }
        });
    }

    private void pullInLive() {
        Log.d("mangguo", "   pullInLive: ----直播----  ");
        String url = Contants.Rest_api_v2_test + "mp_push/liveStrategy?";
        //测试地址
        Rest restApi = new Rest(url);
        String tvUserId = Utils.getTvUserId(this);
        restApi.addParam("account", tvUserId);

        restApi.addParam("version", versionCode + "");
        restApi.addParam("timestamp", System.currentTimeMillis() / 1000);
        if (tvUserId.contains("13348615648")) {
            restApi.addParam("testflag", 1);
        } else {
            restApi.addParam("testflag", 0);
        }
        HttpCallback callback = new HttpCallback() {
            @Override
            public void onSuccess(JSONObject rawJsonObj, int state, String msg) throws JSONException {
                Log.d("httpCall", "直播 onSuccess: onLive" + rawJsonObj.toString());
                if (rawJsonObj.has("basic")) {
                    JSONObject baseNode = rawJsonObj.getJSONObject("basic");
                    int req_time = baseNode.getInt("req_time");
                    Contants.DURATION_PING = req_time * 1000;
                }
                if (rawJsonObj.has("list")) {
                    List<AdsBean> adsList = JsonUtil.jsonArrayStringToList(rawJsonObj.getJSONArray("list").toString(), AdsBean.class);
                    Log.d(TAG, "onSuccess: pull  --  " + adsList.size());
                    if (adsList.size() > 0) {
                        adsBean = adsList.get(0);
                    }
                    showCommingMessage();
                }
                nextLivePull();
            }

            @Override
            public void onFailure(JSONObject rawJsonObj, int state, String msg) {
                Log.d(TAG, "onFailure: ");
               // nextLivePull();
            }

            @Override
            public void onError() {
                Log.d(TAG, "onError: ");
                nextLivePull();
            }
        };
        if (Contants.isInMangoLiving) {
            liveGetTime = System.currentTimeMillis();
            restApi.get(callback);
        } else {
            nextLivePull();
        }

    }

    //从服务器获取数据
    protected void pullMessages() {
//        test();
        Log.d("mangguo", "   pullMessages: ----非直播----  ");
        //正式地址
//        String url = Contants.Rest_api_v2 + "mp_push/strategy?";

        String url = Contants.Rest_api_v2 + "mp_push/strategy?";
        //测试地址
        Rest restApi = new Rest(url);
        String tvUserId = Utils.getTvUserId(this);
        restApi.addParam("account", tvUserId);

        restApi.addParam("version", versionCode + "");
        restApi.addParam("timestamp", System.currentTimeMillis() / 1000);
        if (tvUserId.contains("13348615648")) {
            restApi.addParam("testflag", 1);
        } else {
            restApi.addParam("testflag", 0);
        }

        HttpCallback callback = new HttpCallback() {
            @Override
            public void onSuccess(JSONObject rawJsonObj, int state, String msg) throws JSONException {
                Log.i("httpCall", " onSuccess pullMessages " + rawJsonObj.toString());
                if (rawJsonObj.has("basic")) {
                    //"req_time":3600,"space_time":300,"begin_time":300
                    JSONObject baseNode = rawJsonObj.getJSONObject("basic");
                    int req_time = baseNode.getInt("req_time");
                    int space_time = baseNode.getInt("space_time");
                    int begin_time = baseNode.getInt("begin_time");

                    Contants.DURATION_PING = req_time * 1000;

                    Contants.LAST_PING_TIMESTAMP = System.currentTimeMillis();
//                    if (space_time != 0) {
                    Contants.DURATION_TOAST_MESSAGE = space_time * 1000;
//                    }

                    Contants.APP_INIT_TIME = begin_time * 1000;

//                    PfUtil pfUtil = PfUtil.getInstance();
//                    pfUtil.init(BootloaderService.this);
//                    pfUtil.putLong("app_init_time", Contants.APP_INIT_TIME);
                    Log.d(TAG, "pullMessages: success : " + Contants.DURATION_PING);

                }else {
                    Contants.DURATION_PING = 3600 * 1000;
                }

                if (rawJsonObj.has("list")) {
                    List<AdsBean> adsList = JsonUtil.jsonArrayStringToList(rawJsonObj.getJSONArray("list").toString(), AdsBean.class);
                    Log.d(TAG, "onSuccess: pull  --  " + adsList.size());
                    if (adsList.size() > 0) {
                        adsBean = adsList.get(0);
                    }

                    imgUrl = adsBean.getFile_url();
                    mJsonData = adsBean.getJsonData();
                    Log.d(TAG, "getMyWebData: " + imgUrl + "   " + mJsonData.length());
//                    for (AdsBean adsBean : adsList) {
//                        try {
//                            PushMsgStack.putMessage(BootloaderService.this, adsBean);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        //推送消息回执. 修改为冒泡时发送
//                        //sendMessageReceived(adsBean);
//                    }
                    showCommingMessage();
                }

                nextPull();
            }

            @Override
            public void onFailure(JSONObject rawJsonObj, int state, String msg) {
                Log.i("httpCall", "Failure  " + msg);
                Log.d(TAG, "pullMessages: " + Contants.DURATION_PING);
//                nextPull();
            }

            @Override
            public void onError() {
                Log.i("httpCall", "onError 网络请求错误 ");
                Log.d(TAG, "pullMessages: " + Contants.DURATION_PING);
                nextPull();
            }
        };

        if (!Contants.isInMangoLiving) {
            // 在芒果tv直播中，不请求
            Log.d(TAG, "mangguo:" + "网络请求");
            getTime = System.currentTimeMillis();
            restApi.get(callback);
        } else {
            Log.d(TAG, "pullMessages: 直播时： " + Contants.DURATION_PING);
            nextPull();
        }

    }

    Handler pullHander = new Handler(Looper.getMainLooper());

    private void nextPull() {
        if (isLive) {
            return;
        }
        pullHander.postDelayed(new Runnable() {
            @Override
            public void run() {
                pullMessages();
            }
        }, Contants.DURATION_PING);

    }

    private void nextLivePull() {
        if (!isLive) {
            return;
        }
        pullHander.postDelayed(new Runnable() {
            @Override
            public void run() {
                pullInLive();
            }
        }, Contants.DURATION_PING);

    }

    protected void sendMessageReceived(AdsBean adsBean) {
//        String url = Contants.Rest_api_v2 + "mp_push/behavior?";
        Log.d(TAG, "sendMessageReceived: 上报冒泡上报数据 －－－ ");
        String url = Contants.Rest_api_v2 + "mp_push/behavior?";
        Rest restApi = new Rest(url);
        restApi.addParam("account", Utils.getTvUserId(this));
        restApi.addParam("timestamp", System.currentTimeMillis() / 1000);
        restApi.addParam("in_webTime", System.currentTimeMillis() / 1000);
        restApi.addParam("stay_time", 0);

        restApi.addParam("busi_id", adsBean.getBusi_id());
        restApi.addParam("ip_addr", Utils.getPhoneIp(this));
        restApi.addParam("mac_addr", MACUtils.getLocalMacAddressFromBusybox());
        restApi.addParam("read_type", 100); // 说明：100（到达用户,小窗口弹出时）； 101（用户点击确定键进入大窗口页面）；

        //restApi.addParam("in_webTime", 0); //当read_type=10时，该字段必传，用户进入web页面时间戳，单位秒
        //restApi.addParam("stay_time", 0); //当read_type=10时，该字段必传，用户在web页面停留时长，（单位秒）
        Log.d(TAG, "sendMessageReceived: 上报帐号 account: " + Utils.getTvUserId(this) + "  timestamp: " + System.currentTimeMillis() + "  busi_id: " + adsBean.getBusi_id());
        restApi.post(new HttpCallback() {
            @Override
            public void onSuccess(JSONObject rawJsonObj, int state, String msg) throws JSONException {
                Log.d(TAG, "sendMessageReceived: " + "冒泡数据上报成功");
            }

            @Override
            public void onFailure(JSONObject rawJsonObj, int state, String msg) {
                Log.d(TAG, "sendMessageReceived: " + "冒泡数据上报失败");
            }

            @Override
            public void onError() {
                Log.d(TAG, "sendMessageReceived: " + " 冒泡数据上报错误");

            }
        });

/**
 Rest restApi = new Rest(url);
 Log.d(TAG, "sendMessageReceived: 发送数据－－－ :" + url);

 restApi.addParam("account", Utils.getTvUserId(this));
 //        restApi.addParam("timestamp", "1000"); //
 restApi.addParam("bs_version", "1");
 restApi.addParam("gj_version", "2.000");
 restApi.addParam("mtv_version", "3.222");
 restApi.addParam("group_id", "4.44");
 restApi.addParam("sdk_version", "5");
 restApi.addParam("mp_version", "6");
 //        restApi.addParam("busi_id", adsBean.getBusi_id());
 restApi.addParam("ip_addr", Utils.getPhoneIp(this));
 restApi.addParam("mac_addr", MACUtils.getLocalMacAddressFromBusybox());
 //        restApi.addParam("read_type", 0); //默认：0（到达用户,小窗口弹出时）； 10（用户点击确定键进入大窗口页面）；

 //restApi.addParam("in_webTime", 0); //当read_type=10时，该字段必传，用户进入web页面时间戳，单位秒
 //restApi.addParam("stay_time", 0); //当read_type=10时，该字段必传，用户在web页面停留时长，（单位秒）

 restApi.post(new HttpCallback() {
@Override public void onSuccess(JSONObject rawJsonObj, int state, String msg) throws JSONException {
Log.d("restApi", "post_onSuccess: " + "  冒泡时提交的数据  ");
}


@Override public void onFailure(JSONObject rawJsonObj, int state, String msg) {
Log.d("restApi", "post_onFailure: " + "  冒泡时提交的数据  ");

}

@Override public void onError() {
Log.d("restApi", "post_onError: " + "  冒泡时提交的数据  ");

}
});

 */

    }

    private void test() {

        WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
        //通过getApplication获取的是WindowManagerImpl.CompatModeWrapper
        WindowManager mWindowManager = (WindowManager) getApplication().getSystemService(getApplication().WINDOW_SERVICE);
        //设置window type
        wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        //设置图片格式，效果为背景透明
        wmParams.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //调整悬浮窗显示的停靠位置为左侧置顶
        wmParams.gravity = Gravity.CENTER;
        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
        wmParams.x = 0;
        wmParams.y = 0;

        //设置悬浮窗口长宽数据
        wmParams.width = 500;
        wmParams.height = 200;

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局
        Button button = new Button(getApplicationContext());
        button.setText("Window悬浮测试");
        button.setBackgroundColor(Color.BLUE);
        ImageView imageView = new ImageView(getApplicationContext());
        imageView.setImageResource(R.mipmap.round);
        //添加mFloatLayout
        mWindowManager.addView(imageView, wmParams);
        //浮动窗口按钮

        return;
    }

    protected void showCommingMessage() {
        Log.d(TAG, "showCommingMessage: showBean :  " + (adsBean != null));
        if (adsBean != null) {
            showAdsTemplate(adsBean);
        } else {
//            int duration = Contants.DURATION_TOAST_MESSAGE;
//            AdsBean targetBean = PushMsgStack.notifyAlarmMessage(this, duration);
            Log.d(TAG, "showCommingMessage: showBean    - -- null ");
//            if (targetBean != null) {
//                Log.i("iptv", "targetBean msg" + targetBean.getMsg_id());
//                //应该
//                showAdsTemplate(targetBean);
//            } else if (PushMsgStack.getMessageCount(this) > 1) {
//                Log.i("iptv", "wait msg..");
//                Log.i("iptv", "Contants.DURATION_TOAST_MESSAGE = " + Contants.DURATION_TOAST_MESSAGE);
//                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        showCommingMessage();
//
//                    }
//                }, Contants.DURATION_TOAST_MESSAGE);
//            }
        }
    }

    protected void showAdsTemplate(final AdsBean adsBean) {
        Log.d("show", "showAdsTemplate: 方法调用 ---- >>>> ");

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
//                Log.d(TAG, "onFinish: showAdsTemplate showImage path :" + pathOnSdcard);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "runTest ________: ");
                        if (mAdsLayerView != null && mAdsLayerView.getVisibility() == View.VISIBLE && mAdsCounter != 0) {
                            return;
                        }
                        Log.d(TAG, "runTest ????????: ");
//                        if (Contants.isInMangoLiving) {
//                            // 在芒果tv直播中，不弹窗
////                            PushMsgStack.removeMessage(BootloaderService.this, adsBean);
//                            return;
//                        }

                        if (mAdsCounter == 0 && mAdsLayerView != null) {
                            mAdsLayerView.setVisibility(View.GONE);
                        }

//                        Log.d(TAG, "run:    "+mAdsLayerView+adsBean.toString());
                        //播放完后从缓存中删除
//                        PushMsgStack.removeMessage(BootloaderService.this, adsBean);
                        showAdsDialog(adsBean, pathOnSdcard);

                    }
                });

                //播放完后从缓存中删除
//				PushMsgStack.removeMessage(TcpService.this, adsBean);

                //下轮播放时间间隔

//                final AdsBean nextAlarmMessage = PushMsgStack.notifyAlarmMessage(BootloaderService.this, Contants.DURATION_TOAST_MESSAGE);

//                if (nextAlarmMessage != null) {
//                    long execTime = nextAlarmMessage.getExce_starttime();
//                    long now = TimeUtils.getNowSeconds();
//                    long gap = execTime - now;
//                    Log.d("findbug", "onFinish: next :" + nextAlarmMessage + " gap:" + gap + " extime: " + execTime + "  now : " + now + "  TOAST:  " + Contants.DURATION_TOAST_MESSAGE);
//                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//
////							Utils.showToast(getBaseContext(), "next start = " + nextAlarmMessage.getMsg_id());
//                            //定时启动消息
//                            showAdsTemplate(nextAlarmMessage);
//
//                        }
//                    }, gap);
//                }

                //唤起下一轮播放
//                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
////                        AdsBean bean = PushMsgStack.getTopMessage(BootloaderService.this);
//                        AdsBean bean = adsBean;
//                        Log.d(TAG, "run: showAdsTemplate  轮询调用 ： " + (bean != null) + "   轮询间隔：  " + Contants.DURATION_TOAST_MESSAGE);
//                        if (bean != null) {
//                            showAdsTemplate(bean);
//                        }
//                    }
//                }, Contants.DURATION_TOAST_MESSAGE);
            }

            @Override
            public void onFail(Object... o) {
                Log.d(TAG, "onFail: download-");
                PushMsgStack.removeMessage(BootloaderService.this, adsBean);
            }
        };

        if (adsBean.getFile_url() == null) {
            PushMsgStack.removeMessage(BootloaderService.this, adsBean);
        } else {
            Log.d(TAG, "showAdsTemplate: down---" + adsBean.getFile_url());
            DownloadManager.dl(callback, adsBean.getFile_url());

        }

    }

//    private AudioManager mAm;


    protected void showAdsDialog(final AdsBean adsBean, String localPath) {
        try {
//            if (mReceivedMessageIds.keySet().contains(adsBean.getMsg_id())) {
//                //收到过，就不弹窗了
//                //TODO 要限定当天内
//                Long lastTime = mReceivedMessageIds.get(adsBean.getMsg_id());
//
//                Log.i("iptv", "已经收到过该消息 " + adsBean.getMsg_id());
//                return;
//            }
//
//            mReceivedMessageIds.put(adsBean.getMsg_id(), System.currentTimeMillis());

            final WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

            //        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT | WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT | WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
            //        params.flags =  WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;

            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.alpha = 1.0f;
            params.format = PixelFormat.RGBA_8888;

            if (adsBean.getPosition() == AdsBean.SHOW_AT_LEFT_TOP) {
                params.gravity = Gravity.TOP | Gravity.LEFT;
                params.windowAnimations = android.R.style.Animation_Toast;
            } else if (adsBean.getPosition() == AdsBean.SHOW_AT_RIGHT_TOP) {
                params.gravity = Gravity.TOP | Gravity.RIGHT;
                params.windowAnimations = android.R.style.Animation_Translucent; //平移
            } else if (adsBean.getPosition() == AdsBean.SHOW_AT_LEFT_BOTTOM) {
                params.gravity = Gravity.BOTTOM | Gravity.LEFT;
                params.windowAnimations = android.R.style.Animation_Toast;
            } else if (adsBean.getPosition() == AdsBean.SHOW_AT_RIGHT_BOTTOM) {
                params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                params.windowAnimations = android.R.style.Animation_Translucent; //平移
            } else if (adsBean.getPosition() == AdsBean.SHOW_AT_CENTER) {
                params.gravity = Gravity.CENTER;
                params.windowAnimations = android.R.style.Animation_Toast;
            } else if (adsBean.getPosition() == AdsBean.SHOW_AT_BT_CEN) {
                params.gravity = Gravity.BOTTOM | Gravity.CENTER;
                params.windowAnimations = android.R.style.Animation_Translucent; //平移
            } else if (adsBean.getPosition() == AdsBean.SHOW_AT_TP_CEN) {
                params.gravity = Gravity.TOP | Gravity.CENTER;
                params.windowAnimations = android.R.style.Animation_Translucent; //平移
            } else if (adsBean.getPosition() == AdsBean.SHOW_AT_lF_CEN) {
                params.gravity = Gravity.LEFT | Gravity.CENTER;
                params.windowAnimations = android.R.style.Animation_Translucent; //平移
            } else if (adsBean.getPosition() == AdsBean.SHOW_AT_RI_CEN) {
                params.gravity = Gravity.RIGHT | Gravity.CENTER;
                params.windowAnimations = android.R.style.Animation_Translucent; //平移
            }

            params.x = 0;
            params.y = 0;
            //		params.windowAnimations = android.R.style.Animation_Translucent; //平移
            //        params.windowAnimations = android.R.style.Animation_InputMethod;

            final Context context = this;

            LayoutInflater inflater =
                    (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (adsBean.getModel_id() == 1) {
                mAdsLayerView = inflater.inflate(R.layout.layout_ads_layer3, null);

            } else if (adsBean.getModel_id() == 2) {
                mAdsLayerView = inflater.inflate(R.layout.layout_ads_layer3, null);
            } else {
                mAdsLayerView = inflater.inflate(R.layout.layout_ads_layer4, null);
            }

            /*wm.addView(mAdsLayerView, params);
            hideAdsDialog(context, adsBean);*/
            Log.d(TAG, "show_file_type: " + adsBean.getFile_type());
            if (adsBean.getFile_type() == AdsBean.FILE_VOIDE) {
                showMadelay(inflater, context, wm, params, adsBean, localPath);

            } else if (adsBean.getFile_type() == AdsBean.FILE_WEB) {
                //  type = 4 跳转 web
                showWebView(inflater, context, wm, params, adsBean, localPath);

            } else if (adsBean.getFile_type() == AdsBean.FILE_VOIDE_IJK) {
//                showIjkPlay(inflater, context, wm, params, adsBean, localPath);
//                showMadelay(inflater, context, wm, params, adsBean, localPath);
                ////冒泡时上报数据
//                sendMessageReceived(adsBean);
            } else if (adsBean.getFile_type() == AdsBean.FILE_FLOAT_GIF) {
                showGifPlay(inflater, context, wm, params, adsBean, localPath);
            } else {
                //  type ＝ 1 弹单张图片   2 gif 图片
                showImage(context, wm, params, adsBean, localPath);

            }
            ////冒泡时上报数据
            //  sendMessageReceived(adsBean);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String localpathGif = "";

    private void showGifPlay(LayoutInflater inflater, final Context context, WindowManager wm, WindowManager.LayoutParams params, final AdsBean adsBean, final String localPath) {
        mAdsLayerView = inflater.inflate(R.layout.layout_ads_float_gif, null);
        mAdsLayerView.setBackgroundColor(context.getResources().getColor(R.color.transparent));
        mAdsLayerView.setFocusable(true);

        final time_color time_color = JsonUtil.jsonStringToObject(adsBean.getTime_color(), time_color.class);
        final time_text time_text = JsonUtil.jsonStringToObject(adsBean.getTime_text(), time_text.class);
        final float_position float_positio = JsonUtil.jsonStringToObject(adsBean.getFloat_position(), float_position.class);
        wm.addView(mAdsLayerView, params);
        final AdsView adsView = (AdsView) mAdsLayerView.findViewById(R.id.adsView);
        adsView.requestFocus();
        final TextView textView = (TextView) mAdsLayerView.findViewById(R.id.counter);
        final RelativeLayout relativeLayout = (RelativeLayout) mAdsLayerView.findViewById(R.id.relativeLayout);
        textView.setVisibility(time_text.time_visibility);
        Log.e("test", float_positio.getFloat_width() + "===" + float_positio.getFloat_height());
        final GifImageView gifImageView = (GifImageView) mAdsLayerView.findViewById(R.id.gif);
        Drawable b = new BitmapDrawable(localPath);
        relativeLayout.setBackground(b);
        WidgetController.setLayoutViedo(gifImageView, float_positio.getFloat_x(), float_positio.getFloat_y(), float_positio);
//       ijkVideoView.set
//        ijkVideoView.setVideoURI(Uri.parse(adsBean.getVideo_url()));
//        ijkVideoView.start();
//        gifImageView.set
        DownloadManager.dl(new Callback() {
            @Override
            public void onStart(Object... o) {

            }

            @Override
            public void onProgress(Object... o) {

            }

            @Override
            public void onFinish(Object... o) {
                localpathGif = o[0].toString();
                GifDrawable gifFromResource = null;
                try {
                    gifFromResource = new GifDrawable(localpathGif);
                    gifImageView.setImageDrawable(gifFromResource);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    timer.start();
                }
            }

            @Override
            public void onFail(Object... o) {
                timer.start();
            }
        }, adsBean.getVideo_url());

        if (time_text.time_visibility == View.VISIBLE) {
            textView.setVisibility(View.VISIBLE);
            //显示的时候设置时间 文本颜色 文本 控件位置
            textView.setText(String.valueOf(adsBean.getShow_time()));
            //                Drawable
            //                Bitmap bitmap = BitmapFactory.decodeFile(localPath);
            textView.setTextSize(time_text.time_size);
            textView.setTextColor(Color.rgb(time_color.R, time_color.G, time_color.B));
            //                RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams)relativeLayout.getLayoutParams();
            //                params2.setMargins(100, 100, 200, 100);// 通过自定义坐标来放置你的控件
            //                textView .setLayoutParams(params2);
            WidgetController.setLayout(textView, time_text.time_x, time_text.time_y);
            String text = textView.getText().toString();
            int i = adsBean.getShow_time() * 1000;
            timer = new CountDownTimer(i, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    adsBean.setShow_time(adsBean.getShow_time() - 1);
                    textView.setText(String.valueOf(adsBean.getShow_time(1)));
                }

                @Override
                public void onFinish() {
//                    ijkVideoView.stopPlayback();
                    hideAdsDialog(context, adsBean);
//                   clearMeiadPlay(mplayer);
                    timer.cancel();
                }
            };

        } else {
            textView.setVisibility(View.GONE);
        }


        adsView.setKeyBoardCallback(new Callback() {
            @Override
            public void onStart(Object... o) {

            }

            @Override
            public void onProgress(Object... o) {

            }

            @Override
            public void onFinish(Object... o) {
                if (timer != null) {
                    timer.cancel();
                }
                //                int keyCode = (int)o[0];
                AdsKeyEventHandler.onKeyOk(context, adsBean);
                hideAdsDialog(context, adsBean);
                Log.e("test", "this is ok");
//                ijkVideoView.stopPlayback();
                //行为日志上报
                String user = Utils.getTvUserId(context);
                String localIp = Utils.getPhoneIp(context);
                try {
//                    Api.postUserBehaviors(context, adsBean.getMsg_id() + "", user, localIp, "10", adsBean.getBusi_id());
                } catch (Exception ex) {
                    Log.i("iptv", "postUserBehaviors error");
                }
            }

            @Override
            public void onFail(Object... o) {
                if (adsBean.getIs_back() == 0) {
                    hideAdsDialog(context, adsBean);
//                   clearMeiadPlay(mplayer);
//                    ijkVideoView.stopPlayback();
                    if (timer != null) {
                        timer.cancel();
                    }
                }
            }
        });
    }

    /**
     * 显示媒体资源
     */
    @SuppressLint("WrongConstant")
    public void showMadelay(LayoutInflater inflater, final Context context,
                            WindowManager wm, final WindowManager.LayoutParams params, final AdsBean adsBean, final String localPath) {
        try {
            boolean play = isPlay(context);
            Log.i("showMadelay", "play successfully." + play);
//            Log.i("adsBean", "receive ,msgid = " + adsBean.toString());
            mAdsLayerView = inflater.inflate(R.layout.layout_ads_video, null);
            mAdsLayerView.setBackgroundColor(context.getResources().getColor(R.color.transparent));
            mAdsLayerView.setFocusable(true);
            //        mAdsLayerView.requestFocus();
            //        mAdsLayerView.getLocationOnScreen();
            final time_color time_color = JsonUtil.jsonStringToObject(adsBean.getTime_color(), time_color.class);
            final time_text time_text = JsonUtil.jsonStringToObject(adsBean.getTime_text(), time_text.class);
            final float_position float_positio = JsonUtil.jsonStringToObject(adsBean.getFloat_position(), float_position.class);
            Log.d(TAG, "showMadelay: " + time_color + "  " + time_text + "  position:  " + adsBean.getFloat_position());
            wm.addView(mAdsLayerView, params);
            final AdsView adsView = (AdsView) mAdsLayerView.findViewById(R.id.adsView);
            adsView.requestFocus();
            final MediaPlayer mplayer = new MediaPlayer();
            mplayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            adsView.setKeyBoardCallback(new Callback() {
                @Override
                public void onStart(Object... o) {

                }

                @Override
                public void onProgress(Object... o) {

                }

                @Override
                public void onFinish(Object... o) {
                    if (timer != null) {
                        timer.cancel();
                    }
                    //                int keyCode = (int)o[0];
                    AdsKeyEventHandler.onKeyOk(context, adsBean);
                    hideAdsDialog(context, adsBean);
                    Log.e("test", "this is ok");
                    clearMeiadPlay(mplayer);
                    //行为日志上报
                    String user = Utils.getTvUserId(context);
                    String localIp = Utils.getPhoneIp(context);
//                    try {
//                        Api.postUserBehaviors(context, adsBean.getMsg_id() + "", user, localIp, "10", adsBean.getBusi_id());
//                    } catch (Exception ex) {
//                        Log.i("iptv", "postUserBehaviors error");
//                    }
                }

                @Override
                public void onFail(Object... o) {
                    if (adsBean.getIs_back() == 0) {
                        hideAdsDialog(context, adsBean);
                        clearMeiadPlay(mplayer);
                        if (timer != null) {
                            timer.cancel();
                        }
                    }
                }
            });
            //获取视频控件播放视频
            final SurfaceView surfaceView = (SurfaceView) mAdsLayerView.findViewById(R.id.play);
            SurfaceHolder holder = surfaceView.getHolder();
            WidgetController.setLayoutViedo(surfaceView, float_positio.getFloat_x(), float_positio.getFloat_y(), float_positio);
//            surfaceView.setVisibility(adsBean.get);
//                ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
//                lp.width = float_positio.getFloat_width();
//                lp.height =float_positio.getFloat_height();
////                lp.
//                surfaceView.setLayoutParams(lp);
            //            holder.setKeepScreenOn(true);
//            surfaceView.setPadding();
            holder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    //创建
                    Log.e("test", "surfaceCreated");
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    //尺寸发生变化
                    Log.e("test", "surfaceChanged");
                    mplayer.setDisplay(holder);
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    //销毁
                    Log.e("test", "surfaceDestroyed");
                    //                    clearMeiadPlay(mplayer);
                    hideAdsDialog(context, adsBean);

                }
            });

            try {
                //            mplayer.setDataSource("rtp://239.76.252.137:9000");
                mplayer.setDataSource(adsBean.getVideo_url());
                mplayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    // 在播放完毕被回调
                    //                start.setEnabled(true);
                    //销毁
                    clearMeiadPlay(mp);
                    hideAdsDialog(context, adsBean);
                    Log.e("test", "setOnCompletionListener");
                }
            });

            mplayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    //                   int vWidth = mplayer.getVideoWidth();
                    //                   int vHeight = mplayer.getVideoHeight();
                    //                    Point outSize = new Point();
                    //                    if (vWidth > outSize.x || vHeight > outSize.y) {
                    //                        // 如果video的宽或者高超出了当前屏幕的大小，则要进行缩放
                    //                        float wRatio = (float) vWidth / (float) outSize.x;
                    //                        float hRatio = (float) vHeight / (float) outSize.y;
                    //
                    //                        // 选择大的一个进行缩放
                    //                        float ratio = Math.max(wRatio, hRatio);
                    //
                    //                        vWidth = (int) Math.ceil((float) vWidth / ratio);
                    //                        vHeight = (int) Math.ceil((float) vHeight / ratio);
                    //
                    //                        // 设置surfaceView的布局参数
                    //                        params.width = vWidth;
                    //                        params.height = vHeight;
                    //                        surfaceView.setLayoutParams(p);
                    //
                    //                    }
                    Log.e("test", "setOnPreparedListener");
                    // 然后开始播放视频
                    mplayer.setDisplay(surfaceView.getHolder());
                    mplayer.seekTo(0);
                    mplayer.start();

                    sendMessageReceived(adsBean); // 上报视屏播放日志

                    final TextView textView = (TextView) mAdsLayerView.findViewById(R.id.counter);
                    RelativeLayout relativeLayout = (RelativeLayout) mAdsLayerView.findViewById(R.id.relativeLayout);
                    textView.setVisibility(time_text.time_visibility);
//                    Drawable b = new BitmapDrawable(localPath);
//                    relativeLayout.setBackground(b);
                    Log.e("test", float_positio.getFloat_width() + "===" + float_positio.getFloat_height());
                    if (time_text.time_visibility == View.VISIBLE) {
                        textView.setVisibility(View.VISIBLE);
                        //显示的时候设置时间 文本颜色 文本 控件位置
                        textView.setText(String.valueOf(adsBean.getShow_time()));
                        //                Drawable
                        //                Bitmap bitmap = BitmapFactory.decodeFile(localPath);
                        textView.setTextSize(time_text.time_size);
                        textView.setTextColor(Color.rgb(time_color.R, time_color.G, time_color.B));
                        //                RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams)relativeLayout.getLayoutParams();
                        //                params2.setMargins(100, 100, 200, 100);// 通过自定义坐标来放置你的控件
                        //                textView .setLayoutParams(params2);
                        WidgetController.setLayout(textView, time_text.time_x, time_text.time_y);
                        timer = new CountDownTimer(adsBean.getShow_time() * 1000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                adsBean.setShow_time(adsBean.getShow_time() - 1);
                                textView.setText(String.valueOf(adsBean.getShow_time(1)));
                            }

                            @Override
                            public void onFinish() {

                                mplayer.stop();
                                hideAdsDialog(context, adsBean);
                                clearMeiadPlay(mplayer);
                            }
                        };
                        timer.start();
                    } else {
                        textView.setVisibility(View.GONE);
                    }
                }
            });
            mplayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    // 发生错误重新播放
                    //                video_play(0);
                    //                isPlaying = false;
                    Log.e("test", "setOnErrorListener");
                    clearMeiadPlay(mp);
                    hideAdsDialog(context, adsBean);
                    //销毁
                    return false;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*  public void showIjkPlay(LayoutInflater inflater, final Context context,
                              WindowManager wm, final WindowManager.LayoutParams params, final AdsBean adsBean, final String localPath) {
          IjkMediaPlayer.loadLibrariesOnce(null);
          IjkMediaPlayer.native_profileBegin("libijkplayer.so");
          mAdsLayerView = inflater.inflate(R.layout.layout_ads_videoijk, null);
          mAdsLayerView.setBackgroundColor(context.getResources().getColor(R.color.transparent));
          mAdsLayerView.setFocusable(true);
          final time_color time_color = JsonUtil.jsonStringToObject(adsBean.getTime_color(), time_color.class);
          final time_text time_text = JsonUtil.jsonStringToObject(adsBean.getTime_text(), time_text.class);
          final float_position float_positio = JsonUtil.jsonStringToObject(adsBean.getFloat_position(), float_position.class);
          wm.addView(mAdsLayerView, params);
          final AdsView adsView = (AdsView) mAdsLayerView.findViewById(R.id.adsView);
          adsView.requestFocus();


          final TextView textView = (TextView) mAdsLayerView.findViewById(R.id.counter);
          RelativeLayout relativeLayout = (RelativeLayout) mAdsLayerView.findViewById(R.id.relativeLayout);

  //        Log.e("Mytest", float_positio.getFloat_width() + "===" + float_positio.getFloat_height()+" videoUrl: "+adsBean.getVideo_url());
          final IjkVideoView ijkVideoView = (IjkVideoView) mAdsLayerView.findViewById(R.id.video_view);

          WidgetController.setLayoutViedo(ijkVideoView, float_positio.getFloat_x(), float_positio.getFloat_y(), float_positio);

  //        Log.d(TAG, "showIjkPlay: "+adsBean.getTime_text()+" x:"+time_text.time_x+"visibility : "+time_text.time_visibility+adsBean.getVideo_url());

  //        Drawable b = new BitmapDrawable(localPath);
  //        relativeLayout.setBackground(b);
  //        Log.d(TAG, "showIjkPlay: ---- "+time_text);
          if(time_text!=null){

              textView.setVisibility(time_text.time_visibility);
              if (time_text.time_visibility == View.VISIBLE) {
                  textView.setVisibility(View.VISIBLE);
                  //显示的时候设置时间 文本颜色 文本 控件位置
                  textView.setText(String.valueOf(adsBean.getShow_time()));
                  //                Drawable
                  //                Bitmap bitmap = BitmapFactory.decodeFile(localPath);
                  textView.setTextSize(time_text.time_size);
                  textView.setTextColor(Color.rgb(time_color.R, time_color.G, time_color.B));
                  //                RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams)relativeLayout.getLayoutParams();
                  //                params2.setMargins(100, 100, 200, 100);// 通过自定义坐标来放置你的控件
                  //                textView .setLayoutParams(params2);
                  WidgetController.setLayout(textView, time_text.time_x, time_text.time_y);
                  final int[] time = {adsBean.getShow_time()};
                  timer = new CountDownTimer(time[0] *1000+1000, 1000) {
                      @Override
                      public void onTick(long millisUntilFinished) {
                          time[0] = time[0] -1;

                          textView.setText(String.valueOf( time[0]));
                      }

                      @Override
                      public void onFinish() {

                          ijkVideoView.stopPlayback();
                          ijkVideoView.releaseWithoutStop();
                          timer.cancel();
                          hideAdsDialog(context, adsBean);

  //                   clearMeiadPlay(mplayer);

                      }
                  };

              } else {
                  textView.setVisibility(View.GONE);
              }

          }
          ijkVideoView.setVideoURI(Uri.parse(adsBean.getVideo_url()));
          ijkVideoView.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
              @Override
              public void onPrepared(IMediaPlayer iMediaPlayer) {
                  Log.d(TAG, "showIjkPlay  onPrepared: "+ijkVideoView.getDuration()+"  -  "+adsBean.getShow_time());
                  ijkVideoView.start();
                  timer.start();
              }
          });

  //        timer.start();

          adsView.setKeyBoardCallback(new Callback() {
              @Override
              public void onStart(Object... o) {

              }

              @Override
              public void onProgress(Object... o) {

              }

              @Override
              public void onFinish(Object... o) {
                  if (timer != null) {
                      timer.cancel();
                  }
                  //                int keyCode = (int)o[0];
                  AdsKeyEventHandler.onKeyOk(context, adsBean);
                  hideAdsDialog(context, adsBean);
                  Log.e("test", "this is ok");
                  ijkVideoView.stopPlayback();
                  //行为日志上报
                  String user = Utils.getTvUserId(context);
                  String localIp = Utils.getPhoneIp(context);
                  try {
                      Api.postUserBehaviors(context, adsBean.getMsg_id() + "", user, localIp, "10", adsBean.getBusi_id());
                  } catch (Exception ex) {
                      Log.i("iptv", "postUserBehaviors error");
                  }
              }

              @Override
              public void onFail(Object... o) {
                  if (adsBean.getIs_back() == 0) {
                      hideAdsDialog(context, adsBean);
  //                   clearMeiadPlay(mplayer);
                      ijkVideoView.stopPlayback();
                      if (timer != null) {
                          timer.cancel();
                      }
                  }
              }
          });

      }
  */
    long currentTime;
    int showfirst;

    /**
     * 显示交互式按钮
     */
    public void showWebView(final LayoutInflater inflater, final Context context, final WindowManager wm,
                            final WindowManager.LayoutParams params, final AdsBean adsBean, final String localPath) {
        showfirst = 0;
        // 删除下载的图片
        localpathGif = localPath;
        File file = new File(localPath);
        if (file.exists()) {
            file.delete();
        }

        Log.d(TAG, "showWebView: come in --12.12--- ");
        //打开一个小窗口web页面
        mAdsLayerView = inflater.inflate(R.layout.layout_ads_webview, null);
        mAdsLayerView.setBackgroundColor(context.getResources().getColor(R.color.transparent));
        mAdsLayerView.setFocusable(true);

        final time_color time_color = JsonUtil.jsonStringToObject(adsBean.getTime_color(), time_color.class);
        final time_text time_text = JsonUtil.jsonStringToObject(adsBean.getTime_text(), time_text.class);

//        mAdsLayerView.requestFocus();
        wm.addView(mAdsLayerView, params);

        final AdsView adsView = (AdsView) mAdsLayerView.findViewById(R.id.adsView);
//        adsView.requestFocus();
        final WebView webView = (WebView) adsView.findViewById(R.id.webview);
        WebSettings settings = webView.getSettings();
//        webView.setHorizontalScrollBarEnabled(true);
//        webView.setVerticalScrollBarEnabled(true);
//        webView.setFilterTouchesWhenObscured(true);

//        webView.getSettings().setAllowContentAccess(true);
//        webView.getSettings().setJavaScriptEnabled(true);
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportZoom(true);
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
//        settings.setLoadWithOverviewMode(true);
//        settings.setUseWideViewPort(true);

        webView.setBackgroundColor(0);   //    －－ 设置透明
        webView.addJavascriptInterface(new JavaScriptObjectContext(context, webView, mAdsLayerView, adsBean), "AppFunction");

        String urlLoad = adsBean.getSpecial_url();
        final String userToken = Utils.getTvUserToken(context);
        final String tvUserId = Utils.getTvUserId(context);
        final DeviceInfoBean deviceData = com.iptv.hn.entity.Utils.getDeviceData(context, new DeviceInfoBean());
        if (urlLoad.contains("?")) {
            urlLoad = urlLoad + "&userId=" + tvUserId + "&userToken=" + userToken + "&mac=" + deviceData.getMac_addr() + "&ip=" + deviceData.getIp_addr();
        } else {
            urlLoad = urlLoad + "?userId=" + Utils.getTvUserId(context) + "&userToken=" + userToken + "&mac=" + deviceData.getMac_addr() + "&ip=" + deviceData.getIp_addr();
        }

        webView.loadUrl(urlLoad);
        Map<String, String> mapRequest = CRequest.URLRequest(urlLoad);
        final String isView = mapRequest.get("isView");
        Log.d(TAG, "showWebViewUrl:  小web： " + urlLoad + "   is= " + isView);

        webView.requestFocus();
//        webView.getSettings().setBuiltInZoomControls(true);
//        webView.getSettings().setUseWideViewPort(true);

        // 复写WebViewClient类的shouldOverrideUrlLoading方法

        final String finalUrlLoad = urlLoad;
        webView.setWebViewClient(new WebViewClient() {

                                     @Override
                                     public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                         showfirst++;
                                         Log.d(TAG, "shouldOverrideUrlLoadingTest: " + url + "  view： " + mAdsLayerView + "   num: " + showfirst);

                                         // 步骤2：根据协议的参数，判断是否是所需要的url
                                         // 一般根据scheme（协议格式） & authority（协议名）判断（前两个参数）
                                         //假定传入进来的 url = "js://webview?arg1=111&arg2=222"（同时也是约定好的需要拦截的）
//                                          Log.d(TAG, "shouldOverrideUrlLoading: -- : "+ url);
                                         Uri uri = Uri.parse(url);
                                         // 如果url的协议 = 预先约定的 js 协议 就解析往下解析参数

                                         if (uri.getScheme().equals("action")) {
//                                             if (mAdsLayerView == null) {
//                                                 return true;
//                                             }
//                                             String user = Utils.getTvUserId(context);
//                                             String localIp = Utils.getPhoneIp(context);
//                                             Api.postUserBehaviors(context, adsBean.getMsg_id() + "", user, localIp, "10", adsBean.getBusi_id());

                                             // 如果 authority  = 预先约定协议里的 webview，即代表都符合约定的协议
                                             // 所以拦截url,下面JS开始调用Android需要的方法
                                             if (uri.getAuthority().equals("closeWeb")) {
//                                                 Log.d(TAG, "shouldOverrideUrlLoading: 调用closeWeb－－－－");
                                                 /*if (close == 1) {
                                                     return true;
                                                 }
                                                 Log.d(TAG, "shouldClosed:"+"关闭  ");
                                                 close = 1;*/
                                                 long outTime = System.currentTimeMillis() / 1000;
                                                 if (showfirst == 1) {
                                                     sendUserBehavior(adsBean.getBusi_id() + "", 102, currentTime, outTime);

                                                 } else if (showfirst == 2) {
                                                     sendUserBehavior(adsBean.getBusi_id() + "", 1002, currentTime, outTime);
                                                 } else if (showfirst == 3) {
                                                     sendUserBehavior(adsBean.getBusi_id() + "", 10002, currentTime, outTime);
                                                 } else {
                                                     sendUserBehavior(adsBean.getBusi_id() + "", 102, currentTime, outTime);
                                                 }
                                                 webPage = 0;
                                                 //  步骤3：
                                                 // 执行JS所需要调用的逻辑
                                                 Log.d(TAG, "shouldOverrideUrlLoading: " + "   --  js调用了Android的方法: url: " + url + "  uri : " + uri);
                                                 // 可以在协议上带有参数并传递到Android上
                                                 HashMap<String, String> params = new HashMap<>();
                                                 Set<String> collection = uri.getQueryParameterNames();

                                             } else if (uri.getAuthority().equals("openApp")) {
                                                 long outTime = System.currentTimeMillis() / 1000;
                                                 if (showfirst == 1) {
                                                     sendUserBehavior(adsBean.getBusi_id() + "", 101, currentTime, outTime);
                                                 } else if (showfirst == 2) {
                                                     sendUserBehavior(adsBean.getBusi_id() + "", 1001, currentTime, outTime);
                                                 } else if (showfirst == 3) {
                                                     sendUserBehavior(adsBean.getBusi_id() + "", 10001, currentTime, outTime);
                                                 } else {
                                                     sendUserBehavior(adsBean.getBusi_id() + "", 101, currentTime, outTime);
                                                 }

                                                 //url参数键值对
                                                 Map<String, String> mapRequest = CRequest.URLRequest(url);

                                                 String query = uri.getQuery();
                                                 String jsonData = mapRequest.get("jsonData");
                                                 Log.d(TAG, "lastJsonData: " + jsonData);
                                                 String isPay = mapRequest.get("isPay");
                                                 String toClass = mapRequest.get("toClass");
                                                 String toPackage = mapRequest.get("toPackage");
                                                 Log.d(TAG, "shouldOverrideUrlLoading: query :" + query);
                                                 PackageInfo packageInfo = null;
                                                 if (toPackage != null) {
                                                     try {
                                                         packageInfo = context.getPackageManager().getPackageInfo(toPackage, 0);
                                                     } catch (PackageManager.NameNotFoundException e) {
                                                         e.printStackTrace();
                                                     }
                                                 }
                                                 Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                                                 mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                                                 PackageManager mPackageManager = context.getPackageManager();
                                                 List<ResolveInfo> mAllApps = mPackageManager.queryIntentActivities(mainIntent, 0);
                                                 //按包名排序
                                                 Collections.sort(mAllApps, new ResolveInfo.DisplayNameComparator(mPackageManager));
                                                 // 包含  isPay
                                                 if (isPay != null) {

                                                     //   isPay  标志为1时
                                                     if (isPay.equals("1")) {
                                                         if (query.contains("jsonData")) {
                                                             for (ResolveInfo res : mAllApps) {
                                                                 //该应用的包名和主Activity
                                                                 String pkg = res.activityInfo.packageName;
                                                                 String cls = res.activityInfo.name;
                                                                 //packageInfo   不为空说明已经安装了该apk
                                                                 if (packageInfo != null && toClass != null) {

                                                                     ComponentName componet = new ComponentName(toPackage, toClass);
                                                                     Intent intentPay = new Intent();
                                                                     intentPay.setComponent(componet);
//                                                                     String str = "isPay=1&jsonData=";
//                                                                     String replace = query.replace(str, "");
//                                                                     Log.d(TAG, "My_jsonData: "+replace);
                                                                     Log.d(TAG, "My_jsonData:  --  " + jsonData);
                                                                     intentPay.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                     intentPay.putExtra("jsonData", jsonData);
                                                                     context.startActivity(intentPay);
                                                                 }
                                                                 /*
                                                                 else if (pkg.contains("cn.com.bellmann.payment")) {
                                                                     ComponentName componet = new ComponentName(pkg, "cn.com.bellmann.payment.PayActivity");
                                                                     Intent intentPay = new Intent();
                                                                     intentPay.setComponent(componet);
//                                                                     String str = "isPay=1&jsonData=";
//                                                                     String replace = query.replace(str, "");
//                                                                     Log.d(TAG, "My_jsonData: "+replace);
                                                                     Log.d(TAG, "My_jsonData:  --  " + jsonData);
//                                                                     Log.d(TAG, "My_jsonData: "+userToken);
                                                                     intentPay.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                     intentPay.putExtra("jsonData", jsonData);
                                                                     context.startActivity(intentPay);
                                                                 } */

                                                             }
                                                         } else {
                                                             //没有jsonData时用这种方法启动
//                                                             for (ResolveInfo res : mAllApps) {
//                                                                 //该应用的包名和主Activity
//                                                                 String pkg = res.activityInfo.packageName;
//                                                                 String cls = res.activityInfo.name;
//                                                                 if (pkg.contains("cn.com.bellmann.payment")) {
//                                                                     ComponentName componet = new ComponentName(pkg, "cn.com.bellmann.payment.PayActivity");
//                                                                     Intent intentPay = new Intent();
//                                                                     intentPay.setComponent(componet);
//
//                                                                     intentPay.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////                                                                      intent一个一个添加参数
//                                                                     Intent intent = testIntent(intentPay);
//                                                                     context.startActivity(intent);
//
//                                                                 }
//
//                                                             }

                                                         }

                                                     } else {
                                                         String str = "isPay=0&jsonData=";
                                                         String replace = query.replace(str, "");

                                                         String packageName = adsBean.getSpecial_url();
                                                         for (ResolveInfo res : mAllApps) {
                                                             //该应用的包名和主Activity
                                                             String pkg = res.activityInfo.packageName;
                                                             String cls = res.activityInfo.name;

                                                             if (pkg.contains(packageName)) {
                                                                 ComponentName componet = new ComponentName(pkg, cls);
                                                                 Intent intent = new Intent();
                                                                 intent.setComponent(componet);
                                                                 intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                 intent.putExtra("jsonData", replace);
                                                                 context.startActivity(intent);
                                                             }
                                                         }
                                                     }
                                                 } else {
                                                     //  没有 isPay 只有  jsonData
                                                     String packageName = toPackage;
                                                     Log.d(TAG, "shouldOverrideUrlLoading: 其他跳转方法: " + toPackage);
                                                     //  未接收到包名时 返回
                                                     if (packageName == null) {
                                                         hideAdsDialog(context, adsBean);
                                                         return true;
                                                     }
                                                     for (ResolveInfo res : mAllApps) {
                                                         //该应用的包名和主Activity
                                                         String pkg = res.activityInfo.packageName;
                                                         String cls;
                                                         if (toClass == null) {
                                                             cls = res.activityInfo.name;
                                                         } else {
                                                             cls = toClass;
                                                         }

                                                         if (pkg.contains(packageName)) {
                                                             ComponentName componet = new ComponentName(pkg, cls);
                                                             Intent intent = new Intent();
                                                             intent.setComponent(componet);
                                                             intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                             intent.putExtra("jsonData", jsonData);
                                                             context.startActivity(intent);
                                                         }
                                                     }
                                                 }

                                             } else if (uri.getAuthority().equals("sendBroadcast")) {
                                                 Log.d(TAG, "sendBroadcastTest: " + url);
                                                 Map<String, String> dataMap = CRequest.URLRequest(url);
                                                 int act = Integer.parseInt(dataMap.get("act"));
                                                 String action = dataMap.get("Action");
                                                 long outTime = System.currentTimeMillis() / 1000;

                                                 switch (act) {
                                                     case 1:// 芒果详情
                                                         Log.d(TAG, "broadCastShow: " + url);
                                                         Intent intent1 = new Intent();
                                                         intent1.setAction(action);
                                                         String video_id = dataMap.get("video_id");
                                                         String category_id = dataMap.get("category_id");
                                                         String media_assets_id = dataMap.get("media_assets_id");
                                                         String versionCode1 = dataMap.get("versionCode");
                                                         Log.d(TAG, "broadCastShow: video_id: " + video_id + "  category_id:" + category_id + " media_assets_id: " + media_assets_id + " versionCode1: " + versionCode1);

                                                         //  增加参数
                                                         String sourceId = dataMap.get("sourceId");
                                                         String busiId = dataMap.get("busiId");

                                                         Map<String, Object> mapA = new HashMap<>();
                                                         mapA.put("video_id", video_id);
                                                         mapA.put("category_id", category_id);
                                                         mapA.put("media_assets_id", media_assets_id);

                                                         Map<String, Object> map1 = new HashMap<>();
                                                         map1.put("action", act);
                                                         map1.put("versionCode", versionCode1);
                                                         map1.put("data", mapA);
                                                         map1.put("sourceId", sourceId);
                                                         map1.put("busiId", busiId);

                                                         Gson gson1 = new GsonBuilder().enableComplexMapKeySerialization().create();
                                                         String jsonString1 = gson1.toJson(map1);

                                                         intent1.putExtra("jsonData", jsonString1);

                                                         sendBroadcast(intent1);
                                                         sendUserBehavior(adsBean.getBusi_id() + "", 101, currentTime, outTime);
                                                         break;
                                                     case 2:// 芒果专题
                                                         Intent intent2 = new Intent();
                                                         String nns_special_id = dataMap.get("nnsId");
                                                         String versionCode = dataMap.get("versionCode");
                                                         //  增加参数
                                                         String sourceId2 = dataMap.get("sourceId");
                                                         String busiId2 = dataMap.get("busiId");


                                                         intent2.setAction(action);
                                                         Map<String, Object> mapB = new HashMap<>();
                                                         mapB.put("nns_special_id", nns_special_id);
                                                         Gson gson2 = new GsonBuilder().enableComplexMapKeySerialization().create();

                                                         Map<String, Object> map2 = new HashMap<>();
                                                         map2.put("action", act);
                                                         map2.put("versionCode", versionCode);
                                                         map2.put("data", mapB);
                                                         map2.put("sourceId", sourceId2);
                                                         map2.put("busiId", busiId2);


                                                         String jsonString2 = gson2.toJson(map2);

                                                         intent2.putExtra("jsonData", jsonString2);
                                                         sendBroadcast(intent2);
                                                         sendUserBehavior(adsBean.getBusi_id() + "", 101, currentTime, outTime);
                                                         break;
                                                     case 3:
                                                         Intent intent3 = new Intent();
                                                         intent3.setAction(action);

                                                         Log.d(TAG, "broadCastShow: " + url);
                                                         String bag = dataMap.get("bag");
                                                         String button_name = dataMap.get("button_name");
                                                         String id = dataMap.get("id");
                                                         String name = dataMap.get("name");
                                                         String price = dataMap.get("price");
                                                         String product_type = dataMap.get("product_type");
                                                         String time = dataMap.get("time");
                                                         String order_url = dataMap.get("order_url");
                                                         Log.d(TAG, "broadCastShow: bag :" + bag + " button_name:" + button_name + " id:" + id + " name:" + name + " price:" + price + " product_type:" + product_type + " time:" + time + " order_url:" + order_url);
                                                         String my_button_name = "";
                                                         String my_name = "";
                                                         try {
                                                             my_button_name = URLDecoder.decode(button_name, "UTF-8");
                                                             my_name = URLDecoder.decode(name, "UTF-8");

                                                         } catch (UnsupportedEncodingException e) {
                                                             e.printStackTrace();
                                                         }
                                                         Log.d(TAG, "broadCastShow: 转码： " + my_button_name + "    " + my_name);

                                                         Map<String, Object> mapC1 = new HashMap<>();
                                                         mapC1.put("bag", bag);
                                                         mapC1.put("button_name", my_button_name);
                                                         mapC1.put("id", id);
                                                         mapC1.put("name", my_name);
                                                         mapC1.put("price", price);
                                                         mapC1.put("product_type", product_type);
                                                         mapC1.put("time", time);
                                                         mapC1.put("order_url", order_url);

                                                         Map<String, Object> mapC2 = new HashMap<>();
                                                         mapC2.put("product_list", mapC1);

                                                         Map<String, Object> mapC3 = new HashMap<>();
                                                         mapC3.put("action", act);
                                                         mapC3.put("data", mapC2);

                                                         Gson gson3 = new GsonBuilder().enableComplexMapKeySerialization().create();
                                                         String jsonString3 = gson3.toJson(mapC3);
                                                         intent3.putExtra("jsonData", jsonString3);

                                                         sendBroadcast(intent3);
                                                         break;
                                                     case 4:

                                                         break;
                                                 }

                                             }

                                             hideAdsDialog(context, adsBean);

                                             return true;
                                         }

                                         webPage += 1;
                                         String s = CRequest.UrlPage(url);
                                         if (webPage == 1) {
                                             bigWebUrl = s;
                                             Log.d(TAG, "onKeyWebPage: load :" + webPage + "   " + s);
                                             long outTime = System.currentTimeMillis() / 1000;
                                             sendUserBehavior(adsBean.getBusi_id() + "", 101, currentTime, outTime);
                                         }

                                         //  此判断用来打开新的webView 解决 不能全屏bug
                                         if (isView != null && isView.equals("1")) {

//                                             sendUserBehavior(adsBean.getBusi_id() + "", 101, currentTime);

                                             final View bigView = inflater.inflate(R.layout.layout_ads_big_webview, null);
                                             final WebView bigWeb = (WebView) bigView.findViewById(R.id.big_web);
                                             bigView.setBackgroundColor(context.getResources().getColor(R.color.transparent));

                                             wm.removeViewImmediate(mAdsLayerView);
                                             mAdsLayerView.setVisibility(View.GONE);
                                             mAdsLayerView = null;
                                             wm.addView(bigView, params);

                                             bigView.setFocusable(true);

                                             WebSettings settings1 = bigWeb.getSettings();
                                             settings1.setJavaScriptEnabled(true);
                                             settings1.setSupportZoom(true);

                                             ViewGroup.LayoutParams layoutParams = bigWeb.getLayoutParams();
                                             layoutParams.width = wm.getDefaultDisplay().getWidth();
                                             layoutParams.height = wm.getDefaultDisplay().getHeight();
                                             bigWeb.setLayoutParams(layoutParams);
                                             bigWeb.loadUrl(url);
                                             Log.d(TAG, "onKeyBackUrl: " + bigWeb.getUrl());
                                             bigWeb.addJavascriptInterface(new JavaScriptObjectContext(context, bigWeb, bigView, adsBean), "AppFunction");
                                             bigWeb.setFocusable(true);
                                             bigWeb.requestFocus();
                                             bigWeb.setWebViewClient(new WebViewClient() {
                                                 @Override
                                                 public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                                     Log.d(TAG, "shouldOverrideUrlLoadingtt: load : " + url);
                                                     return super.shouldOverrideUrlLoading(view, url);
                                                 }
                                             });

                                             bigWeb.setOnKeyListener(new View.OnKeyListener() {
                                                 @Override
                                                 public boolean onKey(View view, int i, KeyEvent keyEvent) {
                                                     Log.d(TAG, "onKey:=======  " + keyEvent);
                                                     if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                                                         Log.d(TAG, "shouldOverrideUrlLoadingtt: back: " + bigWeb.getUrl());
                                                         if (bigWeb.canGoBack()) {
                                                             bigWeb.goBack();
                                                         } else {
                                                             WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                                                             windowManager.removeViewImmediate(bigView);
                                                             bigView.setVisibility(View.GONE);

                                                         }
                                                     }
                                                     return false;
                                                 }
                                             });
                                             return true;
                                         }
                                         return super.shouldOverrideUrlLoading(view, url);
                                     }


                                     @Override
                                     public void onPageFinished(WebView view, String url) {
                                         super.onPageFinished(view, url);
                                         Log.d(TAG, "onPageFinished: 222: " + url + "  json: " + mJsonData.length());
//                                         Log.d(TAG, "showWebView  onPageFinished: 页面加载完毕－－ " + url.equals(finalUrlLoad));
                                         currentTime = System.currentTimeMillis() / 1000;
                                         if (mJsonData.length() > 0) {
                                             view.loadUrl("javascript:setImage('" + imgUrl + "')");
                                             view.loadUrl("javascript:setJsonData('" + mJsonData + "')");
                                         }
                                         ////冒泡时上报数据
                                         if (url.equals(finalUrlLoad)) {
                                             sendMessageReceived(adsBean);// 上报小web数据
                                         }
                                         if (showfirst == 1) {
                                             sendUserBehavior(adsBean.getBusi_id() + "", 1000, 0l, currentTime);//第二个页面加载完成
                                         }
                                     }
                                 }
        );

        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
//                mAdsLayerView.setFocusable(false);
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    String url2 = webView.getUrl();
                    Log.d(TAG, "onKeyUrl: " + url2);
//                    String back = CRequest.UrlPage(urlBack);
                    if (webPage == 0 && adsBean.getIs_back() == 1) {
                        return false;
                    }
                    String url1 = webView.getUrl();
                    String s = CRequest.UrlPage(url1);
                    Log.d(TAG, "onKeyWebPage: " + webPage + "   " + s + "  " + bigWebUrl);
                    if (s != null && s.equals(bigWebUrl) || webPage == 1) {
//                        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
//                        windowManager.removeViewImmediate(mAdsLayerView);
//                        mAdsLayerView.setVisibility(View.GONE);
//                        mAdsLayerView = null;

                        hideAdsDialog(context, adsBean);
                    } else if (webView.canGoBack() && webPage > 1) {
                        Log.d(TAG, "onKeyWebPage onKey:back ");
                        webView.goBack();
                        showfirst -= 2;
                        webPage--;
                    } else {
//                        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
//                        windowManager.removeViewImmediate(mAdsLayerView);
//                        mAdsLayerView.setVisibility(View.GONE);
//                        mAdsLayerView = null;
                        long outTime = System.currentTimeMillis() / 1000;
                        Log.d(TAG, "onKeyback : " + "  小web按返回键 ");
                        sendUserBehavior(adsBean.getBusi_id() + "", 103, currentTime, outTime);//  返回键上报
                        hideAdsDialog(context, adsBean);
                    }

                }
                return false;
            }
        });

        //-----------------------

        adsView.setKeyBoardCallback(new Callback() {
            @Override
            public void onStart(Object... o) {

            }

            @Override
            public void onProgress(Object... o) {

            }

            @Override
            public void onFinish(Object... o) {
//                if (adsBean.getIs_back()==0) {
                Log.d(TAG, "onFinish: adsView - ");
//                    AdsKeyEventHandler.onKeyOk(context, adsBean);
//                    hideAdsDialog(context, adsBean);
//                    //行为日志上报
//                    String user = Utils.getTvUserId(context);
//                    String localIp = Utils.getPhoneIp(context);
//                    try {
//                        Api.postUserBehaviors(context, adsBean.getMsg_id() + "", user, localIp, "10", adsBean.getBusi_id());
//                    } catch (Exception ex) {
//                    }
//                }
            }

            @Override
            public void onFail(Object... o) {
                if (adsBean.getIs_back() == 0) {
                    hideAdsDialog(context, adsBean);
                }
            }
        });

//        final TextView counterView = (TextView) mAdsLayerView.findViewById(R.id.counter);
//counterView.setBackgroundColor(getResources().getColor(R.color.green));
//        GradientDrawable drawable =(GradientDrawable)counterView.getBackground();
//        drawable.setColor(getResources().getColor(R.color.green));
//        counterView.setTextColor(Color.rgb(time_color.R,time_color.G,time_color.B));
/*
        new Thread(new Runnable() {
            @Override
            public void run() {

                while (adsBean.getShow_time() > 0) {
//                    counterView.setVisibility(View.VISIBLE);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
//                            mAdsLayerView.requestFocus();
//                            adsView.requestFocus();
//                            counterView.setText("" + adsBean.getShow_time());

                            adsBean.setShow_time(adsBean.getShow_time() - 1);
                            mAdsCounter = adsBean.getShow_time();
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if(mAdsLayerView==null){

                }else {
                    hideAdsDialog(context, adsBean);
                }
            }
        }).start();*/

    }

    /**
     * {
     * "SPID": "spa00071",
     * "backClass": "com.shareinfo.cphn.OrderActivity",
     * "backPackage": "com.shareinfo.cphn",
     * "busiId": "117",
     * "key": "8:2",
     * "notifyUrl": "http://10.255.25.152:8081/IPTVService/iptv/hunan/dinggouNotify.jsp?uid=1334861564813",
     * "optFlag": "VAS",
     * "price": "240000",
     * "productID": "productIDa3000000000000000000623",
     * "productName": "",
     * "sign": "a41da626079b1cd368d1d7b5813c0e3c",
     * "sourceId": "1008",
     * "sourceUrl": "http://www.baidu.com",
     * "transactionID": "spa0007120180613112940617321959070607117",
     * "userIDType": 0,
     * "userId": "136643152:433",
     * "userToken": "/4.4/31342.;26746:167507.:/62304"
     * }
     */

    private int imageToWeb = 0;

    /**
     * 显示图片
     *
     * @param context
     * @param wm
     * @param params
     * @param adsBean
     * @param localPath
     */
    public void showImage(final Context context, WindowManager wm, WindowManager.LayoutParams params, final AdsBean adsBean, final String localPath) {

        mAdsLayerView.setBackgroundColor(context.getResources().getColor(R.color.transparent));
        mAdsLayerView.setFocusable(true);
//        mAdsLayerView.requestFocus();
//        mAdsLayerView.getLocationOnScreen();
        wm.addView(mAdsLayerView, params);
        final AdsView adsView = (AdsView) mAdsLayerView.findViewById(R.id.adsView);
        final time_color time_color = JsonUtil.jsonStringToObject(adsBean.getTime_color(), time_color.class);
        final time_text time_text = JsonUtil.jsonStringToObject(adsBean.getTime_text(), time_text.class);
        adsView.requestFocus();
        final long currentTime = System.currentTimeMillis() / 1000;
        adsView.setKeyBoardCallback(new Callback() {
            @Override
            public void onStart(Object... o) {
                Log.d("myShow", "onStart: show");
            }

            @Override
            public void onProgress(Object... o) {

            }

            @Override
            public void onFinish(Object... o) {
//                int keyCode = (int)o[0];
                Log.d("myShow", "onFinish:  点击完毕～  ");
                hideAdsDialog(context, adsBean);
                //行为日志上报
                String user = Utils.getTvUserId(context);
                String localIp = Utils.getPhoneIp(context);

               /*
                    Intent intent = new Intent();
                    intent.setAction("com.iptv.maopao.mangopush");
                    Map<String, Object> map = new HashMap<>();
                    map.put("nns_special_id", "5b4ee2aacbc21d0985f10a5f3fef9f0d");
                    Gson gson2 = new GsonBuilder().enableComplexMapKeySerialization().create();

                    Map<String, Object> map1 = new HashMap<>();
                    map1.put("action", "2");
                    map1.put("versionCode", "8");
                    map1.put("data", map);

                    String jsonString = gson2.toJson(map1);

                    intent.putExtra("jsonData", jsonString);
                    sendBroadcast(intent);

                */

//                try {
//                    Api.postUserBehaviors(context, adsBean.getMsg_id() + "", user, localIp, "10", adsBean.getBusi_id());
                long outTime = System.currentTimeMillis() / 1000;
                sendUserBehavior(adsBean.getBusi_id() + "", 101, currentTime, outTime);
//                } catch (Exception ex) {
//                }
                if (adsBean.getSpecial_type() == AdsBean.ACTION_WEBVIEW) {
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
//                            Log.d(TAG, "run: myShow --  post_delay ");
                            imageToWeb = 1;
                            showWeb(adsBean);
                        }
                    }, 386);
                } else {
                    AdsKeyEventHandler.onKeyOk(context, adsBean);
                }

            }

            @Override
            public void onFail(Object... o) {

                Log.e("myShow", "fail this is brack");
                if (adsBean.getIs_back() == 0) {

                    long outTime = System.currentTimeMillis() / 1000;
                    Log.d(TAG, "onKeyback : " + "  图片按返回键 ");
                    sendUserBehavior(adsBean.getBusi_id() + "", 103, currentTime, outTime);//  返回键上报

                    hideAdsDialog(context, adsBean);

                }
            }
        });

        /**
         * 以下为测试代码
         */
        //------------------------------------------------------------------

//      adsView.setOnKeyListener(new View.OnKeyListener() {
//          @Override
//          public boolean onKey(View view, int i, KeyEvent keyEvent) {
//              Log.d("myShow", " onKey: "+keyEvent.getKeyCode());
//
//              if(keyEvent.getKeyCode()==KeyEvent.KEYCODE_BACK){
//                  //按了返回键
//
//              }else if(keyEvent.getKeyCode()==keyEvent.KEYCODE_DPAD_CENTER){
//
//              }
//              return true;
//          }
//      });
        //----------------------------------------------------
        GifImageView gifView = (GifImageView) mAdsLayerView.findViewById(R.id.gif);
        final TextView counterView = (TextView) mAdsLayerView.findViewById(R.id.counter);
        if (time_text != null) {
            counterView.setVisibility(time_text.time_visibility);
            if (counterView.getVisibility() == View.VISIBLE) {
                counterView.setText(String.valueOf(adsBean.getShow_time()));
//          RelativeLayout relativeLayout = (RelativeLayout) mAdsLayerView.findViewById(R.id.relativeLayout);
                counterView.setTextSize(time_text.time_size);
                counterView.setTextColor(Color.rgb(time_color.R, time_color.G, time_color.B));
                WidgetController.setLayout(counterView, time_text.time_x, time_text.time_y);
            } else {
                counterView.setVisibility(View.GONE);
            }
        }
        TextView tipsView = (TextView) mAdsLayerView.findViewById(R.id.textDesc);
        if (adsBean.getModel_id() == 2) {
            tipsView.setText(Html.fromHtml(getString(R.string.ads_rich_text)));
            tipsView.setTextColor(context.getResources().getColor(R.color.black));
        } else {
            tipsView.setTextColor(context.getResources().getColor(R.color.white));
        }

        try {
            //判断展示类型进行展示
            if (adsBean.getFile_type() == AdsBean.FILE_GIF) {
                GifDrawable gifFromResource = new GifDrawable(localPath);
                gifView.setImageDrawable(gifFromResource);
                sendMessageReceived(adsBean);//   冒出gif上报数据
            } else {
                Bitmap bitmap = BitmapFactory.decodeFile(localPath);
                Log.d(TAG, "showImage: show_picture   path:" + localPath);
                gifView.setImageBitmap(bitmap);
                if (mAdsLayerView != null) {
                    sendMessageReceived(adsBean);//  冒出图片上报数据
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        gifView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("myShow", "onClick: gifView  - -  - ");
                AdsKeyEventHandler.onKeyOk(context, adsBean);
                hideAdsDialog(context, adsBean);

                //行为日志上报
                String user = Utils.getTvUserId(context);
                String localIp = Utils.getPhoneIp(context);
//              showWeb(adsBean);

                try {
                    Api.postUserBehaviors(context, adsBean.getMsg_id() + "", user, localIp, "10", adsBean.getBusi_id());
                } catch (Exception ex) {
                    Log.i("iptv", "postUserBehaviors error");
                }
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {

                while (adsBean.getShow_time() > 0) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
//                            mAdsLayerView.requestFocus();
                            adsView.requestFocus();

                            counterView.setText("" + adsBean.getShow_time());
                            adsBean.setShow_time(adsBean.getShow_time() - 1);
                            mAdsCounter = adsBean.getShow_time();
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (imageToWeb == 0) {

                    hideAdsDialog(context, adsBean);
                    File file = new File(localPath);
                    if (file.exists() && file.isFile()) {
                        Log.d(TAG, "showImage:  delete---- " + file.delete());
                    }
                }

            }
        }).start();

    }

    private void showWeb(final AdsBean adsBean) {
        final int[] imgToWeb = {0};
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT | WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.alpha = 1.0f;
        params.format = PixelFormat.RGBA_8888;
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = 0;
        LayoutInflater inflater =
                (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mAdsLayerView = inflater.inflate(R.layout.layout_ads_webview, null);


        mAdsLayerView.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.transparent));
        mAdsLayerView.setFocusable(false);


        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        wm.addView(mAdsLayerView, params);
        final AdsView adsView = (AdsView) mAdsLayerView.findViewById(R.id.adsView);
        final WebView webView = (WebView) mAdsLayerView.findViewById(R.id.webview);
        ViewGroup.LayoutParams layoutParams = webView.getLayoutParams();
        layoutParams.height = wm.getDefaultDisplay().getHeight();
        layoutParams.width = wm.getDefaultDisplay().getWidth();
//        webView.setLayoutParams(layoutParams);
        WebSettings settings = webView.getSettings();
//        settings.setAllowContentAccess(true);
        settings.setJavaScriptEnabled(true);
//        settings.setBuiltInZoomControls(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);//js和android交互
        settings.setSupportZoom(true);//关闭zoom按钮
//        settings.setBuiltInZoomControls(false);//关闭zoom
//        settings.setUseWideViewPort(true);//设置webview自适应屏幕大小
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);//设置，可能的话使所有列的宽度不超过屏幕宽度
//        settings.setLoadWithOverviewMode(true);//设置webview自适应屏幕大小 这条命令 web  不能全屏－－
//        settings.setDomStorageEnabled(true);//设置可以使用localStorage
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        String url = adsBean.getSpecial_url();
//        Log.d(TAG, "showWeb: weburl:" + url);
        String userToken = Utils.getTvUserToken(this);
        DeviceInfoBean deviceData = com.iptv.hn.entity.Utils.getDeviceData(this, new DeviceInfoBean());
        if (url.contains("?")) {
            url = url + "&userId=" + Utils.getTvUserId(this) + "&userToken=" + userToken +
                    "&mac=" + deviceData.getMac_addr() + "&ip=" + deviceData.getIp_addr() +
                    "&payType=" + adsBean.getPay_type();
        } else {
            url = url + "?userId=" + Utils.getTvUserId(this)
                    + "&userToken=" + userToken + "&mac=" + deviceData.getMac_addr()
                    + "&ip=" + deviceData.getIp_addr() + "&payType=" + adsBean.getPay_type();
        }
        Log.d(TAG, "showWedUrl: weburl:" + url);
        webView.loadUrl(url);
        webView.requestFocus();
        final String finalUrl = url;
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                imgToWeb[0] += 1;
                Uri uri = Uri.parse(url);
                if (uri.getScheme().equals("action")) {

                    if (uri.getAuthority().equals("closeWeb")) {

                    }
                }
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String murl) {
                super.onPageFinished(view, murl);
                Log.d(TAG, "onPageFinished:--- " + imgToWeb[0]);
                if (imgToWeb[0] == 1) {
                    sendUserBehavior(adsBean.getBusi_id() + "", 1000, 0l, currentTime);
                }
            }
        });


//        webView.setWebChromeClient(new WebChromeClient() {
//            @Override
//            public void onProgressChanged(WebView view, int newProgress) {
//                super.onProgressChanged(view, newProgress);
//                Log.d(TAG, "onProgressChanged: " + newProgress);
//                if (newProgress >= 100) {
//                    // 切换页面
//                }
//            }
//
//        });
        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {

                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK && keyEvent.getAction() == KeyEvent.ACTION_UP) {
//                    WebBackForwardList webBackForwardList = webView.copyBackForwardList();
//                    Log.d(TAG, "onKey: size:"+webBackForwardList.getSize()+" index: "+webBackForwardList.getCurrentIndex());
                    if (webView.canGoBack()) {
                        webView.goBack();
                        webView.goBack();
                    } else {
                        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                        windowManager.removeViewImmediate(mAdsLayerView);
                        mAdsLayerView.setVisibility(View.GONE);
                        mAdsLayerView = null;
                        imageToWeb = 0;
                    }
                }
                return false;
            }
        });

//        mAdsLayerView.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View view, int i, KeyEvent keyEvent) {
//                Log.d(TAG, "myKeyEvent: mAdsLayerView  " + keyEvent.getKeyCode());
//
//                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK) {
//                    WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
//                    windowManager.removeViewImmediate(mAdsLayerView);
//                    mAdsLayerView.setVisibility(View.GONE);
//                    mAdsLayerView = null;
//                } else {
//
//                    mAdsLayerView.setFocusable(false);
//                    webView.setFocusable(true);
//                }
//                return false;
//            }
//        });

    }

    protected void hideAdsDialog(final Context context, final AdsBean adsBean) {
        webPage = 0;
        if (mAdsLayerView == null) {
            return;
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                File file = new File(localpathGif);
                if (file.exists()) {
                    file.delete();
                    Log.i("iptv", "delete message file " + file.getAbsolutePath());
                }
                PushMsgStack.deleteResources(adsBean);

                if (mAdsLayerView == null) {
                    return;
                }

                if (mAdsLayerView.getWindowToken() != null) {
                    WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                    windowManager.removeViewImmediate(mAdsLayerView);
                }
                mAdsLayerView.setVisibility(View.GONE);
                mAdsLayerView = null;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    protected void loadConfigure() {
        final PfUtil pfUtil = PfUtil.getInstance();
        pfUtil.init(this);
        long lastConfigTime = pfUtil.getLong("server_config_time", 0);

        //每天只更新配置文件一次
        long now = System.currentTimeMillis();
        long peroid = (now - lastConfigTime) / (1000 * 60 * 60); //hour
        if (peroid < 12) {
            return;
        }
    }


    protected void clearMeiadPlay(MediaPlayer mediaPlayer) {
        try {
            if (mediaPlayer != null) {
                synchronized (BootloaderService.class) {
                    if (mediaPlayer != null) {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.stop();
                        }
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getAppTVStoreVersionCode(Context context, String packageName,
                                         PackageManager pManager) {
        int versionCode = 0;
        try {
            // 获取软件版本号，对应AndroidManifest.xml下android:versionCode
            versionCode = context.getPackageManager().getPackageInfo(
                    packageName, 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    private boolean isPlay(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
        return audioManager.isMusicActive();
    }

    public class JiaoHu {
        @JavascriptInterface
        public void showAndroid() {
            Toast.makeText(BootloaderService.this, "js调用了android的方法", Toast.LENGTH_SHORT).show();
        }
    }

    WebViewClient webViewClient = new WebViewClient() {
        //
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Uri uri = Uri.parse(url);
            if (uri.getScheme().equals("action")) {
                if (uri.getAuthority().equals("closeWeb")) {
                    long outTime = System.currentTimeMillis() / 1000;
                    sendUserBehavior(adsBean.getBusi_id() + "", 102, currentTime, outTime);
//                    webPage = 0;
                } else if (uri.getAuthority().equals("openApp")) {

                }
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }
    };


    int findActivityCount = 0;

    private void getProcesses() {
        findActivityCount++;

        String activity_name = manager.getRunningTasks(1).get(0).topActivity.getClassName();
        //      com.hunantv.operator.ui.MainActivity
        Log.d(TAG, "getProcesses: 开启service ：Thread: " + Thread.currentThread().getName() + "   *    " + Contants.SERVICE_GET + "  count: " + findActivityCount + "  Top-activity_name :－ " + activity_name + "  started: "+started);
        if (started) {
            return;
        }
        if (activity_name.equals("com.hunantv.operator.ui.MainActivity") || activity_name.equals("com.starcor.hunan.SplashActivity")) {
            Contants.SERVICE_GET++;
            Log.d(TAG, "getProcesses: inHunan 检测到主页");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            start();
        } else if (findActivityCount == 18 && Contants.SERVICE_GET == 0 && !started) {
            Log.d(TAG, "getProcesses: " + "没检测到自启");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            start();
        }

        try {
            Thread.sleep(5000);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        getProcesses();

    }
}
