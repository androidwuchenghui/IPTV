package com.iptv.hn.service;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.iptv.hn.utility.PfUtil;

import static android.content.Context.ACTIVITY_SERVICE;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiverMaopao";


    public BootReceiver() {

    }

    @Override
    public void onReceive(final Context context, Intent intent) {

        if (!isServiceRunning(context)) {

            PfUtil pfUtil = PfUtil.getInstance();
            pfUtil.init(context);


            Log.d(TAG, "onReceiveBootReceiver: 收到开机广播－－－－openBox － －");
            Intent intentService = new Intent(context, BootloaderService.class);
            intentService.setAction("com.iptv.hn.service");
            intentService.setPackage("com.iptv.maopao");
            context.startService(intentService);
        }

    }

    private boolean isServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.iptv.hn.service.BootloaderService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


}
