package com.yl.deepseekxunfei.APICalls;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.yl.deepseekxunfei.page.LocationResult;
import com.yl.deepseekxunfei.OnPoiSearchListener;
import com.yl.deepseekxunfei.utlis.positioning;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 周边搜索
 */
public class NeighborhoodSearch {
    private static OkHttpClient okHttpClient = new OkHttpClient();

    /**
     * 高德地图通过地址获取经纬度
     */
    public static void getLocation(String address, String city, LocationListener listener) {
        if (TextUtils.isEmpty(address) && TextUtils.isEmpty(city)) {
            if (listener != null) {
                listener.onSuccess("");
            }
            return;
        }
        //"http://restapi.amap.com/v3/geocode/geo?address=上海市东方明珠&output=JSON&key=xxxxxxxxx";
        String geturl;
        if (city.isEmpty()) {
            geturl = "http://restapi.amap.com/v3/geocode/geo?key=b134db263b1cdde4d64d26dadbaf3e65&address=" + address;
        } else {
            geturl = "http://restapi.amap.com/v3/geocode/geo?key=b134db263b1cdde4d64d26dadbaf3e65&address=" + address + "&city=" + city;
        }

        Request request = new Request.Builder().url(geturl).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                listener.onSuccess("");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String jsonData = response.body().string();
                try {
                    JSONObject json = new JSONObject(jsonData);
                    JSONArray geocodes = json.getJSONArray("geocodes");
                    JSONObject geocode = geocodes.getJSONObject(0);
                    if (listener != null) {
                        listener.onSuccess(geocode.get("location").toString());
                    }
                } catch (JSONException e) {
                }
            }
        });
    }

    public interface LocationListener {
        void onSuccess(String location);
    }


    /**
     * 调用API 方法
     *
     * @param keywords 查询关键字
     * @param radius   查询半径
     */
    public static void search(String keywords, String location, int radius, OnPoiSearchListener onPoiSearchListener, Context context) {
        positioning positioning = new positioning();
        try {
            positioning.initLocation(context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            positioning.release();
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences("Location", MODE_PRIVATE);
        if (TextUtils.isEmpty(location)) {
            float lat = sharedPreferences.getFloat("latitude", 0);
            float lot = sharedPreferences.getFloat("longitude", 0);
            location = lot + "," + lat;
        }
        Log.d("API坐标", "location: " + location);
        Log.d("API坐标", "keywords: " + keywords);
        Log.d("API坐标", "radius: " + radius);
        // 构造高德POI搜索URL
        //https://restapi.amap.com/v3/place/around?parameters
        String url = "https://restapi.amap.com/v5/place/around?location=" + location + "&keywords=" + keywords + "&radius="+radius+"&key=b134db263b1cdde4d64d26dadbaf3e65&sortrule=distance&show_fields=photos";
//        String url = "https://restapi.amap.com/v3/place/around?key=b134db263b1cdde4d64d26dadbaf3e65&keywords=" + Uri.encode(keywords) + "&radius=" + radius + "&location=" + location + "&extensions=base";
        Request request = new Request.Builder().url(url).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                onPoiSearchListener.onError(e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String jsonData = response.body().string();

                try {
                    JSONObject json = new JSONObject(jsonData);
                    JSONArray pois = json.getJSONArray("pois");
                    Log.d("TAG", "onResponsepois: "+pois);
                    List<LocationResult> results = new ArrayList<>();
                    for (int i = 0; i < pois.length(); i++) {
                        JSONObject poi = pois.getJSONObject(i);
                        String name = poi.getString("name");
                        String address = poi.getString("address");
                        String location = poi.getString("location"); // 格式 "经度,纬度"

                        String[] latLng = location.split(",");
                        double longitude = Double.parseDouble(latLng[0]);
                        double latitude = Double.parseDouble(latLng[1]);
                       // 解析第一个.jpg图片
                        String firstJpgUrl = null;
                        if (poi.has("photos")) {
                            JSONArray photos = poi.getJSONArray("photos");
                            for (int j = 0; j < photos.length(); j++) {
                                String photoUrl = photos.getJSONObject(j).optString("url", "");
                                if (!photoUrl.isEmpty() && photoUrl.toLowerCase().endsWith(".jpg")) {
                                    firstJpgUrl = photoUrl;
                                    break; // 只取第一张
                                }
                            }
                        }
                        results.add(new LocationResult(name, address, latitude, longitude,firstJpgUrl));
                    }
                    Log.d("TAG", "onResponse: "+results);
                    onPoiSearchListener.onSuccess(results);
                } catch (JSONException e) {
                    onPoiSearchListener.onError("解析失败");
                }
            }
        });
    }
}
