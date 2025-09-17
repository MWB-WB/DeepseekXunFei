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

    private static String[] navControl = {"退出导航", "继续导航"};

    public static boolean judgeIsNavControl(String text){
        String replaceText = text.replace("。", "");
        Optional<String> any = Arrays.stream(navControl).filter(t -> t.equals(replaceText)).findAny();
        if (any.isPresent()) {
            return true;
        }
        return false;
    }
    private static final String[] TIME_QUERY_KEYWORDS = {
            "现在几点", "现在时间", "当前时间", "几点了", "现在几点了",
            "今天几号", "今天日期", "今天是几号", "今天是星期几",
            "当前日期", "现在日期", "现在是几点", "现在是什么时间",
            "当前是几点", "几点钟", "现在钟点", "今天星期几"
    };

    /**
     * 时间查询正则表达式（覆盖复杂句式，允许中间有语气词、空格、标点）
     * 支持场景：
     * - "请问现在几点？" "你知道现在时间吗？"
     * - "今天是2024年几月几号呀？" "当前是星期几呢？"
     * - "What time is it now?" "What's the current date?"
     */
    private static final Pattern[] TIME_QUERY_PATTERNS = {
            // 中文时间查询句式
            Pattern.compile(".*(现在|当前|今天)(是|有)?(几点|时间|日期|几号|星期几)(吗|呢|呀|啊|吧)?.*[？?]?"),
            // 英文时间查询句式（忽略大小写）
            Pattern.compile(".*(what time is it|current time|now time|today date|what's the date).*", Pattern.CASE_INSENSITIVE)
    };

    /**
     * 判断用户输入是否为“获取当前时间/日期”的请求
     * @param input 用户输入的文本（支持中文、英文，允许带标点和口语化表达）
     * @return true：是时间查询场景；false：不是
     */
    public static boolean isTimeQuery(String input) {
        // 1. 空输入检查
        if (TextUtils.isEmpty(input)) {
            return false;
        }

        // 2. 输入预处理：去除空格、标点，转为小写（统一匹配规则）
        String cleanedInput = input.trim()
                .replaceAll("[\\s\\p{P}]", "") // 移除所有空格和标点（如“现在 几点？”→“现在几点”）
                .toLowerCase(); // 转为小写（适配英文不区分大小写）

        // 3. 第一步：关键词匹配（快速判断常见场景，性能优先）
        for (String keyword : TIME_QUERY_KEYWORDS) {
            if (cleanedInput.contains(keyword)) {
                Log.d("TimeQuery", "匹配到时间查询关键词：" + keyword);
                return true;
            }
        }

        // 4. 第二步：正则表达式匹配（覆盖复杂句式，避免关键词遗漏）
        for (Pattern pattern : TIME_QUERY_PATTERNS) {
            if (pattern.matcher(cleanedInput).matches()) {
                Log.d("TimeQuery", "正则匹配到时间查询句式：" + input);
                return true;
            }
        }

        // 5. 未匹配到任何时间查询规则
        return false;
    }

    /**
     * （可选）辅助方法：提取时间查询的具体类型（仅时间/仅日期/完整时间）
     * 用于后续针对性返回结果（如用户问“今天几号”，仅返回日期；问“现在几点”，仅返回时间）
     * @param input 用户输入的文本
     * @return 时间查询类型：TIME_ONLY（仅时间）、DATE_ONLY（仅日期）、TIME_AND_DATE（完整时间）、UNKNOWN（无法识别）
     */
    public static TimeQueryType getTimeQueryType(String input) {
        if (TextUtils.isEmpty(input)) {
            return TimeQueryType.UNKNOWN;
        }

        String cleanedInput = input.trim().toLowerCase();

        // 匹配“仅时间”查询（含“几点”“时间”关键词，不含“日期”“几号”“星期”）
        if ((cleanedInput.contains("几点") || cleanedInput.contains("时间"))
                && !cleanedInput.contains("日期")
                && !cleanedInput.contains("几号")
                && !cleanedInput.contains("星期")) {
            return TimeQueryType.TIME_ONLY;
        }

        // 匹配“仅日期”查询（含“日期”“几号”“星期”关键词，不含“几点”）
        if ((cleanedInput.contains("日期") || cleanedInput.contains("几号") || cleanedInput.contains("星期"))
                && !cleanedInput.contains("几点")) {
            return TimeQueryType.DATE_ONLY;
        }

        // 匹配“完整时间”查询（同时包含时间和日期关键词，或未明确区分）
        if ((cleanedInput.contains("几点") || cleanedInput.contains("时间"))
                && (cleanedInput.contains("日期") || cleanedInput.contains("几号") || cleanedInput.contains("星期"))) {
            return TimeQueryType.TIME_AND_DATE;
        }

        // 无法明确识别类型（默认返回完整时间）
        return TimeQueryType.UNKNOWN;
    }

    /**
     * 时间查询类型枚举（用于区分用户需求，针对性返回结果）
     */
    public enum TimeQueryType {
        TIME_ONLY,       // 仅需要时间（如“现在几点”）
        DATE_ONLY,       // 仅需要日期（如“今天几号”）
        TIME_AND_DATE,   // 需要完整时间（日期+时间，如“现在是什么时候”）
        UNKNOWN          // 无法识别类型（默认返回完整时间）
    }

}
