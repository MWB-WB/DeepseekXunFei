package com.yl.deepseekxunfei.scene;

import com.yl.deepseekxunfei.model.BaseChildModel;
import com.yl.deepseekxunfei.model.SceneModel;

public abstract class BaseChildScene {

    public abstract BaseChildModel parseSceneToChild(SceneModel sceneModel);

}
