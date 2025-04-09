package com.yl.deepseekxunfei.APICalls;

import static android.content.Context.MODE_PRIVATE;

import android.adservices.adselection.ReportEventRequest;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.LinearLayout;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLive;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.amap.api.services.weather.WeatherSearchQuery;
import com.yl.deepseekxunfei.ContextHolder;
import com.yl.deepseekxunfei.utlis.positioning;

import java.util.MissingFormatWidthException;

public class WeatherAPI implements WeatherSearch.OnWeatherSearchListener {
    private WeatherSearchQuery mquery;
    private WeatherSearch mweathersearch;
    private LocalWeatherLive weatherlive;
    private Context context = ContextHolder.getContext();

    public void weatherSearch() {
        //获取当前定位城市
        positioning positioning = new positioning();
        try {
            positioning.initLocation(context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            positioning.release();
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences("Location", MODE_PRIVATE);
//        String city = sharedPreferences.getString("city", "");
        String city = "深圳市";
        Log.d("天气查询城市", "weatherSearch: " + city);
//检索参数为城市和天气类型，实况天气为WEATHER_TYPE_LIVE、天气预报为WEATHER_TYPE_FORECAST
        mquery = new WeatherSearchQuery(city, WeatherSearchQuery.WEATHER_TYPE_LIVE);
        try {
            mweathersearch = new WeatherSearch(context);
            mweathersearch.setOnWeatherSearchListener(this);
            mweathersearch.setQuery(mquery);
            mweathersearch.searchWeatherAsyn(); //异步搜索
        } catch (AMapException e) {
            throw new RuntimeException(e);
        }
    }

    //设置回调接口
    public interface OnWeatherListener {
        //查询成功方法
        void onWeatherSuccess(LocalWeatherLive weatherLive);

        //查询失败方法
        void onWeatherError(String message, int rCode);
    }

    private OnWeatherListener onWeatherListener;
    //回调方法，获取查询信息
    public void setOnWeatherListener(OnWeatherListener onWeatherListener) {
        this.onWeatherListener = onWeatherListener;
        Log.d("查询结果", "setOnWeatherListener: " + onWeatherListener.toString());
    }

    /**
     * 实时天气查询回调
     */
    @Override
    public void onWeatherLiveSearched(LocalWeatherLiveResult weatherLiveResult, int rCode) {
        if (rCode == 1000) {
            if (weatherLiveResult != null && weatherLiveResult.getLiveResult() != null) {
                weatherlive = weatherLiveResult.getLiveResult();
                if (onWeatherListener != null) {
                    onWeatherListener.onWeatherSuccess(weatherlive);
                } else {
                    onWeatherListener.onWeatherError("天气查询失败：错误码：", rCode);
                }
            } else {
                Log.d("天气请求", "onWeatherLiveSearched: " + weatherLiveResult.getLiveResult() + "\t" + weatherLiveResult);
//                ToastUtil.show(WeatherSearchActivity.this, R.string.no_result);
            }
        } else {
            Log.d("天气请求", "onWeatherLiveSearched: " + rCode);
//            ToastUtil.showerror(WeatherSearchActivity.this, rCode);
        }
    }

    @Override
    public void onWeatherForecastSearched(LocalWeatherForecastResult localWeatherForecastResult, int i) {

    }
}
