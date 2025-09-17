package com.yl.deepseekxunfei.scene.utils;

import com.yl.deepseekxunfei.model.BaseChildModel;
import com.yl.deepseekxunfei.model.NavControlChildModel;
import com.yl.deepseekxunfei.model.SceneModel;
import com.yl.deepseekxunfei.scene.BaseChildScene;
import com.yl.ylcommon.ylenum.NAV_CONTROL;
import com.yl.ylcommon.ylsceneconst.SceneTypeConst;

import java.text.SimpleDateFormat;

import java.util.Date;


//获取当前时间
public class DateTime extends BaseChildScene {
    public String dateTime(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        return "当前时间是："+simpleDateFormat.format(date);
    }

    @Override
    public BaseChildModel parseSceneToChild(SceneModel sceneModel) {
        String text = sceneModel.getText().replace("。", "");
        NavControlChildModel navControlChildModel = new NavControlChildModel();
        navControlChildModel.setText(text);
        navControlChildModel.setType(SceneTypeConst.DATETIME);
        return navControlChildModel;
    }
}
