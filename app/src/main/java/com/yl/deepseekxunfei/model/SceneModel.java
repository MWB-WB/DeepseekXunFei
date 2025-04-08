package com.yl.deepseekxunfei.model;

import com.yl.deepseekxunfei.sceneenum.SceneType;

public class SceneModel {

    private SceneType scene;
    private String text;

    public SceneModel() {
    }

    public SceneModel(SceneType scene, String text) {
        this.scene = scene;
        this.text = text;
    }

    public SceneType getScene() {
        return scene;
    }

    public void setScene(SceneType scene) {
        this.scene = scene;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
