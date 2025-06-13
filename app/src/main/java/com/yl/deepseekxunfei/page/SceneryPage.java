package com.yl.deepseekxunfei.page;

import java.util.ArrayList;
import java.util.List;

public class SceneryPage {
    String id ;
    String name ;
    String address ;
    String distance ;
    String location; // 格式："经度,纬度"
    String type ;
    List<String> photoUrls = new ArrayList<>(); // 图片URL列表

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getPhotoUrls() {
        return photoUrls;
    }

    public void setPhotoUrls(List<String> photoUrls) {
        this.photoUrls = photoUrls;
    }

    @Override
    public String toString() {
        return "SceneryPage{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", distance='" + distance + '\'' +
                ", location='" + location + '\'' +
                ", type='" + type + '\'' +
                ", photoUrls=" + photoUrls +
                '}';
    }
}
