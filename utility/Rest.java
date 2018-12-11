package com.iptv.hn.utility;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Rest {

	private OkHttpClient client = new OkHttpClient.Builder()
			.connectTimeout(12,TimeUnit.SECONDS)
			.readTimeout(12,TimeUnit.SECONDS)
			.writeTimeout(12,TimeUnit.SECONDS).build();

	private RequestBody formBody;

	private HashMap<String, Object> mParams = new HashMap<String, Object>();

	public static final int STATUS_OK = 0, STATUS_QUERY_ERROR = 2, STATUS_PARAM_ERROR = 1;

//	private String mDomain, mMethod;

	private String mUrl;

	public Rest(String domain, String method) {
//		this.mDomain = domain;
//		this.mMethod = method;
		mUrl = domain +  method;
	}

	public Rest(String url) {
		mUrl = url;
	}

	public Rest addParam(String paramName, Object paramValue) {
		mParams.put(paramName, paramValue);
		//builder.add(paramName, paramValue);
		return this;
	}

	Handler handler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

		}

	};

	private Request callRestfulApiByGetMethod() {

		StringBuilder buffer = new StringBuilder();
		Iterator<String> keys = mParams.keySet().iterator();
		while(keys.hasNext()) {
			String key = keys.next();
			String value = mParams.get(key).toString();
			try {
				buffer.append(key + "=" + URLEncoder.encode(value, "utf-8") + "&");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		mParams.clear();

		String url = mUrl + "?" + buffer.toString();
		if (url.contains("?")) {
			url = mUrl  + buffer.toString();
		}else{
			url = mUrl  +"?"+ buffer.toString();
		}
		if(url.charAt(url.length()-1)=='&'){
			url = url.substring(0,url.length()-1).trim();
		}
		Log.i("iptv", "http get url = " + url);

		Request request = new Request.Builder()
				.url(url)
				.build();

		return request;
	}

	/**
	 * get method
	 * @param callback
	 */
	public void get(final HttpCallback callback) {
		Request request = callRestfulApiByGetMethod();
		httpCallback(request, callback);
	}

	protected void httpCallback(Request request, final HttpCallback callback) {
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onResponse(Call arg0, Response response) throws IOException {
				String rawJsonObj = response.body().string();

				Log.i("iptv", "url(" + mUrl +  ")response context = " + rawJsonObj);

				if (response.isSuccessful()) {
					try {
						final JSONObject raw = new JSONObject(rawJsonObj);
//						final int state = raw.getInt("code");
//						final String msg = raw.has("msg") ? raw.getString("msg") : "";

						Handler handler = new Handler(Looper.getMainLooper()) {

							@Override
							public void handleMessage(Message message) {
								// TODO Auto-generated method stub
								super.handleMessage(message);

								if (message.what == STATUS_OK) {
									try {
										callback.onSuccess(raw, message.what, "");
									} catch (JSONException e) {
										Log.e("test1",e.toString());
										e.printStackTrace();
										callback.onError();
									}
								} else if (message.what == STATUS_PARAM_ERROR || message.what == STATUS_QUERY_ERROR) {
									callback.onFailure(raw, message.what, "");
								} else {
									Log.e("test2",message.toString());
									callback.onError();
								}
							}

						};

						handler.sendEmptyMessage(0);

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.e("test3",e.toString());
					}

				} else {
					Handler handler = new Handler(Looper.getMainLooper()) {
						@Override
						public void handleMessage(Message message) {
							// TODO Auto-generated method stub
							super.handleMessage(message);
							callback.onError();
							Log.e("test4",message.toString());
						}
					};
					handler.sendEmptyMessage(0);
				}
			}


			@Override
			public void onFailure(final Call arg0, final IOException arg1) {
//				Log.d("httpCall", "onFailure: 请求错误:"+arg1);
				Handler handler = new Handler(Looper.getMainLooper()) {
					@Override
					public void handleMessage(Message message) {
						// TODO Auto-generated method stub
						super.handleMessage(message);
						callback.onError();
						arg1.printStackTrace();
					}
				};
				handler.sendEmptyMessage(0);
			}



		});
	}


	/**
	 * @param
	 */
	public void get() {
		Request request = callRestfulApiByGetMethod();
		try {
			client.newCall(request).execute();
		} catch (IOException e) {
			Log.e("test6",e.getMessage());
			e.printStackTrace();
		}

	}

	/**
	 * post method
	 */
	public void post(final HttpCallback callback) {

		//FIXME 1
		//		String contentType = "application/x-www-form-urlencoded";
		String contentType = "application/json";
		MediaType MEDIA_TYPE_TEXT = MediaType.parse(contentType);

		//		StringBuffer buf = new StringBuffer();
		//		Iterator<String> keys = mParams.keySet().iterator();
		//		while(keys.hasNext()) {
		//			String key = keys.next();
		//			String value = mParams.get(key).toString();
		//			buf.append(key + "=" + value + "&");
		//		}
		//		mParams.clear();
		//
		//		formBody = RequestBody.create(MEDIA_TYPE_TEXT, buf.toString());

		JSONObject json = new JSONObject();
		Iterator<String> keys = mParams.keySet().iterator();
		while(keys.hasNext()) {
			String key = keys.next();
			Object obj = mParams.get(key);

			System.out.println("api set " + key + " = " + obj);

			if (obj instanceof JSONArray) {
				try {
					json.put(key, obj);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				String value = mParams.get(key).toString();
				try {
					json.put(key, value);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}


		}
		mParams.clear();

		formBody = RequestBody.create(MEDIA_TYPE_TEXT, json.toString());

		//FIXME 2

		//		okhttp3.FormBody.Builder builder = new okhttp3.FormBody.Builder();
		//		Iterator<String> keys = mParams.keySet().iterator();
		//		while(keys.hasNext()) {
		//			String key = keys.next();
		//			String value = mParams.get(key).toString();
		//			builder.add(key, value);
		//		}
		//
		//		mParams.clear();
		//
		//		formBody = builder.build();

		//FIXME end

		String url = mUrl;
		Log.i("2026", "http post url = " + url + "\nContent = " + formBody.toString());

		Request request = new Request.Builder()
				.url(url)
				.header("Content-Type", contentType)
				.post(formBody)
				.build();

		httpCallback(request, callback);

	}


	public void delete(final HttpCallback callback) {

		okhttp3.FormBody.Builder builder = new okhttp3.FormBody.Builder();

		Iterator<String> keys = mParams.keySet().iterator();
		while(keys.hasNext()) {
			String key = keys.next();
			String value = mParams.get(key).toString();
			builder.add(key, value);
		}

		mParams.clear();

		formBody = builder.build();
		String url = mUrl;
		Log.i("iptv", "http post url = " + url);

		Request request = new Request.Builder()
				.url(url)
				.delete(formBody)
				.build();

		httpCallback(request, callback);

	}



	public void put(final HttpCallback callback) {

		String contentType = "application/json";
		MediaType MEDIA_TYPE_TEXT = MediaType.parse(contentType);
		JSONObject json = new JSONObject();
		Iterator<String> keys = mParams.keySet().iterator();
		while(keys.hasNext()) {
			String key = keys.next();
			Object obj = mParams.get(key);
			if (obj instanceof JSONArray) {
				try {
					json.put(key, obj);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}  else {
				String value = mParams.get(key).toString();
				try {
					json.put(key, value);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		mParams.clear();

		formBody = RequestBody.create(MEDIA_TYPE_TEXT, json.toString());

		String url = mUrl;
		Log.i("2026", "http post url = " + url);

		Request request = new Request.Builder()
				.url(url)
				.header("Content-Type", contentType)
				.put(formBody)
				.build();

		httpCallback(request, callback);

	}

	public void postFile(final HttpCallback callback) {

		MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
		MediaType TEXT_TYPE_PLAIN = MediaType.parse("text/plain");

		okhttp3.MultipartBody.Builder builder = new okhttp3.MultipartBody.Builder().
				setType(okhttp3.MultipartBody.FORM);

		Iterator<String> keys = mParams.keySet().iterator();
		while(keys.hasNext()) {
			String key = keys.next();
			Object value = mParams.get(key);

			if (value instanceof String) {
				builder.addPart(
						okhttp3.Headers.of("Content-Disposition", "form-data; name=\"" + key + "\""),
						RequestBody.create(null, value.toString()));
				//builder.addFormDataPart(key, "", RequestBody.create(TEXT_TYPE_PLAIN, value.toString())); 
			} else if (value instanceof File) {
				File file = (File) value;
				try {
					SDcardUtil.compressBitmap(file.getAbsolutePath(), key);
				} catch (Exception e) {
					e.printStackTrace();
				}
				builder.addFormDataPart(key, "", RequestBody.create(MEDIA_TYPE_PNG, new File(SDcardUtil.getCompressFile(key))));
			}

		}

		mParams.clear();

		String url = mUrl;

		Request request = new Request.Builder()
				.url(url)
				.post(builder.build())
				.build();

		httpCallback(request, callback);

	}

}
