package com.yl.deepseekxunfei.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
/**
 * 区县编码映射
 */
@Entity(tableName = "location_data")
public class AMapLocationEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "adcode")
    private String adcode;

    @ColumnInfo(name = "citycode")
    private String citycode;

    public AMapLocationEntity(String name, String adcode, String citycode) {
        this.name = name;
        this.adcode = adcode;
        this.citycode = citycode;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAdcode() {
        return adcode;
    }

    public void setAdcode(String adcode) {
        this.adcode = adcode;
    }

    public String getCitycode() {
        return citycode;
    }

    public void setCitycode(String citycode) {
        this.citycode = citycode;
    }

    @Override
    public String toString() {
        return "AMapLocationEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", adcode='" + adcode + '\'' +
                ", citycode='" + citycode + '\'' +
                '}';
    }
}
