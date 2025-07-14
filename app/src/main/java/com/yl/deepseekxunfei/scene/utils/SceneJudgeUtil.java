package com.yl.deepseekxunfei.scene.utils;

import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//判断场景的工具类，暂时先存放在这里
public class SceneJudgeUtil {

    private static SceneJudgeUtil sceneJudgeUtil;

    public static SceneJudgeUtil getInstance() {
        if (sceneJudgeUtil == null) {
            synchronized (SceneJudgeUtil.class) {
                if (sceneJudgeUtil == null) {
                    sceneJudgeUtil = new SceneJudgeUtil();
                }
            }
        }
        return sceneJudgeUtil;
    }

    // 定义自我介绍相关的关键词和句式（支持中英文符号和变体）
    private static final String[] SELF_INTRO_KEYWORDS = {
            "你叫什么", "你的名字", "你是谁", "介绍一下你", "介绍一下自己",
            "如何称呼你", "自我介绍一下", "你是做什么的", "你的功能", "你能做什么", "你是什么东西"
    };

    // 正则表达式匹配变体句式（允许中间有空格、标点、语气词）
    private static final String SELF_INTRO_PATTERN =
            "^(.*?)(你[的名称谁岁绍能]|介绍|称呼|身份|功能|自己)(.*?)(吗|呢|啊|呀|吧|？)?$";

    // 判断是否为自我介绍问题
    public static boolean isSelfIntroduction(String input) {
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
    private static final String CALC_PATTERN =
            "^\\s*([-+]?\\d+\\.?\\d*)\\s*" +
                    "([+＋×xX*\\-\\－÷/除乘加减]|加|减|乘|除)" +
                    "\\s*([-+]?\\d+\\.?\\d*)\\s*" +
                    "(?:等于几|等于多少|是多少|结果是多少|得几)?\\s*[？?]?\\s*$";

    // 判断是否为计算问题
    public static boolean isCalculationQuestion(String input) {
        Pattern pattern = Pattern.compile(CALC_PATTERN);
        Matcher matcher = pattern.matcher(input.trim());
        return matcher.matches();
    }

    /**
     * 处理音乐播放指令（优化版）
     *
     * @param text 用户输入的指令文本
     */
    public static boolean handlePlayCommand(String text) {
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
    public static boolean isInvalidSongName(String name) {
        // 单字"歌"已在前置检查，这里主要检查其他无效情况
        return name.matches("^[\\s\\p{P}]*$"); // 全是空格或标点
    }

    /**
     * 清洗歌名中的特殊符号
     */
    public static String cleanSongName(String name) {
        return name.replaceAll("[《》\"“”‘’'?？]", "").trim();
    }

    public static boolean isLocationQuery(String userInput) {
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

    public static boolean judgeIsOpenApp(String text) {
        if (text.startsWith("打开")) {
            return true;
        }
        return false;
    }

    private static String[] musicControl = {"继续播放", "暂停播放", "上一首", "下一首"};

    public static boolean judgeIsMusicControl(String text) {
        String replaceText = text.replace("。", "");
        Optional<String> any = Arrays.stream(musicControl).filter(t -> t.equals(replaceText)).findAny();
        if (any.isPresent()) {
            return true;
        }
        return false;
    }

}
