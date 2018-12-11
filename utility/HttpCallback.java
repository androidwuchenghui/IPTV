package com.iptv.hn.utility;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/3/11.
 */

public interface HttpCallback {
    public void onSuccess(JSONObject rawJsonObj, int state, String msg) throws JSONException;

    public void onFailure(JSONObject rawJsonObj, int state, String msg);

    public void onError();
}
