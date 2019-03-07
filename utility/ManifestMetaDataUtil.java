package com.iptv.hn.utility;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;


/**
 *
 * 获取Mainfest文件中定义的元数据的值 工具类
 *
 * @author ligao

 */
public class ManifestMetaDataUtil {

	private ManifestMetaDataUtil() {

	}

	private static Object readKey(Context context, String keyName) {

		try {

			ApplicationInfo appi = context.getPackageManager()
					.getApplicationInfo(context.getPackageName(),
							PackageManager.GET_META_DATA);

			Bundle bundle = appi.metaData;

			Object value = bundle.get(keyName);

			return value;

		} catch (NameNotFoundException e) {
			return null;
		}

	}

	public static int getInt(Context context, String keyName) {
		return (Integer) readKey(context, keyName);
	}

	public static String getString(Context context, String keyName) {
		return (String) readKey(context, keyName);
	}

	public static Boolean getBoolean(Context context, String keyName) {
		return (Boolean) readKey(context, keyName);
	}

	public static Object get(Context context, String keyName) {
		return readKey(context, keyName);
	}

	/**
	 * 获取当前版本
	 * @param context
	 * @return
	 */
	public static int getVersionCode(Context context) {
		PackageInfo packageInfo = null;
		try {
			packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return packageInfo.versionCode;
	}

	public static String getVersionName(Context context) {
		PackageInfo packageInfo = null;
		try {
			packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return packageInfo.versionName;
	}
}
