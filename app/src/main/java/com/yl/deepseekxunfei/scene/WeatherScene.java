package com.yl.deepseekxunfei.scene;

import com.yl.deepseekxunfei.model.BaseChildModel;
import com.yl.deepseekxunfei.model.SceneModel;
import com.yl.deepseekxunfei.utlis.SceneTypeConst;

public class WeatherScene extends BaseChildScene{

    @Override
    public BaseChildModel parseSceneToChild(SceneModel sceneModel) {
        String text = sceneModel.getText();
        BaseChildModel weatherSceneModel = new BaseChildModel();
        weatherSceneModel.setText(text);
        if (text.contains("今天") || text.contains("现在")) {
            weatherSceneModel.setType(SceneTypeConst.TODAY_WEATHER);
        } else {
            weatherSceneModel.setType(SceneTypeConst.FEATHER_WEATHER);
        }
        return weatherSceneModel;
    }

}
