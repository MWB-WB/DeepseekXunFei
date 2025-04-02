package com.yl.deepseekxunfei.APICalls;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.yl.deepseekxunfei.page.LocationResult;
import com.yl.deepseekxunfei.OnPoiSearchListener;
import com.yl.deepseekxunfei.utlis.positioning;

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

/**
 * 周边搜索
 */
public class NeighborhoodSearch{
   private static OkHttpClient okHttpClient = new OkHttpClient();

    /**
     * 调用API 方法
     *
     * @param keywords 查询关键字
     * @param lat      纬度
     * @param lot      经度
     * @param radius   查询半径
     */
    public static void search(String keywords, int radius, OnPoiSearchListener onPoiSearchListener, Context context)  {
        positioning positioning = new positioning();
        try {
            positioning.initLocation(context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            positioning.release();
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences("Location",MODE_PRIVATE);
        String address = sharedPreferences.getString("cityCode","");
        String city = sharedPreferences.getString("city","");
        float lat = sharedPreferences.getFloat("latitude",0);
        float lot = sharedPreferences.getFloat("longitude",0);
        Log.d("当前坐标", "城市:: "+address+"\t纬度::"+lat+"\t经度"+lot+"\tcity"+city);
        String location = lot+","+lat;
        Log.d("API坐标", "search: "+location);
        // 构造高德POI搜索URL
        //https://restapi.amap.com/v3/place/around?parameters
        String url = "https://restapi.amap.com/v3/place/around?key=b134db263b1cdde4d64d26dadbaf3e65&keywords="+Uri.encode(keywords)+"&radius=" + radius + "&location="+location+"&extensions=base";
        Log.d("API请求", "search: "+url);
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
                     List<LocationResult> results = new ArrayList<>();
                     for (int i = 0; i < pois.length(); i++) {
                         JSONObject poi = pois.getJSONObject(i);
                         String name = poi.getString("name");
                         String address = poi.getString("address");
                         String location = poi.getString("location"); // 格式 "经度,纬度"

                         String[] latLng = location.split(",");
                         double longitude = Double.parseDouble(latLng[0]);
                         double latitude = Double.parseDouble(latLng[1]);

                         results.add(new LocationResult(name, address, latitude, longitude));
                     }
                     onPoiSearchListener.onSuccess(results);
                 } catch (JSONException e) {
                     onPoiSearchListener.onError("解析失败");
                 }
             }
         });
    }
}
