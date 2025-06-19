package com.yl.gaodeApi.weather;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.weather.LocalDayWeatherForecast;
import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLive;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.amap.api.services.weather.WeatherSearchQuery;
import com.yl.gaodeApi.poi.PositioningUtil;
import com.yl.ylcommon.ylsceneconst.SceneTypeConst;

import java.util.ArrayList;
import java.util.List;

public class WeatherAPI implements WeatherSearch.OnWeatherSearchListener {
    private WeatherSearchQuery mquery;
    private WeatherSearch mweathersearch;
    private LocalWeatherLive weatherlive;
    private Context context;

    public WeatherAPI(Context context) {
        this.context = context;
    }

    public void weatherSearch(int type, String city) {
        //获取当前定位城市
        PositioningUtil positioning = new PositioningUtil();
        try {
            positioning.initLocation(context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            positioning.release();
        }
        if (TextUtils.isEmpty(city)) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("Location", MODE_PRIVATE);
            city = sharedPreferences.getString("city", "");
        }
        Log.d("TAG", "weatherSearch: " + city);
        //检索参数为城市和天气类型，实况天气为WEATHER_TYPE_LIVE、天气预报为WEATHER_TYPE_FORECAST
        if (type == SceneTypeConst.TODAY_WEATHER) {
            mquery = new WeatherSearchQuery(city, WeatherSearchQuery.WEATHER_TYPE_LIVE);
        } else if (type == SceneTypeConst.FEATHER_WEATHER) {
            mquery = new WeatherSearchQuery(city, WeatherSearchQuery.WEATHER_TYPE_FORECAST);
        }
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
        void onWeatherSuccess(YLLocalWeatherLive weatherLive);

        //查询失败方法
        void onWeatherError(String message, int rCode);
    }

    public interface OnForecastWeatherListener {
        //查询成功方法
        void onWeatherSuccess(List<YLLocalWeatherForecastResult> localWeatherForecastResult);

        //查询失败方法
        void onWeatherError(String message, int rCode);
    }

    private OnWeatherListener onWeatherListener;
    private OnForecastWeatherListener onForecastWeatherListener;

    //回调方法，获取查询信息
    public void setOnWeatherListener(OnWeatherListener onWeatherListener) {
        this.onWeatherListener = onWeatherListener;
    }

    public void setOnForecastWeatherListener(OnForecastWeatherListener onForecastWeatherListener) {
        this.onForecastWeatherListener = onForecastWeatherListener;
    }

    /**
     * 实时天气查询回调
     */
    @Override
    public void onWeatherLiveSearched(LocalWeatherLiveResult weatherLiveResult, int rCode) {
        if (rCode == 1000) {
            if (weatherLiveResult != null && weatherLiveResult.getLiveResult() != null) {
                weatherlive = weatherLiveResult.getLiveResult();
                //转换成我们自己的内部类
                YLLocalWeatherLive ylLocalWeatherLive = new YLLocalWeatherLive(weatherlive.getCity()
                        , weatherlive.getWeather(), weatherlive.getTemperature(), weatherlive.getWindDirection()
                        , weatherlive.getWindPower(), weatherlive.getHumidity());
                if (onWeatherListener != null) {
                    onWeatherListener.onWeatherSuccess(ylLocalWeatherLive);
                } else {
                    onWeatherListener.onWeatherError("天气查询失败：错误码：", rCode);
                }
            } else {
            }
        }
    }

    @Override
    public void onWeatherForecastSearched(LocalWeatherForecastResult localWeatherForecastResult, int rCode) {
        if (localWeatherForecastResult != null && localWeatherForecastResult.getForecastResult().getWeatherForecast().size() > 0) {
            if (onForecastWeatherListener != null) {
                List<LocalDayWeatherForecast> weatherForecast = localWeatherForecastResult.getForecastResult().getWeatherForecast();
                List<YLLocalWeatherForecastResult> ylLocalWeatherForecastResults = new ArrayList<>();
                for (LocalDayWeatherForecast localDayWeatherForecast : weatherForecast) {
                    ylLocalWeatherForecastResults.add(new YLLocalWeatherForecastResult(localDayWeatherForecast.getDayTemp()
                            , localDayWeatherForecast.getNightTemp(), localDayWeatherForecast.getDate()));
                }
                onForecastWeatherListener.onWeatherSuccess(ylLocalWeatherForecastResults);
            } else {
                onForecastWeatherListener.onWeatherError("天气查询失败：错误码：", rCode);
            }
        }
    }

}
