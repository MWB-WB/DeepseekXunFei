package com.yl.deepseekxunfei.scene;

import com.yl.deepseekxunfei.model.BaseChildModel;
import com.yl.deepseekxunfei.model.SceneModel;
import com.yl.deepseekxunfei.sceneenum.SceneType;
import com.yl.deepseekxunfei.utlis.SceneTypeConst;

public class SceneManager {

    private WeatherScene weatherScene;
    private NavScene navScene;
    private int currentSceneType;

    public SceneManager() {
        initChildScene();
    }

    private void initChildScene() {
        weatherScene = new WeatherScene();
        navScene = new NavScene();
    }

    //解析场景
    public SceneModel parseQuestionToScene(String text) {
        SceneModel resultModel = new SceneModel();
        resultModel.setText(text);
        if (text.startsWith("导航") || text.contains("附近的")) {
            resultModel.setScene(SceneType.NAVIGATION);
        } else if (text.startsWith("播放") || text.contains("音乐") || text.startsWith("我要听")) {
            resultModel.setScene(SceneType.MUSIC);
        } else if (text.contains("当前位置") || text.contains("我在哪")) {
            resultModel.setScene(SceneType.LOCATION);
        } else if (text.contains("天气")) {
            resultModel.setScene(SceneType.WEATHER);
        } else {
            resultModel.setScene(SceneType.CHITCHAT);
        }
        return resultModel;
    }


    //分发场景
    public BaseChildModel distributeScene(SceneModel sceneModel) {
        BaseChildModel baseChildModel;
        switch (sceneModel.getScene()) {
            case WEATHER:
                baseChildModel = weatherScene.parseSceneToChild(sceneModel);
                currentSceneType = baseChildModel.getType();
                break;
            case NAVIGATION:
                baseChildModel = navScene.parseSceneToChild(sceneModel);
                currentSceneType = baseChildModel.getType();
                break;
            case CHITCHAT:
                baseChildModel = new BaseChildModel();
                baseChildModel.setType(SceneTypeConst.CHITCHAT);
                baseChildModel.setText(sceneModel.getText());
                currentSceneType = SceneTypeConst.CHITCHAT;
                break;
            default:
                baseChildModel = new BaseChildModel();
        }
        return baseChildModel;
    }


}
