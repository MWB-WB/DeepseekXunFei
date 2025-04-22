package com.yl.deepseekxunfei.scene;

import com.yl.deepseekxunfei.model.BaseChildModel;
import com.yl.deepseekxunfei.model.SceneModel;
import com.yl.deepseekxunfei.utlis.SceneTypeConst;

public class MusicScene extends BaseChildScene{
    @Override
    public  BaseChildModel parseSceneToChild(SceneModel sceneModel) {
        String text = sceneModel.getText();
        BaseChildModel baseChildModel = new BaseChildModel();
        baseChildModel.setText(text);
        baseChildModel.setType(SceneTypeConst.MUSIC);
        return baseChildModel;
    }
}
