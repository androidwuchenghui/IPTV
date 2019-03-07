package com.iptv.hn.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.iptv.hn.Contants;

/**
 * Created by Administrator on 2017/11/6.
 */

public class MangoLiveReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {

        String action = intent.getAction();
        //0进入直播，1离开直播，2持续，3换台

        //int s = intent.getExtras().getInt("status");

        int status = 0;
        try {
            status = intent.getIntExtra("status", 0);
            Log.d("mangguo", "onReceive: " + status);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (status == 0 || status == 2 || status == 3) {
            Contants.isInMangoLiving = true;
        } else if (status == 1) {
            Contants.isInMangoLiving = false;
        }
     if(liveListener!=null){
            liveListener.onLive(status);
     }
//        if ( !isServiceRunning(context)) {
//                Api.getConfigure(context, new HttpCallback() {
//                    @Override
//                    public void onSuccess(JSONObject rawJsonObj, int state, String msg) throws JSONException {
//                        Intent intentService = new Intent(context, TcpService.class);
//                        context.startService(intentService);
//
//                    }
//
//                    @Override
//                    public void onFailure(JSONObject rawJsonObj, int state, String msg) {
//
//                    }
//
//                    @Override
//                    public void onError() {
//
//                    }
//                });
//
//        }

    }


//    private boolean isServiceRunning(Context context) {
//        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
//        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//            if ("com.iptv.hn.service.TcpService".equals(service.service.getClassName())) {
//                return true;
//            }
//        }
//        return false;
//    }
    private static LiveListener liveListener;

    public interface LiveListener{
        void onLive(int status);
    }

    public static void setLiveListener(LiveListener mLiveListener){
        liveListener=mLiveListener;
    }
}
