package io.agora.audiocustomization.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.agora.audiocustomization.MyApplication;
import io.agora.rtc.Constants;

/**
 * Created by ChengleiQiu on 2018/1/17.
 */

public class SpUtils {

    public static final String IS_FIRST_LAUNCH = "is_first_launch";

    public static final String IS_RAW_FILES_READY = "is_raw_files_ready";

    public static final String CLIENT_ROLE = "client_role";
    public static final String USER_ID = "uid";

    public static final String MUSIC_EDIT_URI_LIST = "music_edit_uri_list";
    public static final String PRIVATE_PARAMETER_LIST = "private_parameter_list";

    public static final String PF_APP_TO_APP_VALUE = "0";
    public static final String PF_APP_TO_SDK_VALUE = "1";
    public static final String PF_SDK_TO_APP_VALUE = "2";
    public static final String PF_SDK_TO_SDK_VALUE = "3";

    public static void putStringSet(String key, Set<String> value) {
        if (value == null)
            remove(key);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance());

        sp.edit().putStringSet(key, value).apply();
    }

    public static Set<String> getStringSet(String key, Set<String> defValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance());
        return sp.getStringSet(key, defValue);
    }

    public static void put(String key, Object value) {
        if (value == null)
            remove(key);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance());

        if (value instanceof Boolean) {
            sp.edit().putBoolean(key, (boolean) value).apply();
        } else if (value instanceof Integer) {
            sp.edit().putInt(key, (int) value).apply();
        } else if (value instanceof Long) {
            sp.edit().putLong(key, (long) value).apply();
        } else if (value instanceof String) {
            sp.edit().putString(key, (String) value).apply();
        } else if (value instanceof Float) {
            sp.edit().putFloat(key, (float) value).apply();
        }
    }

    public static void remove(String key) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance());

        sp.edit().remove(key).apply();
    }

    public static void put(@StringRes int key, Object value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance());

        if (value instanceof Boolean) {
            sp.edit().putBoolean(getString(key), (boolean) value).apply();
        } else if (value instanceof Integer) {
            sp.edit().putInt(getString(key), (int) value).apply();
        } else if (value instanceof Long) {
            sp.edit().putLong(getString(key), (long) value).apply();
        } else if (value instanceof String) {
            sp.edit().putString(getString(key), (String) value).apply();
        } else if (value instanceof Float) {
            sp.edit().putFloat(getString(key), (float) value).apply();
        }
    }

    public static Object get(String key, Object defValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance());

        if (defValue instanceof Boolean) {
            return sp.getBoolean(key, (Boolean) defValue);
        } else if (defValue instanceof Integer) {
            return sp.getInt(key, (int) defValue);
        } else if (defValue instanceof Long) {
            return sp.getLong(key, (long) defValue);
        } else if (defValue instanceof String) {
            return sp.getString(key, (String) defValue);
        } else if (defValue instanceof Float) {
            return sp.getFloat(key, (float) defValue);
        }

        return defValue;
    }

    public static Object get(@StringRes int key, Object defValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance());

        if (defValue instanceof Boolean) {
            return sp.getBoolean(getString(key), (Boolean) defValue);
        } else if (defValue instanceof Integer) {
            return sp.getInt(getString(key), (int) defValue);
        } else if (defValue instanceof Long) {
            return sp.getLong(getString(key), (long) defValue);
        } else if (defValue instanceof String) {
            return sp.getString(getString(key), (String) defValue);
        } else if (defValue instanceof Float) {
            return sp.getFloat(getString(key), (float) defValue);
        }

        return defValue;
    }

    private static String getString(@StringRes int key) {
        return MyApplication.getInstance().getString(key);
    }

    /**
     * 取出List<String>
     *
     * @param key
     *            List<String> 对应的key
     * @return List<String>
     */
    public static List<String> getStrListValue(String key) {
        List<String> strList = new ArrayList<String>();
        int size = (int) get(key + "size", 0);
        //Log.d("sp", "" + size);
        for (int i = 0; i < size; i++) {
            strList.add((String) get(key + i, ""));
        }
        return strList;
    }

    /**
     * 清空List<String>所有数据
     *
     * @param key
     *            List<String>对应的key
     */
    public static void removeStrList(String key) {
        int size = (int) get(key + "size", 0);
        if (0 == size) {
            return;
        }
        remove(key + "size");
        for (int i = 0; i < size; i++) {
            remove(key + i);
        }
    }


    /**
     * 存储List<String>
     *
     * @param key
     *            List<String>对应的key
     * @param strList
     *            对应需要存储的List<String>
     */
    public static void putStrListValue(String key,
                                       List<String> strList) {
        if (null == strList) {
            return;
        }
        // 保存之前先清理已经存在的数据，保证数据的唯一性
        removeStrList(key);
        int size = strList.size();
        put(key + "size", size);
        for (int i = 0; i < size; i++) {
            put(key + i, strList.get(i));
        }
    }

}
