package com.yl.deepseekxunfei.scene;

import com.yl.deepseekxunfei.model.BaseChildModel;
import com.yl.deepseekxunfei.model.NavControlChildModel;
import com.yl.deepseekxunfei.model.SceneModel;
import com.yl.ylcommon.ylenum.NAV_CONTROL;
import com.yl.ylcommon.ylsceneconst.SceneTypeConst;

public class NavControlScene extends BaseChildScene {


    @Override
    public BaseChildModel parseSceneToChild(SceneModel sceneModel) {
        String text = sceneModel.getText().replace("。", "");
        NavControlChildModel navControlChildModel = new NavControlChildModel();
        navControlChildModel.setText(text);
        navControlChildModel.setType(SceneTypeConst.CONTROL_NAV);
        if (text.equals("退出导航")) {
            navControlChildModel.setNavControl(NAV_CONTROL.EXIT_NAV);
        } else if (text.equals("继续导航")) {
            navControlChildModel.setNavControl(NAV_CONTROL.CONTINUE_NAV);
        }
        return navControlChildModel;
    }

}
