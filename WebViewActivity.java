package com.iptv.hn;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.iptv.hn.entity.AdsBean;
import com.iptv.hn.entity.DeviceInfoBean;
import com.iptv.hn.utility.HttpCallback;
import com.iptv.hn.utility.JavaScriptObject;
import com.iptv.hn.utility.MACUtils;
import com.iptv.hn.utility.Rest;
import com.iptv.hn.utility.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/2/24.
 */

public class WebViewActivity extends AppCompatActivity {

    private String mUrl;

    private String mBusiId;

    private WebView mWebView;

    private long mInTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        mUrl = getIntent().getStringExtra("url");
        mBusiId = getIntent().getStringExtra("busi_id");

        mInTime = System.currentTimeMillis()/1000;

        setContentView(R.layout.layout_webview);

        mWebView = (WebView) findViewById(R.id.webview1);
        final TextView loadFailView = (TextView) findViewById(R.id.loadFailView);

        final ProgressBar pg1=(ProgressBar) findViewById(R.id.progressBar1);

        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                final String userToken = Utils.getTvUserToken(WebViewActivity.this);
                final DeviceInfoBean deviceData = com.iptv.hn.entity.Utils.getDeviceData(WebViewActivity.this, new DeviceInfoBean());
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                mWebView.setVisibility(View.GONE);
                loadFailView.setVisibility(View.VISIBLE);
            }
        });
        WebSettings seting = mWebView.getSettings();
        seting.setAllowContentAccess(true);
        seting.setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new JavaScriptObject(this,mWebView),"AppFunction");
        mWebView.requestFocus();
        mWebView.setFocusable(true);
        mWebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if(newProgress==100){
                    pg1.setVisibility(View.GONE);
                }
                else{
                    pg1.setVisibility(View.VISIBLE);
                    pg1.setProgress(newProgress);
                }
            }
        });
        mWebView.loadUrl(mUrl);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

//        mWebView.dispatchKeyEvent(event);

        if ((keyCode == KeyEvent.KEYCODE_BACK) && !mWebView.canGoBack()) {
            sendUserBehavior();
        }

        return super.onKeyDown(keyCode, event);
    }

    protected void sendUserBehavior() {
        String url = Contants.Rest_api_v2 + "mp_push/behavior?";
        Rest restApi = new Rest(url);
        restApi.addParam("account", Utils.getTvUserId(this));
        restApi.addParam("timestamp", System.currentTimeMillis()/1000); //

        restApi.addParam("busi_id", mBusiId);
        restApi.addParam("ip_addr", Utils.getPhoneIp(this));
        restApi.addParam("mac_addr", MACUtils.getLocalMacAddressFromBusybox());
        restApi.addParam("read_type", 10); //默认：0（到达用户,小窗口弹出时）； 10（用户点击确定键进入大窗口页面）；

        long outTime = System.currentTimeMillis()/1000;
        restApi.addParam("in_webTime", mInTime); //当read_type=10时，该字段必传，用户进入web页面时间戳，单位秒
        restApi.addParam("stay_time", outTime-mInTime); //当read_type=10时，该字段必传，用户在web页面停留时长，（单位秒）

        restApi.post(new HttpCallback() {
            @Override
            public void onSuccess(JSONObject rawJsonObj, int state, String msg) throws JSONException {

            }

            @Override
            public void onFailure(JSONObject rawJsonObj, int state, String msg) {

            }

            @Override
            public void onError() {

            }
        });

    }
}
