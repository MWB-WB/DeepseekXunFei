package com.yl.deepseekxunfei.scene;

import com.yl.deepseekxunfei.model.BaseChildModel;
import com.yl.deepseekxunfei.model.MusicControlChildModel;
import com.yl.deepseekxunfei.model.SceneModel;
import com.yl.ylcommon.ylenum.MUSIC_CONTROL;
import com.yl.ylcommon.ylsceneconst.SceneTypeConst;

public class MusicControlScene extends BaseChildScene {

    @Override
    public BaseChildModel parseSceneToChild(SceneModel sceneModel) {
        String text = sceneModel.getText().replace("。", "");
        MusicControlChildModel model = new MusicControlChildModel();
        model.setText(text);
        model.setType(SceneTypeConst.CONTROL_MUSIC);
        if (text.equals("继续播放")) {
            model.setControl(MUSIC_CONTROL.PLAY);
        } else if (text.equals("暂停播放")) {
            model.setControl(MUSIC_CONTROL.PAUSE);
        } else if (text.equals("上一首")) {
            model.setControl(MUSIC_CONTROL.PREV);
        } else if (text.equals("下一首")) {
            model.setControl(MUSIC_CONTROL.NEXT);
        }
        return model;
    }

}
