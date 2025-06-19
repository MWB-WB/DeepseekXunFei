package com.yl.ylcommon.utlis;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {

    private static Toast mToast;
    private static Object mLock = new Object();

    public static void show(Context context, String text) {
        synchronized (mLock) {
            if (mToast != null) {
                mToast.cancel();
                mToast = null;
            } else {
                mToast = new Toast(context);
                mToast.setText(text);
                mToast.setDuration(Toast.LENGTH_SHORT);
                mToast.show();
            }
        }
    }

}
