package com.yl.deepseekxunfei.broadcast;

/**
 * 回家或者公司意图处理
 */
public class IntentProcessing {

    // 家庭相关关键词
    private static final String[] HOME_KEYWORDS = {
            "回家", "回家去", "我要回家", "导航回家", "回到家里", "回家路上", "我要回去了"
    };

    // 公司相关关键词
    private static final String[] WORK_KEYWORDS = {
            "回公司", "去公司", "我要上班", "导航去公司", "回到公司", "去上班", "返回公司"
    };

    public static String recognizeIntent(String input) {
        String cleanInput = input.trim().toLowerCase();

        // 检查家庭关键词
        for (String keyword : HOME_KEYWORDS) {
            if (cleanInput.contains(keyword)) {
                return "home";
            }
        }

        // 检查公司关键词
        for (String keyword : WORK_KEYWORDS) {
            if (cleanInput.contains(keyword)) {
                return "work";
            }
        }

        return "unknown";
    }
}
