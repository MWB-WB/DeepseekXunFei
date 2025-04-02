package com.yl.deepseekxunfei;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

//com.autonavi.amapauto 高德包名
// 导航工具类
public class AmapNavigator {

    /**
     * 调用高德车机版8.1.0进行导航
     *
     * @param context 上下文
     * @param poiName 目的地名称（如"中国航油裕乐丰加油站"）
     */
    public static void startNavigationByUri(Context context, String poiName, double lat, double lon) {
        Uri uri = Uri.parse("androidauto://navi?lat=" + lat + "&lon=" + lon + "&name=" + Uri.encode(poiName));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.autonavi.amapauto");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //发送广播通知launcher MapView进行导航规划
        Intent intents = new Intent("com.yl.deepseek.start.navigation");
        intents.putExtra("latitude", lat);
        intents.putExtra("longitude", lon);
        Log.d("intent广播", "showSearchResults: "+intents);
        context.sendBroadcast(intents);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e("Navigation", "通过 URL Scheme 启动导航失败", e);
        }
    }
}

