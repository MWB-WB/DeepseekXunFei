package com.yl.deepseekxunfei.scene;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import com.yl.deepseekxunfei.model.BaseChildModel;
import com.yl.deepseekxunfei.model.SceneModel;
import com.yl.deepseekxunfei.model.WeatherChildMode;
import com.yl.deepseekxunfei.utlis.CityDictionary;
import com.yl.deepseekxunfei.utlis.SceneTypeConst;

import java.util.List;

public class WeatherScene extends BaseChildScene {

    private static final Segment SEGMENT = HanLP.newSegment()
            .enablePlaceRecognize(true); // 启用地名识别

    @Override
    public BaseChildModel parseSceneToChild(SceneModel sceneModel) {
        String text = sceneModel.getText();
        WeatherChildMode weatherSceneModel = new WeatherChildMode();
        List<Term> terms = SEGMENT.seg(text);
        for (Term term : terms) {
            String word = term.word;
            if (CityDictionary.isCity(word)) {
                weatherSceneModel.setCity(word);
            }
        }
        weatherSceneModel.setText(text);
        if (text.contains("最近") || text.contains("近几天") || text.contains("这几天")) {
            weatherSceneModel.addType(SceneTypeConst.FEATHER_WEATHER);

        } else {
            weatherSceneModel.addType(SceneTypeConst.TODAY_WEATHER);
        }
        return weatherSceneModel;
    }

}
