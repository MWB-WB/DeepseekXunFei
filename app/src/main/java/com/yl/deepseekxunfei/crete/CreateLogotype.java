package com.yl.deepseekxunfei.crete;

public class CreateLogotype {

    private String  featureInfo; //特征描述
    private String  featureId;//特征唯一标识
    private String  groupName;//声纹分组名称
    private String  groupInfo;//分组描述信息
    private String  groupId;// 分组标识

    public String getFeatureInfo() {
        return featureInfo;
    }

    public void setFeatureInfo(String featureInfo) {
        this.featureInfo = featureInfo;
    }

    public String getFeatureId() {
        return featureId;
    }

    public void setFeatureId(String featureId) {
        this.featureId = featureId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupInfo() {
        return groupInfo;
    }

    public void setGroupInfo(String groupInfo) {
        this.groupInfo = groupInfo;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
