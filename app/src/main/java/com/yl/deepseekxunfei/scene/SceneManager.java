package com.yl.deepseekxunfei.scene;

import android.util.Log;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import com.yl.deepseekxunfei.model.BaseChildModel;
import com.yl.deepseekxunfei.model.SceneModel;
import com.yl.deepseekxunfei.model.WordNLPModel;
import com.yl.deepseekxunfei.sceneenum.SceneType;
import com.yl.deepseekxunfei.utlis.BotConstResponse;
import com.yl.deepseekxunfei.utlis.ChineseSegmentationUtil;
import com.yl.deepseekxunfei.utlis.OptionPositionParser;
import com.yl.deepseekxunfei.utlis.SceneTypeConst;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SceneManager {

    private WeatherScene weatherScene;
    private NavScene navScene;
    private MovieScene movieScene;
    private VideoScene videoScene;
    private MusicScene musicScene;
    private SceneType currentSceneType = SceneType.CHITCHAT;
    // 连接词集合（可扩展）
    private static final Set<String> CONJUNCTIONS = new HashSet<>(Arrays.asList(
            "然后", "之后", "接着", "再", "随后", "并", "和", "及", "还有"
    ));
    private static final Segment SEGMENT = HanLP.newSegment()
            .enablePlaceRecognize(true); // 启用地名识别

    public SceneManager() {
        initChildScene();
    }

    private void initChildScene() {
        weatherScene = new WeatherScene();
        navScene = new NavScene();
        movieScene = new MovieScene();
        videoScene = new VideoScene();
        musicScene = new MusicScene();
    }

    public List<BaseChildModel> parseToScene(String text) {
        List<SceneModel> sceneModels = parseQuestionToScene(text);
        return distributeScene(sceneModels);
    }

    //解析场景
    private List<SceneModel> parseQuestionToScene(String text) {
        List<SceneModel> sceneModelList = new ArrayList<>();
        List<Term> terms = SEGMENT.seg(text);
        Optional<Term> first = terms.stream().filter(term -> CONJUNCTIONS.contains(term.word)).findFirst();
        //如果有多分词，则会将给语句拆分成多个场景
        if (!first.isEmpty()) {
            String[] textSplit = text.split(first.get().word);
            WordNLPModel wordNLPModel = ChineseSegmentationUtil.SegmentWords(textSplit[0]);
            SceneModel sceneModel = getSceneModel(wordNLPModel, textSplit[0]);
            sceneModelList.add(sceneModel);
            WordNLPModel wordNLPModel1 = ChineseSegmentationUtil.SegmentWords(textSplit[1]);
            SceneModel sceneModel1 = getSceneModel(wordNLPModel1, textSplit[1]);
            sceneModelList.add(sceneModel1);
            if (sceneModel.getScene() == SceneType.NAVIGATION) {
                currentSceneType = SceneType.NAVIGATION;
            } else {
                currentSceneType = sceneModel1.getScene();
            }
        } else {
            WordNLPModel wordNLPModel = ChineseSegmentationUtil.SegmentWords(text);
            SceneModel sceneModel = getSceneModel(wordNLPModel, text);
            sceneModelList.add(sceneModel);
            currentSceneType = sceneModel.getScene();
        }
        return sceneModelList;
    }

    private SceneModel getSceneModel(WordNLPModel wordNLPModel, String text) {
        Log.e("test111", "getSceneModel: " + wordNLPModel.toString() + ":: text: " + text);
        SceneModel resultModel = new SceneModel();
        resultModel.setText(text);
        if (wordNLPModel.getV().contains("导航") || wordNLPModel.getV().contains("去") || wordNLPModel.getV().contains("到") || wordNLPModel.getF().contains("附近")) {
            resultModel.setScene(SceneType.NAVIGATION);
        } else if (text.startsWith("播放") || text.contains("音乐") || text.startsWith("我要听")) {
            resultModel.setScene(SceneType.MUSIC);
        } else if (text.contains("当前位置") || text.contains("我在哪")) {
            resultModel.setScene(SceneType.LOCATION);
        } else if (wordNLPModel.getN().contains("天气")) {
            resultModel.setScene(SceneType.WEATHER);
        } else if (wordNLPModel.getN().contains("电影")) {
            resultModel.setScene(SceneType.MOVIE);
        } else if (wordNLPModel.getN().contains("视频")) {
            resultModel.setScene(SceneType.VIDEO);
        } else if (OptionPositionParser.parsePosition(text)) {
            if (currentSceneType.equals(SceneType.NAVIGATION) || currentSceneType.equals(SceneType.MUSIC)) {
                resultModel.setScene(SceneType.SELECTION);
            } else {
                resultModel.setScene(SceneType.CHITCHAT);
            }
        } else if (!Arrays.stream(BotConstResponse.quitCommand).filter(s -> s.contains(text)).findFirst().isEmpty()) {
            resultModel.setScene(SceneType.QUIT);
        } else {
            resultModel.setScene(SceneType.CHITCHAT);
        }
        return resultModel;
    }


    //分发场景
    private List<BaseChildModel> distributeScene(List<SceneModel> sceneModelList) {
        List<BaseChildModel> baseChildModelList = new ArrayList<>();
        for (SceneModel sceneModel : sceneModelList) {
            baseChildModelList.add(getChildModel(sceneModel));
        }
        return baseChildModelList;
    }

    public SceneType getCurrentSceneType() {
        return currentSceneType;
    }

    private BaseChildModel getChildModel(SceneModel sceneModel) {
        BaseChildModel baseChildModel;
        switch (sceneModel.getScene()) {
            case WEATHER:
                baseChildModel = weatherScene.parseSceneToChild(sceneModel);
                break;
            case NAVIGATION:
                baseChildModel = navScene.parseSceneToChild(sceneModel);
                if (baseChildModel.getType() == SceneTypeConst.CHITCHAT) {
                    currentSceneType = SceneType.CHITCHAT;
                }
                break;
            case MOVIE:
                baseChildModel = movieScene.parseSceneToChild(sceneModel);
                break;
            case VIDEO:
                baseChildModel = videoScene.parseSceneToChild(sceneModel);
                break;
            case MUSIC:
                baseChildModel = musicScene.parseSceneToChild(sceneModel);
                break;
            case SELECTION:
                baseChildModel = new BaseChildModel();
                baseChildModel.setType(SceneTypeConst.SELECTION);
                baseChildModel.setText(sceneModel.getText());
                break;
            case QUIT:
                baseChildModel = new BaseChildModel();
                baseChildModel.setType(SceneTypeConst.QUIT);
                baseChildModel.setText(sceneModel.getText());
                break;
            default:
                baseChildModel = new BaseChildModel();
                baseChildModel.setType(SceneTypeConst.CHITCHAT);
                baseChildModel.setText(sceneModel.getText());
                break;
        }
        return baseChildModel;
    }


}
