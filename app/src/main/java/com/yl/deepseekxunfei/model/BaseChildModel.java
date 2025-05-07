package com.yl.deepseekxunfei.model;

public class BaseChildModel {

    protected Integer type;
    protected String text;

    public BaseChildModel() {
    }

    public BaseChildModel(String text) {
        this.text = text;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "BaseChildModel{" +
                "type=" + type +
                ", text='" + text + '\'' +
                '}';
    }
}
