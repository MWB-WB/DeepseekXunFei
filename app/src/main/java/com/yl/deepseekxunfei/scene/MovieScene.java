package com.yl.deepseekxunfei.scene;

import com.yl.deepseekxunfei.model.BaseChildModel;
import com.yl.deepseekxunfei.model.SceneModel;
import com.yl.deepseekxunfei.utlis.SceneTypeConst;

public class MovieScene extends BaseChildScene {

    @Override
    public BaseChildModel parseSceneToChild(SceneModel sceneModel) {
        String text = sceneModel.getText();
        BaseChildModel baseChildModel = new BaseChildModel();
        baseChildModel.setText(text);
        if (text.contains("最近")) {
            baseChildModel.setType(SceneTypeConst.RECENT_FILMS);
        } else if (text.contains("好看")) {
            baseChildModel.setType(SceneTypeConst.CHITCHAT);
        }
        return baseChildModel;
    }
}
