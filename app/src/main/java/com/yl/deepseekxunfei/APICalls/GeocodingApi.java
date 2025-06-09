package com.yl.deepseekxunfei.APICalls;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GeocodingApi {
    private static OkHttpClient okHttpClient = new OkHttpClient();

    public void geocoding(String address, String city, success success) {
        String Api = "https://restapi.amap.com/v3/geocode/geo?key=b134db263b1cdde4d64d26dadbaf3e65&address=" + address + "&city=" + city;
        Request request = new Request.Builder().url(Api).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("TAG", "onFailure: " + e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseBody = response.body().string();
                    Log.d("TAG", "onResponse: " + responseBody);

                    JSONObject jsonObject = new JSONObject(responseBody);

                    // 首先检查API返回的状态
                    String status = jsonObject.optString("status", "0");
                    if (!"1".equals(status)) {
                        String info = jsonObject.optString("info", "未知错误");
                        Log.d("TAG", "onResponse错误: "+info);
                        return;
                    }
                    // 检查geocodes是否存在
                    if (!jsonObject.has("geocodes")) {
                        return;
                    }
                    JSONArray jsonElements = jsonObject.getJSONArray("geocodes");
                    if (jsonElements.length() == 0) {
                        return;
                    }
                    JSONObject object = jsonElements.getJSONObject(0);
                    String location = object.optString("location", "");
                    if (!location.isEmpty()) {
                        success.SuccessAPI(location);
                    }
                } catch (Exception e) {
                    Log.e("TAG", "解析错误: " + e);
                }
            }
        });
    }

    public interface success {
        void SuccessAPI(String response);
    }
}