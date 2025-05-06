package com.yl.deepseekxunfei.model;

import java.util.ArrayList;
import java.util.List;

public class BaseChildModel {

    protected List<Integer> type;
    protected String text;

    public BaseChildModel() {
        type = new ArrayList<>();
    }

    public BaseChildModel(String text) {
        this.text = text;
        type = new ArrayList<>();
    }

    public List<Integer> getType() {
        return type;
    }

    public void addType(int type) {
        this.type.add(type);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
