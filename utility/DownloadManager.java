package com.iptv.hn.utility;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/2/20.
 */

public class DownloadManager {

    /**
     * download the png, jpg or gif files to SDCard
     * @return download success or fail
     */
    public static boolean dl(final com.iptv.hn.utility.Callback callback, final String uri) {

        Log.i("downUri", "download file= " + uri);
        final OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(uri).build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFail();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;

                String fileName = Md5Util.MD5Encode(uri);
                Log.d("download", "onResponse: "+fileName);
                String SDPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    File file = new File(SDPath, fileName);
                    if (file.exists()) {
                        file.delete();
                    }
                    file.createNewFile();
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);
                    }
                    fos.flush();
                    Log.d("h_bl", "文件下载成功");

                    callback.onFinish(file.getAbsoluteFile());

                } catch (Exception ex) {
                    Log.d("h_bl", "文件下载Exception");
                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        return false;
    }
}
