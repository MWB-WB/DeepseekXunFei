package com.yl.deepseekxunfei.utlis;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.yl.deepseekxunfei.OnPoiSearchListener;
import com.yl.deepseekxunfei.page.LocationResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.*;

/**
 * 关键字搜索
 */
public class searchIn {
    public static void searchInAmap(Context context, String keyword, String city, OnPoiSearchListener listener) {
        OkHttpClient client = new OkHttpClient();
        positioning posit = new positioning();
        try {
            posit.initLocation(context);
        } catch (Exception e) {
            Log.d("报错", "searchInAmap: " + e);
            throw new RuntimeException(e);
        } finally {
            posit.release();
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences("Location", MODE_PRIVATE);
        String cityCode = sharedPreferences.getString("cityCode", "");
        if (TextUtils.isEmpty(city)) {
            city = sharedPreferences.getString("city", "");
        }
        float lat = sharedPreferences.getFloat("latitude", 0);
        float lot = sharedPreferences.getFloat("longitude", 0);
        Log.d("当前坐标", "纬度::" + lat + "\t经度" + lot + "\tcity" + city + "\t区县cityCode编码" + cityCode);
        // 构造高德POI搜索URL
        String url = "https://restapi.amap.com/v3/place/text?key=b134db263b1cdde4d64d26dadbaf3e65&keywords="
                + Uri.encode(keyword) + "&city=" + city + "&offset=20&extensions=all&citylimit=true&"; // 限定城市（可选）
        Log.d("请求", "searchInAmap: " + url);
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                listener.onError(e.getMessage());
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                String jsonData = response.body().string();
                try {
                    JSONObject json = new JSONObject(jsonData);
                    Log.d("TAG", "关键字: " + json);
                    JSONArray pois = json.getJSONArray("pois");
                    Log.d("TAG", "关键字: " + pois);
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
                                    Log.d("TAG", "关键字: "+firstJpgUrl);
                                    break; // 只取第一张
                                }
                            }
                        }
                        results.add(new LocationResult(name, address, latitude, longitude, firstJpgUrl));
                    }

                    listener.onSuccess(results);
                } catch (JSONException e) {
                    listener.onError("解析失败");
                }
            }
        });
    }
}
