package com.yl.deepseekxunfei.contextualResponses;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.yl.gaodeApi.poi.PositioningUtil;

import java.util.List;

public class ContextualResponses {


    /**
     * 关键字传入上下文
     *
     * @param addressName 目的地名称
     * @param
     * @return
     */
    public static String keyWordActionContextualResponses(String  user,String addressName, Context context) {
        String name = addressCity(context);
        String address = null;
        if (addressName != null) {
            address = "我在" + addressName;
        } else {
            if (name.equals("未获取到地址，请提醒用户提供具体地址")){
                address = name;
            }else {
                address ="我在"+name;
            }
        }
        return address;
    }

    public static String addressCity(Context context) {
        String city = null;
        PositioningUtil posit = new PositioningUtil();
        try {
            posit.initLocation(context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            posit.release();
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences("Location", MODE_PRIVATE);
        if (TextUtils.isEmpty(city)) {
            Log.d("TAG", "searchInAmap: " + city);
            city = sharedPreferences.getString("city", "");
        }
        String cityCode = sharedPreferences.getString("cityCode", "");
        float lat = sharedPreferences.getFloat("latitude", 0);
        float lot = sharedPreferences.getFloat("longitude", 0);
        Log.d("", "纬度::" + lat + "\t经度" + lot + "\tcity" + city + "\t区县cityCode编码" + cityCode);
        if (city != null) {
            return city;
        } else {
            return "未获取到地址，请提醒用户提供具体地址";
        }
    }
}
