package com.yl.ylcommon.utlis;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
        Log.d("intent广播", "showSearchResults: " + intents);
        context.sendBroadcast(intents);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e("Navigation", "通过 URL Scheme 启动导航失败", e);
        }
    }


    /**
     * 启动高德导航（支持途经点）
     *
     * @param context   上下文
     * @param startName 起点名称（可选）
     * @param startLat  起点纬度
     * @param startLon  起点经度
     * @param endName   终点名称
     * @param endLat    终点纬度
     * @param endLon    终点经度
     * @param viaPoints 途经点列表（格式：lat1,lng1,name1|lat2,lng2,name2）
     */
    public static void startNavigationWithWayPoint(Context context,
                                                   String startName, double startLat, double startLon,
                                                   String endName, double endLat, double endLon,
                                                   String viaPoints) {
        Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
        // 起点参数（如果不需要起点可省略）
        if (startName != null) {
            intent.putExtra("EXTRA_S_POI_ID", startName);
            intent.putExtra("EXTRA_SLAT", startLat);
            intent.putExtra("EXTRA_SLON", startLon);
        }
        intent.putExtra("EXTRA_DNAME", endName);
        intent.putExtra("EXTRA_DLAT", endLat);
        intent.putExtra("EXTRA_DLON", endLon);

        // 途经点处理（最多支持16个途经点）
        if (viaPoints != null && !viaPoints.isEmpty()) {
            String[] viaArray = viaPoints.split("\\|");
            String[] point = viaArray[0].split(",");
            if (point.length == 3) {
                intent.putExtra("EXTRA_FMID_POI_ID", point[2]);
                intent.putExtra("EXTRA_FMIDLAT", point[0]);
                intent.putExtra("EXTRA_FMIDLON", point[1]);
            }
        }
        try {
            intent.putExtra("EXTRA_DEV",0);
            intent.putExtra("EXTRA_M",-1);
            intent.putExtra("EXTRA_NEWMODE",9);
            context.sendBroadcast(intent);
        } catch (Exception e) {
        }
    }

}

