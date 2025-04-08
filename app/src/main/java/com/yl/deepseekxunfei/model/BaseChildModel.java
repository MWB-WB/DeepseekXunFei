package com.yl.deepseekxunfei.model;

public class BaseChildModel {

    protected int type;
    protected String text;

    public BaseChildModel() {
    }

    public BaseChildModel(int type, String text) {
        this.type = type;
        this.text = text;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
