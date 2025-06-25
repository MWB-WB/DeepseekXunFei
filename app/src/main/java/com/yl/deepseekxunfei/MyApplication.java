package com.yl.deepseekxunfei;


import android.app.Application;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.yl.deepseekxunfei.room.AppDatabase;
import com.yl.ylcommon.utlis.ContextHolder;
import com.yl.douyinapi.DouyinApi;
import com.yl.gaodeApi.GaodeApi;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyApplication extends Application {

    private ExecutorService executorService;

    @Override
    public void onCreate() {
        super.onCreate();
        executorService = Executors.newSingleThreadExecutor();
        ContextHolder.init(this); // 保存全局 Context
        initThrid();
        AppDatabase.getInstance(this).query();
    }

    private void initThrid() {
        executorService.submit(() -> {
            // 初始化高德地图SDK
            GaodeApi.gaodeInit(this);
            // 初始化语音合成
            SpeechUtility.createUtility(this, SpeechConstant.APPID + "=27b3a946");
            DouyinApi.init(this);
        });
    }

}



