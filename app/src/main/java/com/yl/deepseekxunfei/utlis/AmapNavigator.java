package com.yl.deepseekxunfei.utlis;

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
        Log.d("intent广播", "showSearchResults: "+intents);
        context.sendBroadcast(intents);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e("Navigation", "通过 URL Scheme 启动导航失败", e);
        }
    }


    /**
     * 启动高德导航（支持途经点）
     * @param context 上下文
     * @param startName 起点名称（可选）
     * @param startLat 起点纬度
     * @param startLon 起点经度
     * @param endName 终点名称
     * @param endLat 终点纬度
     * @param endLon 终点经度
     * @param viaPoints 途经点列表（格式：lat1,lng1,name1|lat2,lng2,name2）
     */
    public static void startNavigation(Context context,
                                       String startName, double startLat, double startLon,
                                       String endName, double endLat, double endLon,
                                       String viaPoints) {

        // 构建URI参数
        Uri.Builder builder = Uri.parse("amapuri://route/plan/").buildUpon();

        // 起点参数（如果不需要起点可省略）
        if (startName != null) {
            builder.appendQueryParameter("slat", String.valueOf(startLat))
                    .appendQueryParameter("slon", String.valueOf(startLon))
                    .appendQueryParameter("sname", startName);
        }

        // 终点参数（必须）
        builder.appendQueryParameter("dlat", String.valueOf(endLat))
                .appendQueryParameter("dlon", String.valueOf(endLon))
                .appendQueryParameter("dname", endName);

        // 途经点处理（最多支持16个途经点）
        if (viaPoints != null && !viaPoints.isEmpty()) {
            String[] viaArray = viaPoints.split("\\|");
            for (int i = 0; i < Math.min(viaArray.length, 16); i++) {
                String[] point = viaArray[i].split(",");
                if (point.length == 3) {
                    builder.appendQueryParameter("via" + (i + 1) + "Latlng", point[0] + "," + point[1])
                            .appendQueryParameter("via" + (i + 1) + "Name", point[2]);
                }
            }
        }

        // 其他参数
        builder.appendQueryParameter("dev", "0")  // 0代表不包含偏移
                .appendQueryParameter("t", "0");   // 0-驾车 1-公交 2-步行 3-骑行

        Uri uri = builder.build();

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setPackage("com.autonavi.minimap");
            context.startActivity(intent);
        } catch (Exception e) {
        }
    }

}

