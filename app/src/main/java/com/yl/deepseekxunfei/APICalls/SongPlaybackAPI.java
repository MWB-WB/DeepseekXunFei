package com.yl.deepseekxunfei.APICalls;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttp;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

//歌曲播放API
public class SongPlaybackAPI {
   private static OkHttpClient okHttpClient = new OkHttpClient();
    public static void playBack(Context context,String musicId ){
        //构建歌曲播放Api
        //http://m.kugou.com/app/i/getSongInfo.php?cmd=playInfo&hash=07BC472ACDE477702A36BF02306C40F9
//        String url = "http://antiserver.kuwo.cn/anti.s?type=convert_url&rid="+musicId+"&format=aac|mp3&response=url";
        String url = "http://m.kugou.com/app/i/getSongInfo.php?cmd=playInfo&hash="+musicId;
        Request request = new Request.Builder().url(url).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("TAG", "onFailure: "+e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d("播放", "状态码: "+response);
            }
        });
    }
}
