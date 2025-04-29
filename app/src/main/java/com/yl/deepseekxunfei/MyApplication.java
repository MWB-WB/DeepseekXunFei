package com.yl.deepseekxunfei;


import android.app.Application;

import com.amap.api.location.AMapLocationClient;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.yl.douyinapi.DouyinApi;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyApplication extends Application {

    private ExecutorService executorService;

    @Override
    public void onCreate() {
        super.onCreate();
        executorService = Executors.newSingleThreadExecutor();
        initThrid();
    }

    private void initThrid() {
        executorService.submit(() -> {
            // 初始化高德地图SDK
            AMapLocationClient.setApiKey("5c04f780c8748ab0d52f27608efa579f");
            AMapLocationClient.updatePrivacyShow(this, true, true);
            AMapLocationClient.updatePrivacyAgree(this, true);
            // 初始化语音合成
            SpeechUtility.createUtility(this, SpeechConstant.APPID + "=27b3a946");
            DouyinApi.init(this);
        });
    }

}



