/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.iptv.hn;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.iptv.hn.service.BootloaderService;

/*
 * MainActivity class that loads MainFragment
 */
public class MainActivity extends Activity implements View.OnClickListener {

    private View mClickView;
// 测试
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.layout_test);

        mClickView = findViewById(R.id.test);
        mClickView.setOnClickListener(this);

        Intent intentService = new Intent(this, BootloaderService.class);
        startService(intentService);

        finish();

        /*Api.getConfigure(this, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject rawJsonObj, int state, String msg) throws JSONException {
                Intent intentService = new Intent(MainActivity.this, TcpService.class);
                startService(intentService);

                finish();
            }

            @Override
            public void onFailure(JSONObject rawJsonObj, int state, String msg) {

            }

            @Override
            public void onError() {

            }
        });*/


//        showTestAds();


//        String url = "http://www.baidu.com";
//        Intent intent = new Intent();
//        intent.putExtra("url", url);
//        intent.setClass(this, WebViewActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);

    }

    @Override
    public void onClick(View v) {
//        Toast.makeText(this, "test", Toast.LENGTH_SHORT).show();

//        Intent intent = new Intent("com.iptv.ads");
//        sendBroadcast(intent);

//        finish();
//        Api.getConfigure(this);
//        Api.postUserBehaviors("03e23453", "admin", "192.168.10.29");

//        AdsBean ads2 = new AdsBean();
//        ads2.setModel_id(2);
//        ads2.setMsg_id(2);
//        ads2.setSpecial_type(AdsBean.ACTION_APP);
//        ads2.setSpecial_url("http://www.baidu.com");
//        ads2.setDown_type(AdsBean.DOWNLOAD_FROM_SERVER);
//        ads2.setDown_url("http://ftp-apk.pconline.com.cn/80c6e0508dba2e354041a209265c10b3/pub/download/201010/pconline1482411644018.apk");
//        ads2.setShow_time(6);
//        ads2.setFile_type(AdsBean.FILE_GIF);
//        ads2.setFile_url("http://images.17173.com/2014/news/2014/03/10/g0310if03.gif");
//        ads2.setPriority_level(2);
//
//        AdsKeyEventHandler.onKeyOk(this, ads2);

//        String user = Utils.getTvUserId(this);
//        Utils.showToast(this, user);


/*String url = "http://124.232.135.239:8080/sm_iptv/thematicactivity/jsp/smspecial/special_iptvsm.jsp?userId=" + Utils.getTvUserId(this);
//        String url = "http://124.232.135.239:8088/special_iptvsm_test.jsp?userId=" + Utils.getTvUserId(this);
        AdsKeyEventHandler.openWebView(this, url);*/
//        Intent intent = new Intent();
//        intent.setAction("android.intent.action.view");
//        Uri uri = Uri.parse(url);
//        intent.setData(uri);
//        startActivity(intent);

    }

}
