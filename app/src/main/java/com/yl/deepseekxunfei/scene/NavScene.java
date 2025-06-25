package com.yl.deepseekxunfei.scene;

import android.content.Context;
import android.util.Log;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import com.yl.deepseekxunfei.model.BaseChildModel;
import com.yl.deepseekxunfei.model.NavChildMode;
import com.yl.deepseekxunfei.model.SceneModel;
import com.yl.gaodeApi.poi.LocationValidator;
import com.yl.ylcommon.utlis.CityDictionary;
import com.yl.ylcommon.ylenum.SceneType;
import com.yl.ylcommon.ylsceneconst.SceneTypeConst;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NavScene extends BaseChildScene {
    public static String addressLocation;
    private LocationValidator locationValidator;
    public NavScene(Context context) {
        locationValidator = new LocationValidator(context);
    }

    private final Segment SEGMENT = HanLP.newSegment()
            .enablePlaceRecognize(true); // 启用地名识别

    @Override
    public BaseChildModel parseSceneToChild(SceneModel sceneModel) {
        NavChildMode navChildMode = new NavChildMode();
        CountDownLatch countDownLatch = new CountDownLatch(1);
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
            Log.d("导航地址", "parseSceneToChild: " + entities);
            navChildMode.setLocation(extractLocation(terms));
            navChildMode.setType(SceneTypeConst.NEARBY);
        } else {
            Log.d("地址1", "parseSceneToChild: " + text);
            String address = extractLocation(text);
            Log.d("地址2", "parseSceneToChild: " + address);
            if (address.isEmpty() || address.equals("。") || address.equals("导航到")) {
                navChildMode.setType(SceneTypeConst.NAVIGATION_UNKNOWN_ADDRESS);
                NavScene.addressLocation = address;
            } else {
                var ref = new Object() {
                    boolean isTextValid = false;
                };
                locationValidator.validateAddress(address, isValid -> {
                    ref.isTextValid = isValid;
                    Log.e("地址2", "parseSceneToChild: " + isValid);
                    countDownLatch.countDown();
                });
                try {
                    // 主线程等待子线程完成
                    countDownLatch.await();
                    Log.e("地址2", "parseSceneToChild123: " + ref.isTextValid);
                    // 子线程执行完后，更新 UI
                    if (ref.isTextValid) {
                        NavScene.addressLocation = address;
                        navChildMode.setLocation(address);
                        navChildMode.setType(SceneTypeConst.KEYWORD);
                    } else {
                        navChildMode.setType(SceneTypeConst.NAVIGATION_ADDRESS_INVALIDATOR);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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

    private String[] navStart = {"导航到", "去", "我要去", "带我去", "帮我找", "附近有", "我想去", "附近的", "导航去", "导航"};

    private String extractLocation(String input) {
        Optional<String> result = Arrays.asList(navStart).stream().filter(
                t -> input.contains(t)
        ).findFirst();
        String location = "";
        if (result.isPresent()) {
            String[] split = input.split(result.get());
            Log.e("result", "extractLocation: " + split.length);
            if (split.length > 1) {
                location = split[1];
            }
        } else {
            location = input;
        }
        Log.d("返回", "extractLocation: " + input);
        return location; // 如果没有匹配到，返回原输入（可能已经是纯地名）
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
        return word.matches("附近|周围|周边|这里|那里|当前位置|最近");
    }

    private boolean isNearbySearch(List<NavChildMode.GeoEntity> entities) {
        return entities.stream()
                .anyMatch(e -> e.getType() == NavChildMode.GeoEntityType.GENERAL_AREA);
    }

}
