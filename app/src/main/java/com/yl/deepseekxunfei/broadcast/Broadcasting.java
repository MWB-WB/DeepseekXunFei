package com.yl.deepseekxunfei.broadcast;

import static android.provider.Settings.System.getInt;
import static android.provider.Settings.System.getString;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
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
        Intent intent = new Intent();
        Log.d("打开", "top: 打开高德");
        intent.setComponent(new ComponentName("com.autonavi.amapauto","com.autonavi.amapauto.MainMapActivity"));
//        intent.setPackage("com.autonavi.amapauto");
        try {
            startActivity(context,intent,null);
        } catch (ActivityNotFoundException e) {
            Log.d("Navigation", "成功启动高德地图");
            throw new RuntimeException(e);
        }
//        Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
//        intent.setPackage("com.autonavi.amapauto");
//        intent.putExtra("KEY_TYPE", 10070);
//        intent.putExtra("EXTRA_TYPE", 0); // 0=家页面，1=公司页面，2=收藏夹，3=设置页，4=地图数据下载页，5=U盘更新页
//        context.sendBroadcast(intent);
    }
}
