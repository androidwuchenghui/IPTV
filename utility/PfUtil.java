package com.iptv.hn.utility;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class PfUtil {

	private static final Method APPLY_METHOD = findApplyMethod();

	private SharedPreferences mSharedPreferences;

	private SharedPreferences.Editor mEdit;

	private static final PfUtil INSTANCE = new PfUtil();

	private static final String FILE_PREFERENCE = "iptvplus.data";

	private PfUtil() {

	}

	public static PfUtil getInstance() {
		return INSTANCE;
	}

	public void init(Context context) {
		if (mSharedPreferences == null) {
			mSharedPreferences = context.getSharedPreferences(FILE_PREFERENCE,
					0);
			mEdit = mSharedPreferences.edit();
		}
	}

	public Map<String, ?> getAll() {
		return mSharedPreferences.getAll();
	}

	public String getString(String key, String defValue) {
		return mSharedPreferences.getString(key, defValue);
	}

	public Long getLong(String key, long defValue) {
		return mSharedPreferences.getLong(key, defValue);
	}

	public Float getFloat(String key, float defValue) {
		return mSharedPreferences.getFloat(key, defValue);
	}

	public Boolean getBoolean(String key, Boolean defValue) {
		return mSharedPreferences.getBoolean(key, defValue);
	}

	public Integer getInt(String key, Integer defValue) {
		return mSharedPreferences.getInt(key, defValue);
	}

	public void putInt(String key, Integer value) {
		mEdit.putInt(key, value);
		apply(mEdit);
	}

	public void putString(String key, String value) {
		mEdit.putString(key, value);
		apply(mEdit);
	}

	public void putLong(String key, long value) {
		mEdit.putLong(key, value);
		apply(mEdit);
	}

	public void putBoolean(String key, Boolean value) {
		mEdit.putBoolean(key, value);
		apply(mEdit);
	}

	public void putFloat(String key, float value) {
		mEdit.putFloat(key, value);
		apply(mEdit);
	}

	public void remove(String key) {
		mEdit.remove(key);
		apply(mEdit);
	}

	@SuppressWarnings("unchecked")
	private static Method findApplyMethod() {
		try {
			@SuppressWarnings("rawtypes")
			Class cls = SharedPreferences.Editor.class;
			return cls.getMethod("apply");
		} catch (NoSuchMethodException unused) {
			unused.printStackTrace();
		}
		return null;
	}

	public void apply() {
		apply(mEdit);
	}

	public static void apply(SharedPreferences.Editor editor) {
		if (APPLY_METHOD != null) {
			try {
				APPLY_METHOD.invoke(editor);
				return;
			} catch (InvocationTargetException unused) {
				unused.printStackTrace();
			} catch (IllegalAccessException unused) {
				unused.printStackTrace();
			}
		}
		editor.commit();
	}
}
