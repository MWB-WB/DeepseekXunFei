package com.yl.gaodeApi.poi;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

//定位工具类
public class PositioningUtil {
    // 在合适的位置初始化定位服务
    private AMapLocationClient mLocationClient;

    public  void initLocation(  Context context) throws Exception {
        // 初始化定位
        mLocationClient = new AMapLocationClient(context.getApplicationContext());

        // 设置定位回调监听
        mLocationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
                    // 定位成功
                    double latitude = aMapLocation.getLatitude();
                    double longitude = aMapLocation.getLongitude();
                    String cityCode = aMapLocation.getCityCode();
                    String adcode = aMapLocation.getAdCode();
                    // 城市信息（需setNeedAddress(true)）
                    String city = aMapLocation.getCity();          // 城市名称（如"北京市"）
                    Log.d("经纬度变化", "latitude: "+latitude+"\t"+"longitude\t"+longitude);
                    Log.d("城市", "cityCode: "+cityCode+"\tcity"+city);
                    // 保存位置信息，用于后续搜索
                    SharedPreferences.Editor editor = context.getSharedPreferences("Location", MODE_PRIVATE).edit();
                    editor.putFloat("latitude", (float) latitude);
                    editor.putFloat("longitude", (float) longitude);
                    editor.putString("cityCode", cityCode);
                    editor.putString("city", city); // 增加城市名称存储
                    editor.putString("adcode", adcode); // 增加城市名称存储
                    editor.apply();
                } else {
                    // 定位失败
                    Log.d("定位失败", "错误码:" + aMapLocation.getErrorCode() + ", 错误信息:" + aMapLocation.getErrorInfo());
                }
            }
        });

        // 设置定位参数
        AMapLocationClientOption option = new AMapLocationClientOption();
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        option.setOnceLocation(true);
        option.setNeedAddress(true);
        option.setGeoLanguage(AMapLocationClientOption.GeoLanguage.ZH);
        mLocationClient.setLocationOption(option);
        option.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.SignIn); // 明确用途
        // 启动定位
        mLocationClient.startLocation();
        SharedPreferences sharedPreferences = context.getSharedPreferences("Location",MODE_PRIVATE);
        String address = sharedPreferences.getString("adcode","");
        String cityCode = sharedPreferences.getString("cityCode","");
        String city = sharedPreferences.getString("city","");
        float lat = sharedPreferences.getFloat("latitude",0);
        float lot = sharedPreferences.getFloat("longitude",0);
        Log.d("当前坐标", "区县:: "+address+"\t纬度::"+lat+"\t经度"+lot+"\tcity"+city+"\tcityCode"+cityCode);
    }
    // 释放资源
    public void release() {
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
    }
}
