package com.yl.gaodeApi;

import android.content.Context;

import com.amap.api.location.AMapLocationClient;

public class GaodeApi {

    public static void gaodeInit(Context context){
        AMapLocationClient.setApiKey("5c04f780c8748ab0d52f27608efa579f");
        AMapLocationClient.updatePrivacyShow(context, true, true);
        AMapLocationClient.updatePrivacyAgree(context, true);
    }

}
