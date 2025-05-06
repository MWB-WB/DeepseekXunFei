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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class NavScene extends BaseChildScene {

    // 连接词集合（可扩展）
    private static final Set<String> CONJUNCTIONS = new HashSet<>(Arrays.asList(
            "然后", "之后", "接着", "再", "随后", "并", "和", "及", "还有"
    ));
    private static final Segment SEGMENT = HanLP.newSegment()
            .enablePlaceRecognize(true); // 启用地名识别

    @Override
    public BaseChildModel parseSceneToChild(SceneModel sceneModel) {
        NavChildMode navChildMode = new NavChildMode();
        String text = sceneModel.getText();
        List<Term> terms = SEGMENT.seg(text);
        Optional<Term> first = terms.stream().filter(term -> CONJUNCTIONS.contains(term.word)).findFirst();
//        if (first.isEmpty()) {
            List<NavChildMode.GeoEntity> entities = new ArrayList<>();
            for (Term term : terms) {
                String word = term.word;
                Log.e("TAG", "parseSceneToChild: " + word);
                String nature = term.nature.toString();

                NavChildMode.GeoEntityType type = determineGeoType(word, nature);
                if (type != NavChildMode.GeoEntityType.UNKNOWN) {
                    entities.add(new NavChildMode.GeoEntity(word, type));
                }
            }
            navChildMode.setMorePosition(false);
            navChildMode.setLocation(extractLocation(terms));
            navChildMode.setEntities(entities);
            if (isNearbySearch(entities)) {
                navChildMode.addType(SceneTypeConst.NEARBY);
            } else {
                navChildMode.addType(SceneTypeConst.KEYWORD);
            }
//        } else {
//            navChildMode.setMorePosition(true);
//            Log.e("TAG", "parseSceneToChild: " + first.get().word);
//            String[] split = text.split(first.get().word);
//            Map<Integer, List<Term>> termMap = new HashMap<>();
//            for (int i = 0; i < split.length; i++) {
//                termMap.put(i, SEGMENT.seg(split[i]));
//            }
//            for (int i = 0; i < termMap.size(); i++) {
//                List<NavChildMode.GeoEntity> entities = new ArrayList<>();
//                NavChildMode navChildMode1 = new NavChildMode();
//                for (Term term : termMap.get(i)) {
//                    String word = term.word;
//                    Log.e("TAG", "parseSceneToChild: " + word);
//                    String nature = term.nature.toString();
//
//                    NavChildMode.GeoEntityType type = determineGeoType(word, nature);
//                    if (type != NavChildMode.GeoEntityType.UNKNOWN) {
//                        entities.add(new NavChildMode.GeoEntity(word, type));
//                    }
//                }
//                navChildMode1.setLocation(extractLocation(terms));
//                navChildMode1.setEntities(entities);
//                if (isNearbySearch(entities)) {
//                    navChildMode1.addType(SceneTypeConst.NEARBY);
//                } else {
//                    navChildMode1.addType(SceneTypeConst.KEYWORD);
//                }
//                navChildMode.addNavChildMode(navChildMode1);
//            }
//        }
        navChildMode.setText(text);
        return navChildMode;
    }

    // 提取地点
    private static String extractLocation(List<Term> terms) {
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

    private static NavChildMode.GeoEntityType determineGeoType(String word, String nature) {
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

    private static boolean isGeneralArea(String word) {
        return word.matches("附近|周围|周边|这里|那里|当前位置");
    }

    private boolean isNearbySearch(List<NavChildMode.GeoEntity> entities) {
        return entities.stream()
                .anyMatch(e -> e.getType() == NavChildMode.GeoEntityType.GENERAL_AREA);
    }

}
