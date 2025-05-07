package com.yl.deepseekxunfei.APICalls;

import com.yl.deepseekxunfei.page.LocationMusccarResult;
import com.yl.deepseekxunfei.page.LocationResult;

import java.util.List;

//酷我回调接口
public interface OnMusicSearchListenerMusccar {
    void onSuccess(List<LocationMusccarResult> results);
    void onError(String error);
}
