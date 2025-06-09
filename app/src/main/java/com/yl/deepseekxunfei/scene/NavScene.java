package com.yl.deepseekxunfei.scene;

import android.util.Log;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import com.yl.deepseekxunfei.model.BaseChildModel;
import com.yl.deepseekxunfei.model.NavChildMode;
import com.yl.deepseekxunfei.model.SceneModel;
import com.yl.deepseekxunfei.utlis.CityDictionary;
import com.yl.deepseekxunfei.utlis.SceneTypeConst;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NavScene extends BaseChildScene {
   public static String addressLocation;


    private final Segment SEGMENT = HanLP.newSegment()
            .enablePlaceRecognize(true); // 启用地名识别

    @Override
    public BaseChildModel parseSceneToChild(SceneModel sceneModel) {
        NavChildMode navChildMode = new NavChildMode();
        String text = sceneModel.getText();
        List<Term> terms = SEGMENT.seg(text);
        List<NavChildMode.GeoEntity> entities = new ArrayList<>();
        for (Term term : terms) {
            String word = term.word;
            String nature = term.nature.toString();

            NavChildMode.GeoEntityType type = determineGeoType(word, nature);
            if (type != NavChildMode.GeoEntityType.UNKNOWN) {
                entities.add(new NavChildMode.GeoEntity(word, type));
            }
        }
        navChildMode.setEntities(entities);
        if (isNearbySearch(entities)) {
            Log.d("导航地址", "parseSceneToChild: "+entities);
            navChildMode.setLocation(extractLocation(terms));
            navChildMode.setType(SceneTypeConst.NEARBY);
        } else {
            Log.d("地址1", "parseSceneToChild: "+sceneModel.getText());
            Log.d("地址1", "parseSceneToChild: "+text);
            String address = extractLocation(text);
            Log.d("地址2", "parseSceneToChild: "+address);
            if (address.isEmpty() || address.equals("。") || address.equals("导航到")) {
                navChildMode.setType(SceneTypeConst.NAVIGATION_UNKNOWN_ADDRESS);
                NavScene.addressLocation = address;
            } else {
                Log.d("地址3", "parseSceneToChild: "+ extractLocation(address));
                NavScene.addressLocation = address;
                navChildMode.setLocation(extractLocation(address));
                navChildMode.setType(SceneTypeConst.KEYWORD);
            }
        }
        navChildMode.setText(text);
        return navChildMode;
    }

    // 提取地点
    private String extractLocation(List<Term> terms) {
        StringBuilder location = new StringBuilder();
        for (Term term : terms) {
            // 识别地名（需要加载地名词典）
            if (term.nature.toString().startsWith("ns") ||
                    term.nature.toString().startsWith("nl")) {
                location.append(term.word);
            }
            // 补充识别POI（需加载自定义词典）
            else if (term.nature.toString().equals("nz")) {
                location.append(term.word);
            } else if (term.nature.toString().equals("n")) {
                location.append(term.word);
            }
        }
        return location.toString();
    }

    private String extractLocation(String input) {
        // 匹配 "导航到XXX"、"去XXX"、"我要去XXX" 等模式
        String pattern = "(导航到|去|我要去|带我去|帮我找|附近有|我想去|附近的|导航去|导航)(.+?)(。|$)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(input);
        if (m.find()) {
            Log.d("地名", "extractLocation: " + m.group(2).trim());
            return m.group(2).trim(); // 返回匹配的地名
        }
        Log.d("返回", "extractLocation: "+input);
        return input; // 如果没有匹配到，返回原输入（可能已经是纯地名）
    }

    private NavChildMode.GeoEntityType determineGeoType(String word, String nature) {
        // 规则1：优先匹配城市
        if (CityDictionary.isCity(word)) {
            return NavChildMode.GeoEntityType.CITY;
        }

        // 规则2：地名词性识别（ns=地名）
        if (nature.startsWith("ns")) {
            return NavChildMode.GeoEntityType.SPECIFIC_PLACE;
        }

        if (nature.startsWith("nz")) {
            return NavChildMode.GeoEntityType.NATION_Z;
        }

        if (nature.startsWith("n")) {
            return NavChildMode.GeoEntityType.NATION;
        }

        // 规则3：泛指区域关键词
        if (isGeneralArea(word)) {
            return NavChildMode.GeoEntityType.GENERAL_AREA;
        }

        return NavChildMode.GeoEntityType.UNKNOWN;
    }

    private boolean isGeneralArea(String word) {
        return word.matches("附近|周围|周边|这里|那里|当前位置");
    }

    private boolean isNearbySearch(List<NavChildMode.GeoEntity> entities) {
        return entities.stream()
                .anyMatch(e -> e.getType() == NavChildMode.GeoEntityType.GENERAL_AREA);
    }

}
