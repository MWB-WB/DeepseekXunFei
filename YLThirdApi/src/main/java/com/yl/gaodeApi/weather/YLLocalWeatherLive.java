package com.yl.gaodeApi.weather;

public class YLLocalWeatherLive {

    public YLLocalWeatherLive(String city, String weather, String temperature, String windDirection, String windPower, String humidity) {
        this.city = city;
        this.weather = weather;
        this.temperature = temperature;
        this.windDirection = windDirection;
        this.windPower = windPower;
        this.humidity = humidity;
    }

    private String city;
    private String weather;
    private String temperature;
    private String windDirection;
    private String windPower;
    private String humidity;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(String windDirection) {
        this.windDirection = windDirection;
    }

    public String getWindPower() {
        return windPower;
    }

    public void setWindPower(String windPower) {
        this.windPower = windPower;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }
}
