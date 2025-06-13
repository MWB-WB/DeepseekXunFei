package com.yl.deepseekxunfei.APICalls;

import android.util.Log;

import androidx.annotation.NonNull;

import com.yl.deepseekxunfei.page.SceneryPage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ScenerySpotApi {
    public static SceneryPage sceneryPage = new SceneryPage();
    public static void ScenerySpotAPi(String location, String keywords, scenerySuccess success) {
        OkHttpClient okHttpClient = new OkHttpClient();
        String uri = "https://restapi.amap.com/v5/place/around?location=" + location + "&keywords=" + keywords + "&radius=50000&types=110000&key=b134db263b1cdde4d64d26dadbaf3e65&sortrule=distance&show_fields=photos";
        Request request = new Request.Builder().url(uri).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("请求失败", "onFailure: " + e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String jsonData = response.body().string();
                Log.d("请求", "onResponse: " + jsonData);
                try {
                    JSONObject sceneryJson = new JSONObject(jsonData);
                    JSONArray sceneryArray = sceneryJson.getJSONArray("pois");
                    List<SceneryPage> sceneryPageList = new ArrayList<>(); // 每次请求新建列表

                    for (int i = 0; i < sceneryArray.length(); i++) {
                        JSONObject sceneryFor = sceneryArray.getJSONObject(i);
                        SceneryPage sceneryPage = new SceneryPage(); // 每次循环新建对象

                        // 解析基础字段
                        sceneryPage.setId(sceneryFor.optString("id", ""));
                        sceneryPage.setName(sceneryFor.optString("name", ""));
                        sceneryPage.setAddress(sceneryFor.optString("address", ""));
                        sceneryPage.setDistance(sceneryFor.optString("distance", ""));
                        sceneryPage.setLocation(sceneryFor.optString("location", ""));
                        sceneryPage.setType(sceneryFor.optString("type", ""));

                        List<String> photoUrls = new ArrayList<>();
                        if (sceneryFor.has("photos")) {
                            JSONArray photos = sceneryFor.getJSONArray("photos");
                            for (int j = 0; j < photos.length(); j++) {
                                String photoUrl = photos.getJSONObject(j).optString("url", "");
                                if (!photoUrl.isEmpty()&& photoUrl.toLowerCase().endsWith(".jpg")) {
                                    photoUrls.add(photoUrl);
                                    Log.d("POI图片", "图片URL: " + photoUrl);
                                }
                            }
                            sceneryPage.setPhotoUrls(photoUrls);
                        }
                        sceneryPageList.add(sceneryPage); // 添加新对象到列表
                    }
                    success.success(sceneryPageList);
                } catch (JSONException e) {
                    Log.e("解析错误", "JSON异常: " + e.getMessage());
                    success.err(e.getMessage());
                }
            }
        });
    }
    public interface scenerySuccess{
        public void success(List<SceneryPage> list);
        public void err(String e);
    }
}
