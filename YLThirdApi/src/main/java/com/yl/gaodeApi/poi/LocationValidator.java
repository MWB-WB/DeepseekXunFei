package com.yl.gaodeApi.poi;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class LocationValidator {

    public LocationValidator(Context context) {

    }

    public void validateAddress(String address, ValidationCallback validationCallback) {
        OkHttpClient client = new OkHttpClient();
        // 构造高德POI搜索URL
        String url = "https://restapi.amap.com/v3/geocode/geo?key=b134db263b1cdde4d64d26dadbaf3e65&" + "&address=" + address;
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (validationCallback != null) {
                    validationCallback.onResult(false);
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {
                String jsonData = response.body().string();
                try {
                    JSONObject json = new JSONObject(jsonData);
                    int count = Integer.parseInt(json.getString("count"));
                    Log.e("地址2", "onResponse: " + count);
                    if (count > 0) {
                        if (validationCallback != null) {
                            validationCallback.onResult(true);
                        }
                    } else {
                        if (validationCallback != null) {
                            validationCallback.onResult(false);
                        }
                    }
                } catch (JSONException e) {
                    if (validationCallback != null) {
                        validationCallback.onResult(false);
                    }
                }
            }
        });
    }


    public interface ValidationCallback {
        void onResult(boolean isValid);
    }
}