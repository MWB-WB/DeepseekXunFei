package com.yl.deepseekxunfei.scene;

import com.yl.deepseekxunfei.model.BaseChildModel;
import com.yl.deepseekxunfei.model.SceneModel;
import com.yl.deepseekxunfei.utlis.JudgmentNavigation;
import com.yl.deepseekxunfei.utlis.SceneTypeConst;

public class NavScene extends BaseChildScene {

    public JudgmentNavigation judgmentNavigation;

    public NavScene() {
        judgmentNavigation = new JudgmentNavigation();
    }

    @Override
    public BaseChildModel parseSceneToChild(SceneModel sceneModel) {
        String text = sceneModel.getText();
        BaseChildModel baseChildModel = new BaseChildModel();
        baseChildModel.setText(text);
        if (judgmentNavigation.isKeywordNavigation(text)) {
            baseChildModel.setType(SceneTypeConst.KEYWORD);
        } else if (judgmentNavigation.isNearbySearch(text)) {
            baseChildModel.setType(SceneTypeConst.NEARBY);
        }
        return baseChildModel;
    }

}
