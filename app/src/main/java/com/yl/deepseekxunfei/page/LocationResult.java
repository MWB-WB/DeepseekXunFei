package com.yl.deepseekxunfei.page;

/**
 * 数据模型
 */
public class LocationResult {
    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private String photoUrl;//图片url

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public LocationResult(String address, String name,double longitude, double latitude,String photoUrl) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.address = address;
        this.name = name;
        this.photoUrl = photoUrl;
    }

}
