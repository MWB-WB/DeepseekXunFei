package com.yl.deepseekxunfei.scene;

import android.util.Log;

import com.yl.deepseekxunfei.model.BaseChildModel;
import com.yl.deepseekxunfei.model.OpenAppChildMode;
import com.yl.deepseekxunfei.model.SceneModel;
import com.yl.ylcommon.ylsceneconst.SceneTypeConst;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class OpenAppScene extends BaseChildScene {

    private static final String[] appList = {"酷我音乐", "高德地图", "地图", "酷狗音乐", "设置"};
    private static final String[] appPkgList = {"cn.kuwo.kwmusiccar", "com.autonavi.amapauto", "com.autonavi.amapauto",
            "com.kugou.android.auto", "com.android.settings"};

    @Override
    public BaseChildModel parseSceneToChild(SceneModel sceneModel) {
        String text = sceneModel.getText();
        OpenAppChildMode appChildMode = new OpenAppChildMode();
        appChildMode.setText(text);
        String app = text.substring(2).replaceAll("。", "");
        Log.e("TAGTEST", "parseSceneToChild: " + app);
        AtomicInteger index = new AtomicInteger(0);
        Optional<String> first = Arrays.stream(appList).filter(t -> {
            index.getAndIncrement();//每比对一个元素，数值加1
            return t.equals(app);
        }).findFirst();
        if (first.isPresent()) {
            appChildMode.setType(SceneTypeConst.OPEN_APP);
            appChildMode.setAppName(first.get());
            Log.e("TAGTEST", "parseSceneToChild: " + first.get() + ":: " + index.get());
            appChildMode.setAppPkgName(appPkgList[index.get() - 1]);
        } else {
            appChildMode.setType(SceneTypeConst.UNKNOWN_OPEN_APP);
        }
        return appChildMode;
    }
}
