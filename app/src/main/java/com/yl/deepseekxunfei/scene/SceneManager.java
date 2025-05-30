package com.yl.deepseekxunfei.scene;

import android.content.Context;
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
import com.yl.deepseekxunfei.utlis.LocationValidator;
import com.yl.deepseekxunfei.utlis.OptionPositionParser;
import com.yl.deepseekxunfei.utlis.SceneTypeConst;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SceneManager {

    private WeatherScene weatherScene;
    private NavScene navScene;
    private MovieScene movieScene;
    private VideoScene videoScene;
    private MusicScene musicScene;
    private ComputeScene computeScene;
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
        navScene = new NavScene();
        movieScene = new MovieScene();
        videoScene = new VideoScene();
        musicScene = new MusicScene();
        computeScene = new ComputeScene();
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
        if (wordNLPModel.getV().contains("导航") || wordNLPModel.getVn().contains("导航")
                || text.contains("我要去") || text.contains("我想去") || text.contains("去") || wordNLPModel.getF().contains("附近")) {
            if (text.contains("攻略") || text.contains("规划") || text.contains("计划")) {
                resultModel.setScene(SceneType.CHITCHAT);
            } else {
                resultModel.setScene(SceneType.NAVIGATION);
            }
        } else if (text.startsWith("播放") || text.contains("音乐") || text.startsWith("我要听") || text.contains("歌")) {
            resultModel.setScene(SceneType.MUSIC);
        } else if (text.contains("当前位置") || text.contains("我在哪")) {
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
        } else if (isCalculationQuestion(text)) {
            resultModel.setScene(SceneType.COMPUTE);
        } else if (isSelfIntroduction(text)) {
            resultModel.setScene(SceneType.SELFINTRODUCE);
        } else {
            resultModel.setScene(SceneType.CHITCHAT);
        }
        return resultModel;
    }

    private SceneModel judgeSceneWithContext(String text) {
        SceneModel sceneModel = null;
        //如果上一次的场景里包含了导航、音乐，并且此次的是选项则走选择场景
        if (lastSceneTypeList.contains(SceneType.NAVIGATION) || lastSceneTypeList.contains(SceneType.MUSIC)) {
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
                            sceneModel.setText("导航到" + text);
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
        BaseChildModel baseChildModel;
        switch (sceneModel.getScene()) {
            case WEATHER:
                baseChildModel = weatherScene.parseSceneToChild(sceneModel);
                break;
            case NAVIGATION:
                baseChildModel = navScene.parseSceneToChild(sceneModel);
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
            case COMPUTE:
                baseChildModel = computeScene.parseSceneToChild(sceneModel);
                break;
            case SELFINTRODUCE:
                baseChildModel = new BaseChildModel();
                baseChildModel.setType(SceneTypeConst.SELFINTRODUCE);
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

    // 定义自我介绍相关的关键词和句式（支持中英文符号和变体）
    private final String[] SELF_INTRO_KEYWORDS = {
            "你叫什么", "你的名字", "你是谁", "介绍一下你", "介绍一下自己",
            "如何称呼你", "自我介绍一下", "你是做什么的", "你的功能", "你能做什么", "你是什么东西"
    };

    // 正则表达式匹配变体句式（允许中间有空格、标点、语气词）
    private final String SELF_INTRO_PATTERN =
            "^(.*?)(你[的名称谁岁绍能]|介绍|称呼|身份|功能|自己)(.*?)(吗|呢|啊|呀|吧|？)?$";

    // 判断是否为自我介绍问题
    private boolean isSelfIntroduction(String input) {
        String cleanedInput = input.trim()
                .replaceAll("[\\s\\p{P}]", "") // 移除所有空格和标点
                .toLowerCase();

        // 1. 检查是否包含关键词
        for (String keyword : SELF_INTRO_KEYWORDS) {
            if (cleanedInput.contains(keyword)) {
                return true;
            }
        }

        // 2. 正则表达式匹配句式
        Pattern pattern = Pattern.compile(SELF_INTRO_PATTERN, Pattern.CASE_INSENSITIVE);
        return pattern.matcher(cleanedInput).matches();
    }

    // 正则表达式匹配计算场景
    private final String CALC_PATTERN =
            "^\\s*([-+]?\\d+\\.?\\d*)\\s*" +
                    "([+＋×xX*\\-\\－÷/除乘加减]|加|减|乘|除)" +
                    "\\s*([-+]?\\d+\\.?\\d*)\\s*" +
                    "(?:等于几|等于多少|是多少|结果是多少|得几)?\\s*[？?]?\\s*$";

    // 判断是否为计算问题
    private boolean isCalculationQuestion(String input) {
        Pattern pattern = Pattern.compile(CALC_PATTERN);
        Matcher matcher = pattern.matcher(input.trim());
        return matcher.matches();
    }


}
