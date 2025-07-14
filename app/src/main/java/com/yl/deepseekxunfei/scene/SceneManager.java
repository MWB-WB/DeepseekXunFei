package com.yl.deepseekxunfei.scene;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import com.yl.deepseekxunfei.scene.utils.GoHomeOrWorkProcessing;
import com.yl.deepseekxunfei.model.BaseChildModel;
import com.yl.deepseekxunfei.model.SceneModel;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        navScene = new NavScene(mContext);
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
        Log.d("sceneModelGetSceneModel", "getSceneModel: " + sceneModel);
        if (wordNLPModel.getV().contains("导航") || wordNLPModel.getVn().contains("导航") || text.contains("我要去") || text.contains("我想去") || wordNLPModel.getF().contains("附近")) {
            if (text.contains("攻略") || text.contains("规划") || text.contains("计划")) {
                resultModel.setScene(SceneType.CHITCHAT);
            } else {
                resultModel.setScene(SceneType.NAVIGATION);
            }
        } else if (text.startsWith("播放") || text.contains("音乐") ||
                text.startsWith("我要听") || text.contains("歌") || text.contains("想听")
                || text.startsWith("来一首") ||  text.contains("今日推荐音乐")) {
            if (handlePlayCommand(text)) {
                resultModel.setScene(SceneType.MUSIC);
            } else {
                resultModel.setScene(SceneType.CHITCHAT);
            }
        } else if (isLocationQuery(text)) {
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
        } else if (isCalculationQuestion(text)) {
            resultModel.setScene(SceneType.COMPUTE);
        } else if (wordNLPModel.getV().contains("设置") && wordNLPModel.getN().contains("公司") || wordNLPModel.getQ().contains("家")) {
            resultModel.setScene(SceneType.SETHOMECOMPANY);
        } else if (GoHomeOrWorkProcessing.recognizeIntent(text).equals("home") || GoHomeOrWorkProcessing.recognizeIntent(text).equals("work")) {
            resultModel.setScene(SceneType.GOHOMETOWORK);
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

    /**
     * 处理音乐播放指令（优化版）
     *
     * @param text 用户输入的指令文本
     */
    public boolean handlePlayCommand(String text) {
        // 1. 空指令检查
        if (TextUtils.isEmpty(text)) {
            Log.d("播放错误", "请说出您想听的歌曲，例如：播放晴天 或 我想听周杰伦的歌");
            return false;
        }

        // 2. 统一处理为小写并去除首尾空格
        String processedInput = text.trim().toLowerCase();

        // 3. 排除单字"歌"的情况
        if (processedInput.equals("歌")) {
            Log.d("播放错误", "请说出完整的歌曲名称哦~");
            return false;
        }

        // 4. 支持的指令前缀（可配置）
        String[] prefixes = {"播放", "我要听", "我想听", "来一首", "听一下", "放一首"};

        // 5. 检查是否匹配任一播放指令
        String matchedPrefix = null;
        for (String prefix : prefixes) {
            String lowerPrefix = prefix.toLowerCase();
            if (processedInput.startsWith(lowerPrefix)) {
                // 检查指令后是否有内容（排除"播放"后为空的情况）
                if (processedInput.length() > lowerPrefix.length()) {
                    matchedPrefix = prefix;
                    break;
                }
            }
        }

        // 6. 如果不是有效的播放指令，直接返回
        if (matchedPrefix == null) {
            return false;
        }

        // 7. 提取歌名部分（保留原始大小写）
        String songName = text.substring(matchedPrefix.length()).trim();

        // 8. 二次验证歌名有效性
        if (songName.isEmpty() || isInvalidSongName(songName)) {
            Log.d("播放错误", "请说出完整的歌曲名称，例如：" + matchedPrefix + "晴天");
            return false;
        }

        // 9. 过滤歌名中的特殊符号songName = cleanSongName(songName);

        // 10. 执行播放
        return true;
    }

    /**
     * 检查是否为无效歌名
     */
    private boolean isInvalidSongName(String name) {
        // 单字"歌"已在前置检查，这里主要检查其他无效情况
        return name.matches("^[\\s\\p{P}]*$"); // 全是空格或标点
    }

    /**
     * 清洗歌名中的特殊符号
     */
    private String cleanSongName(String name) {
        return name.replaceAll("[《》\"“”‘’'?？]", "").trim();
    }
    public boolean isLocationQuery(String userInput) {
        // 预处理输入
        userInput = userInput.toLowerCase()
                .replaceAll("[?？,.!！]", "")
                .trim();

        // ----------------- 1. 多语言关键词匹配 -----------------
        String[][][] keywordGroups = {
                // 中文组
                {
                        {"我在哪", "我的位置", "当前位置", "这是哪里", "我的地址", "当前坐标"},
                        {"我在哪儿", "俺在哪", "这是啥地方", "我在啥位置", "咯里是哪里"}
                },
                // 英文组
                {
                        {"where am i", "my location", "current position", "what is this place"},
                        {"where is me", "locate me", "show my position"}
                }
        };

        for (String[][] langGroup : keywordGroups) {
            for (String[] synonyms : langGroup) {
                for (String keyword : synonyms) {
                    if (userInput.contains(keyword)) {
                        return true;
                    }
                }
            }
        }

        // ----------------- 2. 高级句式匹配 -----------------
        Pattern[] patterns = {
                Pattern.compile(".*(我|你|当前|现在)(的?)(位置|坐标|地址|在哪[里儿]?).*"),
                Pattern.compile(".*(where is|what's|show me)( my)? (current )?(loc|position).*", Pattern.CASE_INSENSITIVE)
        };

        for (Pattern p : patterns) {
            if (p.matcher(userInput).matches()) return true;
        }


        return false;
    }
}
