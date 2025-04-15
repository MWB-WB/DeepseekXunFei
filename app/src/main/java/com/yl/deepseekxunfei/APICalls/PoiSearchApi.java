package com.yl.deepseekxunfei.APICalls;

import android.content.Context;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.poisearch.PoiSearchV2;

public class PoiSearchApi{

    private static PoiSearchV2 poiSearchV2;
    private static PoiSearchV2.Query query;

    public static void searchPoi(Context context, String keyword, String cityCode, PoiSearchV2.OnPoiSearchListener onPoiSearchListener) {
        try {
            query = new PoiSearchV2.Query(keyword, "", cityCode);
            poiSearchV2 = new PoiSearchV2(context, query);
            poiSearchV2.setOnPoiSearchListener(onPoiSearchListener);
            poiSearchV2.searchPOIAsyn();
        } catch (AMapException e) {
            throw new RuntimeException(e);
        }
    }


}
