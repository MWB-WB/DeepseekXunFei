package com.yl.gaodeApi.weather;

public class YLLocalWeatherForecastResult {

    public YLLocalWeatherForecastResult(String dayTemp, String nightTemp, String date) {
        this.dayTemp = dayTemp;
        this.nightTemp = nightTemp;
        this.date = date;
    }

    private String dayTemp;
    private String nightTemp;
    private String date;

    public String getDayTemp() {
        return dayTemp;
    }

    public void setDayTemp(String dayTemp) {
        this.dayTemp = dayTemp;
    }

    public String getNightTemp() {
        return nightTemp;
    }

    public void setNightTemp(String nightTemp) {
        this.nightTemp = nightTemp;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
