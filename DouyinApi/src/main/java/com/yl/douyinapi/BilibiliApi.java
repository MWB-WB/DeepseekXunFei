package com.yl.douyinapi;

import android.app.Activity;

import androidx.annotation.Nullable;

import com.bilibili.lib.accountoauth.OAuthManager;

public class BilibiliApi implements OAuthManager.IOauthCallback {

    private OAuthManager manager;

    public void init(Activity activity) {
        manager = new OAuthManager(activity, "", "");
        manager.setOAuthCallback(this);
        manager.startOauth();
    }


    @Override
    public void onGetCode(@Nullable String code, @Nullable String msg) {

    }

}
