package com.yl.gaodeApi.poi;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Callback;

/**
 * 逆地理编码
 */
public class ReverseGeography {
    private static OkHttpClient okHttpClient = new OkHttpClient();
    public String formattedAddress;


    public void reverseGeographyApi(String location,successApi successApi) {
        String api = "https://restapi.amap.com/v3/geocode/regeo?output=json&location=" +
                location + "&key=b134db263b1cdde4d64d26dadbaf3e65&radius=1000&extensions=all";

        Request request = new Request.Builder().url(api).build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("ReverseGeo", "请求失败: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseBody = response.body().string();
                    Log.d("ReverseGeo", "响应数据: " + responseBody);

                    JSONObject jsonObject = new JSONObject(responseBody);

                    // 检查API返回状态
                    String status = jsonObject.optString("status", "0");
                    if (!"1".equals(status)) {
                        String info = jsonObject.optString("info", "未知错误");
                        Log.e("ReverseGeo", "API错误: " + info);
                        return;
                    }

                    // 检查regeocode是否存在
                    if (!jsonObject.has("regeocode")) {
                        Log.e("ReverseGeo", "缺少regeocode字段");
                        return;
                    }

                    // 获取regeocode对象（注意：这是一个JSONObject，不是JSONArray）
                    JSONObject regeocode = jsonObject.getJSONObject("regeocode");

                    // 解析格式化地址
                    formattedAddress =regeocode.optString("formatted_address", "");
                    Log.d("ReverseGeo", "格式化地址: " + formattedAddress);
                    successApi.success(formattedAddress);
                    // 解析POIs（周边兴趣点）
                    if (regeocode.has("pois")) {
                        JSONArray pois = regeocode.getJSONArray("pois");
                        for (int i = 0; i < pois.length(); i++) {
                            JSONObject poi = pois.getJSONObject(i);
                            String poiName = poi.optString("name", "");
                            String poiAddress = poi.optString("address", "");
                            Log.d("ReverseGeo", "POI[" + i + "]: " + poiName + " - " + poiAddress);
                        }
                    }

                } catch (Exception e) {
                    Log.e("ReverseGeo", "解析错误: " + e.getMessage());
                }
            }
        });
    }
    public interface successApi{
        void success(String formattedAddress);
    }
}