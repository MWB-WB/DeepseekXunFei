package com.yl.deepseekxunfei;

import java.util.regex.Pattern;

/**
 * 换行
 */
public class TextLineBreaker {
    // 匹配中文标点符号（换行并缩进）
    private static final Pattern CHINESE_PUNCTUATION = Pattern.compile("[。？！；]");
    // 匹配英文标点符号（换行并缩进）
    private static final Pattern ENGLISH_PUNCTUATION = Pattern.compile("[!?;]");
    // 匹配其他所有标点符号（需删除）
    private static final Pattern SPECIAL_CHARS = Pattern.compile("[\\p{P}&&[^。？！；.!?;]]");
    // 1. 删除非目标标点符号

    // 缩进字符
    private static final String INDENT = "\t";

    /**
     * 处理文本：
     * 1. 去除特殊符号（保留中英文标点）
     * 2. 中英文标点后换行并缩进
     */
    public static String breakTextByPunctuation(String text) {
        // 1. 去除特殊符号（保留中英文标点）
        String cleanedText = SPECIAL_CHARS.matcher(text).replaceAll("");
        // 2. 中文标点后换行并缩进
        String formattedText = CHINESE_PUNCTUATION.matcher(cleanedText)
                .replaceAll("$0\n" + INDENT);

        // 3. 英文标点后换行并缩进
        formattedText = ENGLISH_PUNCTUATION.matcher(formattedText)
                .replaceAll("$0\n" + INDENT);

        return formattedText;
    }
}
