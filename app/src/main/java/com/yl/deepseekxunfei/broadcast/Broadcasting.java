package com.yl.deepseekxunfei.broadcast;

import static android.provider.Settings.System.getInt;
import static android.provider.Settings.System.getString;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;


import com.yl.deepseekxunfei.R;
import com.yl.deepseekxunfei.utlis.ContextHolder;

public class Broadcasting {
    /**
     * @param context 上下文对象
     */
    public static void BroadcastingActivate(int code, Context context) {
        Intent intent = new Intent();
        intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
        intent.putExtra("KEY_TYPE", 10070);
        intent.putExtra("EXTRA_TYPE", code);
        context.sendBroadcast(intent);
    }

    public static void top(Context context) {
        // 获取应用名称（来自strings.xml）
        String appName =context.getString(R.string.app_name);
        Intent intent = new Intent();
        intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
        intent.putExtra("KEY_TYPE", 10034);
        intent.putExtra("SOURCE_APP", appName);
        // Android 8.0+必须设置包名
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.setPackage("com.autonavi.minimap");
        }
        context.sendBroadcast(intent);
    }
}
