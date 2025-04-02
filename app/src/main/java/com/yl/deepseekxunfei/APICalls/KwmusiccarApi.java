package com.yl.deepseekxunfei.APICalls;
//酷我音乐api歌曲查询接口
//

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.yl.deepseekxunfei.page.LocationMusccarResult;
import com.yl.deepseekxunfei.OnPoiSearchListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class KwmusiccarApi {
    private static OkHttpClient okHttpClient = new OkHttpClient();

    /**
     * 歌曲查询
     * @param context 上下文对象
     * @param all 歌曲名或者歌手名
     * @param pn 要查询的条数
     * @param rn 当前页显示的条数
     */
    public static void musiccar(Context context, String all  , OnPoiSearchListenerMusccar onPoiSearchListenerMusccar){
       int pn = 0;//固定查询10条
        int rn =50 ;//固定显示10条
        //构建酷狗歌曲查询API
        String uri = "http://mobilecdn.kugou.com/api/v3/search/song?format=json&keyword="+all+"&page=1";
        Request request = new Request.Builder().url(uri).build();
        Log.d("查询URI", "musiccar: "+uri);
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                onPoiSearchListenerMusccar.onError(e.getMessage());
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String jsonData = response.body().string();
                Log.d("查询结果", "onResponse: " + jsonData);

                try {
                    JSONObject json = new JSONObject(jsonData);
                    JSONArray infoArray = json.getJSONObject("data").getJSONArray("info"); // 注意层级是 data -> info
                    List<LocationMusccarResult> results = new ArrayList<>();

                    for (int i = 0; i < infoArray.length(); i++) {
                        JSONObject song = infoArray.getJSONObject(i);

                        // 提取关键字段
                        String songName = song.getString("songname");
                        String artist = song.getString("singername");
                        String album = song.getString("filename");
                        String hash = song.getString("hash"); // 酷狗用 hash 作为歌曲唯一ID

                        // 处理 musicId
                        List<String> musicId = new ArrayList<>();
                        musicId.add(hash);

                        // 添加到结果列表
                        results.add(new LocationMusccarResult(songName, album, artist, musicId));

                        // 打印日志验证
                        Log.d("歌曲信息",
                                "歌曲名: " + songName +
                                        ", 歌手: " + artist +
                                        ", 专辑: " + album +
                                        ", Hash: " + hash
                        );
                    }
                    onPoiSearchListenerMusccar.onSuccess(results);
                } catch (JSONException e) {
                    Log.e("解析错误", "JSON解析失败: " + e.getMessage());
                    onPoiSearchListenerMusccar.onError("解析失败");
                }
            }
        });
    }
}
