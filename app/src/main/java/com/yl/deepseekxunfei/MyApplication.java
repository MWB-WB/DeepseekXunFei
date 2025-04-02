package com.yl.deepseekxunfei;


import android.app.Application;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //  开放平台注册的APPID
        SpeechUtility.createUtility(MyApplication.this, SpeechConstant.APPID + "=27b3a946");
    }
}



