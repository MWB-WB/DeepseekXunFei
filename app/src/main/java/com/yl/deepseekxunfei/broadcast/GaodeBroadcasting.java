package com.yl.deepseekxunfei.broadcast;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.yl.deepseekxunfei.R;

public class GaodeBroadcasting {
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

    public static  void goHomeToWord(int code,Context context) {
        Intent intent = new Intent();
        String appName = context.getString(R.string.app_name);
        intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
        intent.putExtra("KEY_TYPE", 10040);
        intent.putExtra("DEST", code);//0 回家；1 回公司(int
        intent.putExtra("IS_START_NAVI", 0);//是否直接开始导航 0 是；1 否
        intent.putExtra("SOURCE_APP", appName);
        context.sendBroadcast(intent);
    }

}
