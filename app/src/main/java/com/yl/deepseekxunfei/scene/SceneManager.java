package com.yl.deepseekxunfei.scene;

import android.content.Context;
import android.util.Log;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import com.yl.deepseekxunfei.scene.utils.GoHomeOrWorkProcessing;
import com.yl.deepseekxunfei.model.BaseChildModel;
import com.yl.deepseekxunfei.model.SceneModel;
import com.yl.deepseekxunfei.scene.utils.SceneJudgeUtil;
import com.yl.ylcommon.utlis.WordNLPModel;
import com.yl.ylcommon.utlis.BotConstResponse;
import com.yl.ylcommon.utlis.ChineseSegmentationUtil;
import com.yl.ylcommon.utlis.OptionPositionParser;
import com.yl.gaodeApi.poi.LocationValidator;
import com.yl.ylcommon.ylenum.SceneType;
import com.yl.ylcommon.ylsceneconst.SceneTypeConst;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class SceneManager {

    private WeatherScene weatherScene;
    private NavScene navScene;
    private MovieScene movieScene;
    private VideoScene videoScene;
    private MusicScene musicScene;
    private ComputeScene computeScene;
    private OpenAppScene openAppScene;
    private MusicControlScene musicControlScene;
    private NavControlScene navControlScene;
    private boolean isMultiIntent = false;
    private List<SceneType> lastSceneTypeList = new ArrayList<>();
    private List<SceneType> sceneTypeList = new ArrayList<>();
    private List<BaseChildModel> lastBaseChildModelList = new ArrayList<>();
    private List<BaseChildModel> baseChildModelList = new ArrayList<>();
    private LocationValidator locationValidator;
    private Context mContext;
    private CountDownLatch countDownLatch;

    // 连接词集合（可扩展）
    private static final Set<String> CONJUNCTIONS = new HashSet<>(Arrays.asList(
            "然后", "之后", "接着", "随后"
    ));
    private static final Segment SEGMENT = HanLP.newSegment()
            .enablePlaceRecognize(true); // 启用地名识别

    public SceneManager(Context context) {
        mContext = context;
        locationValidator = new LocationValidator(mContext);
        countDownLatch = new CountDownLatch(1);
        initChildScene();
    }

    private void initChildScene() {
        weatherScene = new WeatherScene();
        navScene = new NavScene(mContext);
        movieScene = new MovieScene();
        videoScene = new VideoScene();
        musicScene = new MusicScene();
        computeScene = new ComputeScene();
        openAppScene = new OpenAppScene();
        musicControlScene = new MusicControlScene();
        navControlScene = new NavControlScene();
    }

    public List<BaseChildModel> parseToScene(String text) {
        List<SceneModel> sceneModels = parseQuestionToScene(text);
        List<BaseChildModel> childModels = distributeScene(sceneModels);
        baseChildModelList.addAll(childModels);
        return childModels;
    }

    //解析场景
    private List<SceneModel> parseQuestionToScene(String text) {
        List<SceneModel> sceneModelList = new ArrayList<>();
        List<Term> terms = SEGMENT.seg(text);
        Optional<Term> first = terms.stream().filter(term -> CONJUNCTIONS.contains(term.word)).findFirst();
        //需要将上次的场景存储到列表中
        lastSceneTypeList.clear();
        lastSceneTypeList.addAll(sceneTypeList);
        sceneTypeList.clear();
        lastBaseChildModelList.clear();
        lastBaseChildModelList.addAll(baseChildModelList);
        baseChildModelList.clear();
        //如果有多分词，则会将给语句拆分成多个场景
        if (!first.isEmpty()) {
            isMultiIntent = true;
            String[] textSplit = text.split(first.get().word);
            Log.e("TAG", "parseQuestionToScene:111 " + textSplit);
            WordNLPModel wordNLPModel = ChineseSegmentationUtil.SegmentWords(textSplit[0]);
            SceneModel sceneModel = getSceneModel(wordNLPModel, textSplit[0]);
            sceneModelList.add(sceneModel);
            sceneTypeList.add(sceneModel.getScene());
            WordNLPModel wordNLPModel1 = ChineseSegmentationUtil.SegmentWords(textSplit[1]);
            SceneModel sceneModel1 = getSceneModel(wordNLPModel1, textSplit[1]);
            sceneModelList.add(sceneModel1);
            sceneTypeList.add(sceneModel1.getScene());
        } else {
            isMultiIntent = false;
            WordNLPModel wordNLPModel = ChineseSegmentationUtil.SegmentWords(text);
            SceneModel sceneModel = getSceneModel(wordNLPModel, text);
            sceneModelList.add(sceneModel);
            sceneTypeList.add(sceneModel.getScene());
        }
        return sceneModelList;
    }

    private SceneModel getSceneModel(WordNLPModel wordNLPModel, String text) {

        SceneModel resultModel = new SceneModel();
        resultModel.setText(text);
        //可能有上下问关系的场景，主要用来处理一些特殊的逻辑
        SceneModel sceneModel = judgeSceneWithContext(text);
        //如果不为null，直接返回
        if (sceneModel != null) {
            return sceneModel;
        }
        Log.d("sceneModelGetSceneModel", "getSceneModel: " + sceneModel);
        if (SceneJudgeUtil.getInstance().judgeIsOpenApp(text)) {
            resultModel.setScene(SceneType.OPENAPP);
        } else if (SceneJudgeUtil.getInstance().judgeIsNavControl(text)) {
            resultModel.setScene(SceneType.CONTROLNAV);
        } else if (wordNLPModel.getV().contains("导航") || wordNLPModel.getVn().contains("导航") || text.contains("我要去") || text.contains("我想去") || wordNLPModel.getF().contains("附近")) {
            if (text.contains("攻略") || text.contains("规划") || text.contains("计划")) {
                resultModel.setScene(SceneType.CHITCHAT);
            } else {
                resultModel.setScene(SceneType.NAVIGATION);
            }
        } else if (text.startsWith("播放") || text.contains("音乐") ||
                text.startsWith("我要听") || text.contains("歌") || text.contains("想听")
                || text.startsWith("来一首") || text.contains("今日推荐音乐")) {
            if (SceneJudgeUtil.getInstance().handlePlayCommand(text)) {
                resultModel.setScene(SceneType.MUSIC);
            } else {
                resultModel.setScene(SceneType.CHITCHAT);
            }
        } else if (SceneJudgeUtil.getInstance().isLocationQuery(text)) {
            Log.d("位置", "getSceneModel: 当前位置");
            resultModel.setScene(SceneType.LOCATION);
        } else if (wordNLPModel.getN().contains("天气")) {
            resultModel.setScene(SceneType.WEATHER);
        } else if (wordNLPModel.getN().contains("电影")) {
            resultModel.setScene(SceneType.MOVIE);
        } else if (wordNLPModel.getN().contains("视频")) {
            resultModel.setScene(SceneType.VIDEO);
        }
//        else if (wordNLPModel.getN().contains("笑话")) {
//            Log.d("笑话", "SceneManager: 笑话");
//            resultModel.setScene(SceneType.JOKE);
//        //修改为完全匹配，忽略标点符号
//        }
        else if (!Arrays.stream(BotConstResponse.quitCommand)
                .filter(s -> s.replaceAll("[\\p{P}\\s]", "").equalsIgnoreCase(text.replaceAll("[\\p{P}\\s]", "")))
                .findFirst()
                .isEmpty()) {
            resultModel.setScene(SceneType.QUIT);
        } else if (!Arrays.stream(BotConstResponse.stopCommand).filter(s -> s.contains(text)).findFirst().isEmpty()) {
            resultModel.setScene(SceneType.STOP);
        } else if (SceneJudgeUtil.getInstance().isCalculationQuestion(text)) {
            resultModel.setScene(SceneType.COMPUTE);
        } else if (wordNLPModel.getV().contains("设置") && wordNLPModel.getN().contains("公司") || wordNLPModel.getQ().contains("家")) {
            resultModel.setScene(SceneType.SETHOMECOMPANY);
        } else if (GoHomeOrWorkProcessing.recognizeIntent(text).equals("home") || GoHomeOrWorkProcessing.recognizeIntent(text).equals("work")) {
            resultModel.setScene(SceneType.GOHOMETOWORK);
        } else if (SceneJudgeUtil.getInstance().judgeIsMusicControl(text)) {
            resultModel.setScene(SceneType.CONTROLMUSIC);
        }
//        else if (isSelfIntroduction(text)) {
//            resultModel.setScene(SceneType.SELFINTRODUCE);
//        }
        else {
            resultModel.setScene(SceneType.CHITCHAT);
        }
        return resultModel;
    }

    private SceneModel judgeSceneWithContext(String text) {
        SceneModel sceneModel = null;
        //如果上一次的场景里包含了导航、音乐，电影并且此次的是选项则走选择场景
        if (lastSceneTypeList.contains(SceneType.NAVIGATION) || lastSceneTypeList.contains(SceneType.MUSIC) || lastSceneTypeList.contains(SceneType.MOVIE)) {
            if (OptionPositionParser.parsePosition(text)) {
                sceneModel = new SceneModel();
                sceneModel.setText(text);
                sceneModel.setScene(SceneType.SELECTION);
            }
        }
        if (!isMultiIntent) {
            if (lastSceneTypeList.contains(SceneType.WEATHER)) {
                if (lastBaseChildModelList.get(0).getType() == SceneTypeConst.TODAY_WEATHER) {
                    if (text.contains("明天") || text.contains("后天") || text.contains("之后") || text.contains("过几天")) {
                        sceneModel = new SceneModel();
                        sceneModel.setText(text);
                        sceneModel.setScene(SceneType.WEATHER);
                    }
                }
            } else if (lastSceneTypeList.contains(SceneType.NAVIGATION)) {
                if (lastBaseChildModelList.get(0).getType() == SceneTypeConst.NAVIGATION_UNKNOWN_ADDRESS) {
                    var ref = new Object() {
                        boolean isTextValid = false;
                    };
                    locationValidator.validateAddress(text, isValid -> {
                        ref.isTextValid = isValid;
                        countDownLatch.countDown();
                    });
                    try {
                        // 主线程等待子线程完成
                        countDownLatch.await();
                        // 子线程执行完后，更新 UI
                        if (ref.isTextValid) {
                            sceneModel = new SceneModel();
                            sceneModel.setText(text);
                            sceneModel.setScene(SceneType.NAVIGATION);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
        return sceneModel;
    }


    //分发场景
    private List<BaseChildModel> distributeScene(List<SceneModel> sceneModelList) {
        List<BaseChildModel> baseChildModelList = new ArrayList<>();
        for (SceneModel sceneModel : sceneModelList) {
            baseChildModelList.add(getChildModel(sceneModel));
        }
        return baseChildModelList;
    }

    private BaseChildModel getChildModel(SceneModel sceneModel) {
        Log.d("TAG", "getChildModel: " + sceneModel.getScene());
        BaseChildModel baseChildModel;
        switch (sceneModel.getScene()) {
            case WEATHER:
                baseChildModel = weatherScene.parseSceneToChild(sceneModel);
                break;
            case NAVIGATION:
                baseChildModel = navScene.parseSceneToChild(sceneModel);
                break;
            case MOVIE://电影
                baseChildModel = movieScene.parseSceneToChild(sceneModel);
                break;
            case VIDEO:
                baseChildModel = videoScene.parseSceneToChild(sceneModel);
                break;
            case MUSIC://音乐
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
            case STOP:
                baseChildModel = new BaseChildModel();
                baseChildModel.setType(SceneTypeConst.STOP);
                baseChildModel.setText(sceneModel.getText());
                break;
            case COMPUTE:
                baseChildModel = computeScene.parseSceneToChild(sceneModel);
                break;
            case SELFINTRODUCE:
                baseChildModel = new BaseChildModel();
                baseChildModel.setType(SceneTypeConst.SELFINTRODUCE);
                baseChildModel.setText(sceneModel.getText());
                break;
            case SETHOMECOMPANY:
                baseChildModel = new BaseChildModel();
                baseChildModel.setType(SceneTypeConst.HOMECOMPANY);
                baseChildModel.setText(sceneModel.getText());
                break;
            case GOHOMETOWORK:
                baseChildModel = new BaseChildModel();
                baseChildModel.setType(SceneTypeConst.GOHOMETOWORK);
                baseChildModel.setText(sceneModel.getText());
                break;
            case LOCATION:
                baseChildModel = new BaseChildModel();
                baseChildModel.setType(SceneTypeConst.LOCATIONCONST);
                baseChildModel.setText(sceneModel.getText());
                break;
            case OPENAPP:
                baseChildModel = openAppScene.parseSceneToChild(sceneModel);
                break;
            case CONTROLMUSIC:
                baseChildModel = musicControlScene.parseSceneToChild(sceneModel);
                break;
            case CONTROLNAV:
                baseChildModel = navControlScene.parseSceneToChild(sceneModel);
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
