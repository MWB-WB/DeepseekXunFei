package com.yl.deepseekxunfei.model;

import java.util.ArrayList;
import java.util.List;

public class NavChildMode extends BaseChildModel {

    private List<NavChildMode.GeoEntity> entities;
    private String location;
    //执行顺序
    private int order;
    private List<NavChildMode> navChildModes;

    public NavChildMode() {
        navChildModes = new ArrayList<>();
    }

    public List<GeoEntity> getEntities() {
        return entities;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public List<NavChildMode> getNavChildModes() {
        return navChildModes;
    }

    public void addNavChildMode(NavChildMode navChildMode) {
        this.navChildModes.add(navChildMode);
    }

    public void setEntities(List<GeoEntity> entities) {
        this.entities = entities;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "NavChildMode{" +
                "entities=" + entities +
                ", location='" + location + '\'' +
                ", order=" + order +
                ", navChildModes=" + navChildModes +
                '}';
    }

    // GeoEntity.java
    public enum GeoEntityType {
        CITY,          // 城市（如北京、上海市）
        SPECIFIC_PLACE,// 具体地名（如天安门、南京东路）
        NATION,// 具体地名（如天安门、南京东路）
        NATION_Z,// 具体地名（如天安门、南京东路）
        GENERAL_AREA,  // 泛指区域（如附近、周边）
        UNKNOWN        // 未知类型
    }

    public static class GeoEntity {
        private String name;
        private GeoEntityType type;

        public GeoEntity(String name, GeoEntityType type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public GeoEntityType getType() {
            return type;
        }

        public void setType(GeoEntityType type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return "GeoEntity{" +
                    "name='" + name + '\'' +
                    ", type=" + type +
                    '}';
        }
    }

}
