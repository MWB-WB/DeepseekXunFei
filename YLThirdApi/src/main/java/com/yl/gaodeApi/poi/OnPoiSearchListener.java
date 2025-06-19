package com.yl.gaodeApi.poi;

import com.yl.gaodeApi.page.LocationResult;

import java.util.List;

/**
 * 回调接口
 */
public interface OnPoiSearchListener {
    void onSuccess(List<LocationResult> results);
    void onError(String error);
}
