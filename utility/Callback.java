package com.iptv.hn.utility;

/**
 * Created by Administrator on 2017/2/24.
 */

public interface Callback {

    public void onStart(Object... o);

    public void onProgress(Object... o);

    public void onFinish(Object... o);

    public void onFail(Object... o);
}
