package com.yl.cretemodule.crete.roomCrete.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "crete_entity")
public class creteEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;//自增id
    @ColumnInfo(name = "featureInfo")
    private String featureInfo; //特征描述
    @ColumnInfo(name = "featureId")
    private String  featureId;//特征唯一标识
    @ColumnInfo(name = "groupName")
    private String  groupName;//声纹分组名称
    @ColumnInfo(name = "groupInfo")
    private String  groupInfo;//分组描述信息
    @ColumnInfo(name = "groupId")
    private String groupId;// 分组标识

    public creteEntity(String featureInfo, String featureId, String groupName, String groupInfo, String groupId) {
        this.featureInfo = featureInfo;
        this.featureId = featureId;
        this.groupName = groupName;
        this.groupInfo = groupInfo;
        this.groupId = groupId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupInfo() {
        return groupInfo;
    }

    public void setGroupInfo(String groupInfo) {
        this.groupInfo = groupInfo;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getFeatureId() {
        return featureId;
    }

    public void setFeatureId(String featureId) {
        this.featureId = featureId;
    }

    public String getFeatureInfo() {
        return featureInfo;
    }

    public void setFeatureInfo(String featureInfo) {
        this.featureInfo = featureInfo;
    }
}
