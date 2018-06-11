package io.agora.audiocustomization.util;

import android.support.annotation.IdRes;
import android.widget.Toast;

import io.agora.audiocustomization.MyApplication;

/**
 * Created by ChengleiQiu on 2018/1/17.
 */

public class ToastUtil {
    public static void showLong(int stringInt) {
        Toast.makeText(MyApplication.getInstance(), stringInt, Toast.LENGTH_LONG).show();
    }

    public static void showLong(String str) {
        Toast.makeText(MyApplication.getInstance(), str, Toast.LENGTH_LONG).show();
    }
    public static void showShort(String str) {
        Toast.makeText(MyApplication.getInstance(), str, Toast.LENGTH_SHORT).show();
    }
}
